# What's new
### [getPushToken](https://github.com/emartech/android-emarsys-sdk/wiki#22-getpushtoken)
* the SDK now provides a method under push which returns the pushToken
### [isAutomaticPushSendingEnabled](https://github.com/emartech/android-emarsys-sdk/wiki/Config#automaticpushsendingenabled)
* the SDK now provides a method under config which returns if the automaticPushSending is enabled or not
### [sdkVersion](https://github.com/emartech/android-emarsys-sdk/wiki/Config#sdkversion)
* the SDK now provides a method under config which returns the current version of Emarsys SDK 
### [verboseConsoleLogging](https://github.com/emartech/android-emarsys-sdk/wiki#16-enableverboseconsolelogging)
* verbose console logging is now available in the Emarsys SDK
# What's fixed
* Fixed an issue, where Inline In-App buttonClicks were not reported 
* Fixed nullPointerException in EmarsysMessagingService when deviceInfo was accessed before the setup finished 
# Important Notes
* Deprecated methods are going to be removed in our next major release (3.0.0)