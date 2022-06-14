# What's fixed
### [Push](https://github.com/emartech/android-emarsys-sdk/wiki#2-push)
* Fixed an issue what happened when Firebase and Huawei messaging were both used. Emarsys SDK now can detect what platform is in used in a more stable way
### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)
* Fixed an issue where the system tried to unregister broadcast receiver on Geofence disable multiple times and it crashed