# What's new
### [Android 12](https://developer.android.com/about/versions/12/behavior-changes-12)
* Android 12 support is added to the SDK.

### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* Registered geofences can now be [accessed from the SDK](https://github.com/emartech/android-emarsys-sdk/wiki#84-registeredGeofences).

# What's fixed
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* Geofences are now re-registered on device boot.
### [Predict](https://github.com/emartech/android-emarsys-sdk/wiki#4-predict)
* `recommendProducts` CompletionHandlers were called on a background thread, which could cause some crashes on invoke. Now they are delegated to the UI thread.
### [Logging](https://github.com/emartech/android-emarsys-sdk/wiki#17-enableverboseconsolelogging)
* App start logs are now more informative.

# Important Notes
* The Android 12 changes broke the geofencing feature, and the opening of the application from push messages. Earlier versions of the SDK do not contain the fixes of these issues. For more details please visit the [following wiki page](https://github.com/emartech/android-emarsys-sdk/wiki/Android-12).
* __We dropped support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk