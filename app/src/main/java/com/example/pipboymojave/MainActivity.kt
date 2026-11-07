package com.example.pipboymojave

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pipboymojave.ui.screens.AddItemScreen
import com.example.pipboymojave.ui.screens.AssessmentScreen
import com.example.pipboymojave.ui.screens.AuthScreen
import com.example.pipboymojave.ui.screens.CommunityScreen
import com.example.pipboymojave.ui.screens.CompareScreen
import com.example.pipboymojave.ui.screens.JournalScreen
import com.example.pipboymojave.ui.screens.LedgerScreen
import com.example.pipboymojave.ui.screens.MissionsScreen
import com.example.pipboymojave.ui.screens.NewEntryScreen
import com.example.pipboymojave.ui.screens.PipBottomBar
import com.example.pipboymojave.ui.screens.SensorScreen
import com.example.pipboymojave.ui.screens.SharedReportScreen
import com.example.pipboymojave.ui.screens.StatusScreen
import com.example.pipboymojave.ui.theme.PipBoyTheme

class MainActivity : FragmentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PipBoyTheme { Root() }
        }
    }

    @Composable
    private fun Root() {
        val authed by vm.authenticated.collectAsState()
        var error by remember { mutableStateOf<String?>(null) }

        if (!authed) {
            AuthScreen(errorMessage = error, onAuthenticate = { authenticate { ok, msg ->
                if (ok) vm.authenticated.value = true else error = msg
            } })
        } else {
            AuthedApp()
        }
    }

    @Composable
    private fun AuthedApp() {
        val nav = rememberNavController()
        val context = LocalContext.current

        // Start/stop live sensors with the screen lifecycle.
        val owner = LocalLifecycleOwner.current
        DisposableEffect(owner) {
            val obs = LifecycleEventObserver { _, e ->
                when (e) {
                    Lifecycle.Event.ON_START -> vm.sensors.start()
                    Lifecycle.Event.ON_STOP -> vm.sensors.stop()
                    else -> Unit
                }
            }
            owner.lifecycle.addObserver(obs)
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) vm.sensors.start()
            onDispose { owner.lifecycle.removeObserver(obs); vm.sensors.stop() }
        }

        val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            vm.sensors.onPermissionsChanged()
        }
        val requestPerms = {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }
        // Ask once on first entry; the Sensor Array also lets the user retry.
        LaunchedEffect(Unit) {
            val missing = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACTIVITY_RECOGNITION
            ).any { ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (missing) requestPerms()
        }

        val bar: @Composable (String) -> @Composable () -> Unit = { selected ->
            { PipBottomBar(selected) { go(nav, it) } }
        }

        NavHost(nav, startDestination = "stat") {
            composable("stat") { StatusScreen(vm, bar("stat")) }
            composable("sens") { SensorScreen(vm, { requestPerms() }, bar("sens")) }
            composable("jrnl") {
                JournalScreen(vm, onNew = { nav.navigate("newEntry") }, onCompare = { nav.navigate("compare") }, bottomBar = bar("jrnl"))
            }
            composable("ldgr") { LedgerScreen(vm, onAdd = { nav.navigate("addItem") }, bottomBar = bar("ldgr")) }
            composable("msns") { MissionsScreen(vm, onStartAssessment = { nav.navigate("assessment") }, bottomBar = bar("msns")) }
            composable("comm") { CommunityScreen(vm, onOpen = { nav.navigate("report/$it") }, bottomBar = bar("comm")) }

            composable("newEntry") { NewEntryScreen(vm) { nav.popBackStack() } }
            composable("compare") { CompareScreen(vm) { nav.popBackStack() } }
            composable("addItem") { AddItemScreen(vm) { nav.popBackStack() } }
            composable("assessment") { AssessmentScreen(vm) { nav.popBackStack() } }
            composable("report/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                SharedReportScreen(vm, id, onBack = { nav.popBackStack() }, onCompare = { nav.navigate("compare") }, bottomBar = bar("comm"))
            }
        }
    }

    private fun go(nav: NavHostController, route: String) {
        nav.navigate(route) {
            popUpTo("stat") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /** Biometric unlock with device-credential fallback. Falls through if the device has no lock at all. */
    private fun authenticate(result: (Boolean, String?) -> Unit) {
        val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        val can = BiometricManager.from(this).canAuthenticate(authenticators)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            // No screen lock / biometrics set up: let the user in, but say so.
            result(true, null)
            return
        }
        val prompt = BiometricPrompt(
            this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) = result(true, null)
                override fun onAuthenticationError(code: Int, msg: CharSequence) = result(false, msg.toString())
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Pip-Boy: Mojave")
                .setSubtitle("Unlock your journal and ledger")
                .setAllowedAuthenticators(authenticators)
                .build()
        )
    }
}
