# What's fixed
### [Dependencies](https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#emarsys-sdk-uses-an-other-version-of-kotlin-than-my-application-should-i-be-concerned)
* Updated some minor dependencies. Related commits are [this](https://github.com/emartech/android-emarsys-sdk/commit/a5fe0622edcab152a744d9ee2238e89af318f2a0) and [this](https://github.com/emartech/android-emarsys-sdk/commit/fd1a94c6ad742fabf61cdc3b63f77ccdfc1f924a).

### [In-App](https://github.com/emartech/android-emarsys-sdk/wiki#3-inapp)
* Fixed a crash, which caused Push to In-App to crash the SDK, when the application was in the background.
* Related to this, we fixed an issue, that the SDK handled some of the responses in the UI thread, what caused In-App messages to load slower then they should.


# Important Notes
* __Please keep in mind that after 2021.08.06 we will drop support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk
* Deprecated methods are going to be removed in our next major release (3.0.0)
* Kotlin version will be updated from 1.4.32 -> 1.5.0 in the next minor release!
