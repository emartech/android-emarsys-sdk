# What's fixed
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* Fixed a race condition that caused a crash during device restart, when the geofence feature was disabled.
### [Shared Hardware ID](https://github.com/emartech/android-emarsys-sdk/wiki/Shared-Hardware-Id)
* Fixed an issue when installing two apps on the same device without starting the first app caused a problem of missing the shared hardware ID.
### [Session Tracking](https://github.com/emartech/android-emarsys-sdk/wiki/Session-Tracking#session-tracking)
* Fixed an issue regarding session handling.
### [Push](https://github.com/emartech/android-emarsys-sdk/wiki#2-push)
* Fixed a crash when internet connection was lost during image download.
### [Inbox](https://github.com/emartech/android-emarsys-sdk/wiki#7-messageinbox)
* Fixed an issue where the result listener was not called from the main thread.