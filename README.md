# Pip-Boy: Mojave (Android, Kotlin + Jetpack Compose)

Open this folder in Android Studio (Koala or newer), let Gradle sync, then run on a **real phone**
(emulators have few real sensors).

## What's implemented
- Biometric / device-credential unlock (Authenticate screen)
- Status sheet: Stamina, Awareness, Stealth, Navigation, Environment scores from live sensors
- Sensor Array: accelerometer graph (X/Y/Z), gyroscope, magnetometer + heading, step counter,
  barometer, light, proximity, microphone (rough dB estimate), GPS accuracy
- Field Journal: new entries with camera photo, readings, tags, publish toggle; Compare visits
- Resource Ledger: add items, low-stock warnings, log use / restock
- Missions: XP, rewards, 4-step Field Assessment wizard
- Community: browse sample shared reports, save offline, checklist, compare

## Known limits
- Community is sample data only (no backend). Publishing just marks an entry as PUBLISHED locally.
- Label/receipt scan in Add Item is not implemented (button replaced with a note).
- Sound level is uncalibrated. Scores are game values, not health/safety metrics.
- Fonts: Gothic 821 Condensed (headings), Overseer Oblique (body/labels), SF Americana Dreams Bold (amber script), Add City Boy (buttons) are in res/font. These are freeware; check each licence before publishing the app.
- Your supplied images (avatar, caps, Pip-Boy, RadAway, scrap parts, matches, whiskey, medical items, barrel, lunchbox) are in res/drawable-nodpi with white backgrounds removed.
- Not compiled in this environment (no network). If Gradle reports a version issue, update the
  plugin/BOM versions in build.gradle.kts files.
