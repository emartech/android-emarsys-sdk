# What's new
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* If it's enabled, now the SDK sends an event (geofence EXIT) also when the mobile device leaves the area of a configured geofence.
> Note: The Geofence feature is still in [pilot phase](https://github.com/emartech/android-emarsys-sdk/wiki#pilot-version)!

# What's fixed
### [Dependencies](https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#emarsys-sdk-uses-an-other-version-of-kotlin-than-my-application-should-i-be-concerned)
* Updated some minor dependencies. Related commit is [here](https://github.com/emartech/android-emarsys-sdk/commit/bf59374f131fe7718b6c5dfdddbde940895bfc21).
### Threading
* There were some edge-cases when LifecycleObserver addObserver were called on a thread other than the UI thread.
* It caused a crash, but only in the SDK, it could have caused some issues in the session reporting/handling. Now it's always added on the UI thread.
### [Inline In-App](https://github.com/emartech/android-emarsys-sdk/wiki#32-inline-in-app)
* Fixed a crash, which happened when an Inline In-App were displayed before finishing the `Emarsys.setup()`.


# Important Notes
* __Please keep in mind that after 2021.08.06 we will drop support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk
* Deprecated methods are going to be removed in our next major release (3.0.0)
* Kotlin version will be updated from 1.4.32 -> 1.5.0 in the next minor release!
