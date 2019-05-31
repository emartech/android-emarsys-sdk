# Migrate from Mobile Engage SDK

This is a guide on how to move from the Mobile Engage SDK to the new Emarsys SDK. This guide only covers the actual migration from the Mobile Engage SDK to the Emarsys SDK, please look at the [README](README.md) for more general details on how to get started with the Emarsys SDK.

## Project Configuration

### AndroidManifest.xml

```xml
<service android:name="com.emarsys.mobileengage.service.MobileEngageMessagingService">
  <intent-filter>
    <action android:name="com.google.firebase.MESSAGING_EVENT"/>
  </intent-filter>
</service>
```
↓
```xml
<service android:name="com.emarsys.service.EmarsysMessagingService">
  <intent-filter>
    <action android:name="com.google.firebase.MESSAGING_EVENT"/>
  </intent-filter>
</service>
```

### gradle.build

#### Emarsys SDK dependency

```
dependencies {
  implementation 'com.emarsys:mobile-engage-sdk:+'
}
```

```
dependencies {
  implementation 'com.emarsys:emarsys-sdk:+'
}
```

#### Android Target SDK

The play store does not allow new apps to target less than 28 API level starting August 2019, nor updates to existing apps starting November 2019. The SDK has been adjusted accordingly.

```
android {
  compileSdkVersion 26
  defaultConfig {
    ...
    minSdkVersion 16
    targetSdkVersion 26
  }
}
```
↓
```
android {
  compileSdkVersion 28
    defaultConfig {
    ...
    minSdkVersion 19
    targetSdkVersion 28
  }
}
```

## Default Channels

On android, the android notification channels must be set up, there is no longer the functionality of a default android channel: the app developer is expected to setup the appropriate channels instead, and match what the marketers use to send push notifications. This means that `.enableDefaultChannel()` should be removed when building the configuration. If you were explicitly disabling the default channel the `.disableDefaultChannel()` call when building the config can also be removed.

## Methods

### appLogin()

A call of `MobileEngage.appLogin` without parameters is no longer necessary. You no longer login anonymously, instead upon registering your device, we will automatically create an anonymous contact if we never saw this device.

### appLogin(contactFieldId, contactFieldvalue)

The workflow for linking a device to a contact was changed slightly. Instead of passing both the *contactFieldId* and the *contactFieldValue* when the user logs in, you now only need to send the *contactFieldValue*. The *contactFieldId* is set once during the configuration of the EmarsysSDK.

```kotlin
MobileEngage.appLogin(contactFieldId, contactFieldvalue)
```
↓
```kotlin
EmarsysConfig config = new EmarsysConfig.Builder()
  ...
  .contactFieldId(3)
  ...
  .build();

Emarsys.setContact(contactFieldValue)
```

### appLogout

```kotlin
MobileEngage.appLogout()
```
↓
```kotlin
Emarsys.clearContact()
```

### setPushToken

```kotlin
MobileEngage.setPushToken(deviceToken)
```
↓
```kotlin
Emarsys.Push.setPushToken(deviceToken)
```

### setPushToken(null)

If you were calling the `setPushToken` method with `null` in order to remove the token you need to change those calls to use the dedicated method `removePushToken` instead.

```kotlin
MobileEngage.setPushToken(null)
```
↓
```kotlin
Emarsys.Push.removePushToken()
```

### trackMessageOpen(info)

```kotlin
MobileEngage.trackMessageOpen(userInfo)
```
↓
```kotlin
Emarsys.Push.trackMessageOpen(userInfo)
```

### setStatusListener

The `MobileEngage.setStatusListener` method was removed, you can now specify a completion listener for each method instead.

```swift
MobileEngage.setStatusListener(listener)
```
↓
```kotlin
Emarsys.Push.setPushToken(token, listener)
```
