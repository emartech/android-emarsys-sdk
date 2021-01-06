# What's fixed
### [Mobile Engage Session](https://github.com/emartech/android-emarsys-sdk/wiki/Session-Tracking)
* Fixed an issue, where session handling could happen on the UI thread.
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* Fixed an issue, that geofence triggers could cause an exception on some edge cases.
* Fixed that, the GeofenceBroadcastReceiver worked on the UI thread.
# Important Notes
* Deprecated methods are going to be removed in our next major release (3.0.0)