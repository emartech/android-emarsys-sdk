# What's new
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
When [`initialEnterTriggerEnabled`](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence) is true, Emarsys SDK will trigger all the affected geofences with Enter type triggers at the moment when the geofence is enabled if the device is already inside that geofence

# What's fixed
### [In-App](https://github.com/emartech/android-emarsys-sdk/wiki#3-inapp)
* Fixed an issue, where the app crashed when `webView` was not available
### [Application Version](https://github.com/emartech/android-emarsys-sdk/wiki/Data-flows-in-the-SDK)
* Fixed an issue, where application version updates were not reported by the SDK

# Important Notes
* __Please keep in mind that after 2021.08.06 we will drop support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk
* Deprecated methods are going to be removed in our next major release (3.0.0)
* Emarsys SDK is going to force Java 8 from the next major release (3.0.0)
* Kotlin version will be upgraded from 1.4.32 -> 1.5.x in the next major release (3.0.0)