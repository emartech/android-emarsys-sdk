# What's fixed

### [SetPushToken](https://github.com/emartech/android-emarsys-sdk/wiki#21-setpushtoken)

* Fix an issue where the push token was not tracked automatically by the SDK.

* ### [Inapp](https://github.com/emartech/android-emarsys-sdk/wiki#3-inapp)

* Fix an issue where the content of the inapp message was not reloaded when the activity was recreated from a destroyed state, or after an orientation change.

### [Emarsys SDK](https://github.com/emartech/android-emarsys-sdk/wiki)

* Fix an issue where on certain devices encryption-decryption failed due to a KeyStore error.

### [Emarsys SDK](https://github.com/emartech/android-emarsys-sdk/wiki)

* Fix an issue where on initialization the current activity was null and caused a crash.
