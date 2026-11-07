package com.example.pipboymojave.data

import android.content.Context
import com.google.gson.Gson

/** Simple on-device persistence. Everything stays local unless the user publishes an entry. */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("pipboy_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): AppData {
        val json = prefs.getString(KEY, null) ?: return SampleData.seed()
        return try {
            gson.fromJson(json, AppData::class.java) ?: SampleData.seed()
        } catch (e: Exception) {
            SampleData.seed()
        }
    }

    fun save(data: AppData) {
        prefs.edit().putString(KEY, gson.toJson(data)).apply()
    }

    private companion object {
        const val KEY = "app_data_v1"
    }
}
