![build](https://github.com/emartech/android-emarsys-sdk/workflows/Last%20commit%20build/badge.svg)

## Contents

- [What is the Emarsys SDK?](#what-is-the-emarsys-sdk "Emarsys SDK")
- [1. Setup](#1-setup)
    - [1.1 Sample App](#11-sample-app "Sample App")
    - [1.2 Installation with Gradle](#12-installation-with-gradle "Gradle")
    - [1.3 Add Gradle dependency](#13-add-gradle-dependency "Add as dependency")
- [2. Requirements](#2-requirements "Requirements")
    - [2.1 Using AndroidX](#21-using-androidx "AndroidX")
    - [2.2 Firebase](#22-firebase "Firebase")
- [Documentation](https://github.com/emartech/android-emarsys-sdk/wiki "Documentation")
- [Migrate from Mobile Engage SDK](https://github.com/emartech/android-emarsys-sdk/wiki/Migrate-from-Mobile-Engage "Migration Guide")
- [Mobile Engage Glossary](https://github.com/emartech/android-emarsys-sdk/wiki/Glossary "Glossary")
- [Obtaining Firebase Cloud Messaging credentials](https://github.com/emartech/android-emarsys-sdk/wiki/Obtaining-Firebase-Cloud-Messaging-credentials "Firebase credentials")
- [Android Oreo Channels](https://github.com/emartech/android-emarsys-sdk/wiki/Android-Oreo-Channels "Oreo Channels")
- [Rich Push Notifications](https://github.com/emartech/android-emarsys-sdk/wiki/Rich-Push-Notifications "Rich Push Notifications")
- [DeepLink](https://github.com/emartech/android-emarsys-sdk/wiki/DeepLink "DeepLink")
- [FAQ](https://github.com/emartech/android-emarsys-sdk/wiki/FAQ "FAQ")


# What is the Emarsys SDK?

The Emarsys SDK enables you to use Mobile Engage and Predict in a very straightforward way. By incorporating the SDK in your
app, we, among other things, support you in handling credentials, API calls, tracking of opens and events as well as logins and
logouts in the app.
The Emarsys SDK is open-sourced to enhance transparency and to remove privacy concerns. This also means that you will always
be up-to-date with what we are working on.

Using the SDK is also beneficial from the product aspect: it simply makes it much easier to send push messages through your app. Please always use the latest version of the SDK in your app.

## 1. Setup

### 1.1 Sample app

We created a sample application to help in the integration and give an example. Find instructions for the build process [here](https://github.com/emartech/android-emarsys-sdk/blob/master/sample).


### 1.2 Installation with Gradle

Gradle is a build system for Android, which automates and simplifies the process of using 3rd-party libraries.


### 1.3 Add Gradle dependency

To integrate EmarsysSDK into your Android project using Gradle, specify it in your application's build.gradle file:

```groovy
dependencies {
	implementation 'com.emarsys:emarsys-sdk:‹latest_released_version_of_emarsys-sdk›'
}
```

## 2. Requirements

* The minimum Android version should be at least API level 21.
* Requires compileSdkVersion 28 or higher.
* Emarsys SDK is using AndroidX.

### 2.1 Using AndroidX
See [Migrating to AndroidX](https://developer.android.com/jetpack/androidx/migrate "AndroidX Migration Documentation") to learn how to migrate an existing project.

If you want to use Emarsys SDK in a new project, you need to set the compile SDK to Android 9.0 (API level 28) or higher and set both of the following Android Gradle plugin flags to true in your gradle.properties file.

android.useAndroidX: When set to true, the Android plugin uses the appropriate AndroidX library instead of a Support Library. The flag is false by default if it is not specified.

android.enableJetifier: When set to true, the Android plugin automatically migrates existing third-party libraries to use AndroidX by rewriting their binaries. The flag is false by default if it is not specified.

### 2.2 Firebase

When the pushToken arrives we need to set it using `Emarsys.Push.setPushToken()`. For more information about how to obtain your
FCM token please consult the Firebase integration guides:

* [Firebase core integration](https://firebase.google.com/docs/android/setup "Firebase Integration Guide")
* [Firebase messaging](https://firebase.google.com/docs/cloud-messaging/android/client "FCM Documentation")


The recommended way of using the SDK is to enable the Emarsys SDK to automatically handle `setPushToken` and `trackMessageOpen` calls for you, please register this service in your manifest:

```xml
<service android:name="com.emarsys.service.EmarsysMessagingService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT"/>
    </intent-filter>
</service>
```

Additionally, you can set a custom notification icon, by specifying it as a meta-data between your application tag:

```xml
<meta-data
android:name="com.emarsys.mobileengage.small_notification_icon"
android:resource="@drawable/notification_icon" />
```

If it is needed, you can add your custom implementation to handle `setPushToken` and `trackMessageOpen`.

###### Java
```java
public class MyMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Emarsys.Push.setPushToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);

        boolean handledByEmarsysSDK = EmarsysMessagingServiceUtils.handleMessage(this, remoteMessage);

        if (!handledByEmarsysSDK) {
            //handle your custom push message here
            ...
        }
    }
}
```

###### Kotlin
```kotlin
class MyMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Emarsys.Push.setPushToken(token!!)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        val handledByEmarsysSDK =
        EmarsysMessagingServiceUtils.handleMessage(this, remoteMessage)
        if (!handledByEmarsysSDK) {
            //handle your custom push message here
            ...
        }
    }
}
```

> For further informations about how to use our SDK please visit our [Documentation](https://github.com/emartech/android-emarsys-sdk/wiki)

