# What's changed

### [Config](https://github.com/emartech/android-emarsys-sdk/wiki/Config#hardwareid)

* Introduced clientId as a clearer alternative to the now-deprecated hardwareId; both refer to the same property, and while we encourage using clientId, this is not a breaking change as hardwareId remains available for backward compatibility.

### [Geofence](https://github.com/emartech/android-emarsys-sdk/wiki#8-geofence)

* Improved support for Roboelectric tests.


* # What's fixed

### [ChangeApplicationCode](https://github.com/emartech/android-emarsys-sdk/wiki/Config#changeapplicationcode)

* Fix an issue where changeApplicationCode was not working properly with anonymous contacts.

### [Push](https://github.com/emartech/android-emarsys-sdk/wiki#2-push)

* Fix an issue where on Android API level 35 clicking on the push message would not start the application.