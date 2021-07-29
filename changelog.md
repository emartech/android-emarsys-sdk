# Emarsys SDK 3.0.0

We released the current major version of the Emarsys SDK two years ago. In the meantime we have received a lot of valuable usage feedback from our users and also the development tools of the underlying platforms have improved a lot. We always strive to offer the best possible developer experience and the most up-to-date mobile feature set to our users and this sometimes requires to introduce breaking changes of our API and therefore to release a new major version. This migration guide leads you through the changes you have to make to your Emarsys SDK integration to start using the latest 3.0.0 version. We estimate that this migration can be done in about 90 minutes. We recommend to do this migration as soon as possible so that you can benefit from these improvements and also prepare your integration to benefit from future Emarsys SDK improvements. If you need any help in the migration please reach out to Emarsys support.

### [Benefits](https://github.com/emartech/android-emarsys-sdk/wiki/Migration-guide-for-Emarsys-SDK-3.0.0#benefits-of-migrating-to-emarsys-sdk-300)
Benefits of migrating to Emarsys SDK 3.0.0
* Huawei Push Kit can be integrated through the Emarsys SDK which makes it possible to send push notification from Emarsys to users of newer Huawei devices while working simultaneously also with Firebase to reach Google Android phones
* Updated dependencies
* Java 8 usage
* Cleaner and improved API
* Improved the usage of callbacks in Kotlin and Java as well
* Gave more flexibility to the developers by moving the `contactFieldId` into the `setContact` call, so it is not needed at the moment of the SDK setup

### [V3 Migration guide](https://github.com/emartech/android-emarsys-sdk/wiki/Migration-guide-for-Emarsys-SDK-3.0.0)
* Following our official migration guide will help to upgrade to this major version. Our estimation is that the migration can be done in about 90 minutes.

# What's fixed
### [EventHandlers](https://github.com/emartech/android-emarsys-sdk/wiki#25-setnotificationeventhandler)
* In 2.16.0, the SDK used the `onEventActionEventHandler` instead of `notificationEventHandler` on AppEvent buttons.
### KeyStore
* In case of an invalid, or badly implemented Android KeyStore the SDK's SecureSharedPreferences solution crashed, that could cause some invalid setup, or misbehaviour.

# Important Notes
* __Please keep in mind that after 2021.08.06 we will drop support of Android versions below Nougat (API level 24).__ For more details please visit https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#when-do-we-increase-the-minimum-android-version-required-for-the-sdk