# PILOT VERSION

This SDK is still in Pilot phase, please only use if you have a pilot agreement contract in place!

If you are looking for our recommended SDK then please head to [Mobile Engage SDK](https://github.com/emartech/android-mobile-engage-sdk "Mobile Engage SDK")

# Emarsys SDK

The Emarsys SDK enables you to use Mobile Engage and Predict in a very straightforward way. By incorporating the SDK in your
app, we support you, among other things, in handling credentials, API calls, tracking of opens and events as well as logins and
logouts in the app.
The Emarsys SDK is open sourced to enhance transparency and to remove privacy concerns. This also means that you can always
be up-to-date with what we are working on.

Using the SDK is also beneficial from the product aspect: it simply makes it much easier to send push messages through your app. Please always use the latest version of the SDK in your app.

## Contents

- [Usage](#1-usage)
- [Requirements](#2-requirements)
- [Documentation](https://github.com/emartech/android-emarsys-sdk/wiki)
- [Migrate from Mobile Engage SDK](https://github.com/emartech/android-emarsys-sdk/wiki/Migrate-from-Mobile-Engage)
- [Mobile Engage Glossary](https://github.com/emartech/android-emarsys-sdk/wiki/Glossary)
- [Obtaining Firebase Cloud Messaging credentials](https://github.com/emartech/android-emarsys-sdk/wiki/Obtaining-Firebase-Cloud-Messaging-credentials)
- [Android Oreo Channels](https://github.com/emartech/android-emarsys-sdk/wiki/Android-Oreo-Channels)
- [Rich Push Notifications](https://github.com/emartech/android-emarsys-sdk/wiki/Rich-Push-Notifications)
- [DeepLink](https://github.com/emartech/android-emarsys-sdk/wiki/DeepLink)
- [FAQ](https://github.com/emartech/android-emarsys-sdk/wiki/FAQ)


## 1. Usage
### 1.1 Installation with Gradle

Gradle is a build system for Android, which automates and simplifies the process of using 3rd-party libraries.


### 1.2 Add Gradle dependency

To integrate EmarsysSDK into your Android project using Gradle, specify it in your application's build.gradle file:

```groovy
dependencies {
	implementation 'com.emarsys:emarsys-sdk:2.0.0'
}
```

## 2. Requirements

* The minimum Android version should be api level 19 at least.
* Requires compileSdkVersion 28 or higher
* Emarsys SDK is using AndroidX

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
android:name="com.emarsys.small_notification_icon"
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
            Emarsys.Push.trackMessageOpen(remoteMessage);
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
            Emarsys.Push.trackMessageOpen(remoteMessage)
        }
    }
}
```

> For further informations about how to use our SDK please visit our [Documentation](https://github.com/emartech/android-emarsys-sdk/wiki)

