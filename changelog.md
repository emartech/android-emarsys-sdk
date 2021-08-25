# What's fixed
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* The value of `initialEnterTrigger` was not cached between app restarts.
### Lifecycle handling
* In some cases the AndroidLifecycleObserver was not used from the Main Thread, which caused the Mobile Engage features to be disabled.
### [Push](https://github.com/emartech/android-emarsys-sdk/wiki#2-push)
* In case the application was not running the Push handling could freeze the app start for a few seconds
### [In-App](https://github.com/emartech/android-emarsys-sdk/wiki#3-inapp)
* In-App dialog Fragment was missing the default constructor which might have resulted in a crash when the In-App was presented while the Application was restored from the background

# What's changed
### [Config](https://github.com/emartech/android-emarsys-sdk/wiki/Config)
* Removed `Deprecated` annotations from Config API
### [Logging](https://github.com/emartech/android-emarsys-sdk/wiki#17-enableverboseconsolelogging)
* Verbose logging has been extended with new logs at app start

# Important Notes
* __Please keep in mind that in the next minor release we will drop support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk