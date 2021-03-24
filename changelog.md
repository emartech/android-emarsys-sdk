# What's new
### [openId](https://github.com/emartech/ios-emarsys-sdk/wiki#13-setauthenticatedcontact)
* the SDK now provides a method to authenticate users with an idToken issued by an openId compliant provider

# What's changed
### [AppEvent][https://github.com/emartech/android-emarsys-sdk/wiki#2-push]
* AppEvents are now always called from the UI thread

# What's fixed
### [In-App](https://github.com/emartech/ios-emarsys-sdk/wiki#3-in-app)
* Fixed an issue, where in-app weren't displayed until an application restart, in an edge case where the application was started without an applicationCode
### [IdlingResource](https://github.com/emartech/android-emarsys-sdk/wiki#9-testing)
* Updated the version of the idling resource
### [Session tracking](https://github.com/emartech/android-emarsys-sdk/wiki/Session-Tracking)
* Fixed an issue, where the SDK handled sessions on the SDK's thread
* Fixed an issue, where sessions weren't restarted correctly on user identification
### Async
* Fixed issues, that some of the API endpoints (Predict, trackDeviceInfo) weren't working asynchronously, and it caused a crash in the SDK
### [changeAppCode](https://github.com/emartech/android-emarsys-sdk/wiki/Config#changeapplicationcode)
* Fixed an issue, when changeAppCode was set a contact reference if there were no contact set in the SDK


# Important Notes
* Deprecated methods are going to be removed in our next major release (3.0.0)