# PILOT VERSION

This SDK is still in Pilot phase, please only use if you have a pilot agreement contract in place!

If you are looking for our recommended SDK then please head to [Mobile Engage SDK](https://github.com/emartech/android-mobile-engage-sdk "Mobile Engage SDK")

## What is the Emarsys SDK?

The Emarsys SDK enables you to use Mobile Engage and Predict in a very straightforward way. By incorporating the SDK in your
app, we support you, among other things, in handling credentials, API calls, tracking of opens and events as well as logins and
logouts in the app.
The Emarsys SDK is open sourced to enhance transparency and to remove privacy concerns. This also means that you can always
be up-to-date with what we are working on.

Using the SDK is also beneficial from the product aspect: it simply makes it much easier to send push messages through your app. Please always use the latest version of the SDK in your app.

## Why Emarsys SDK over Mobile Engage SDK?

We learned a lot from running Mobile Engage SDK in the past 2 years and managed to apply these learnings and feedbacks in our new SDK.

##### The workflow for linking/unlinking a contact to a device was too complex:

* We removed anonymous contacts from our API. This way you can always send behaviour events, opens without having the complexity to login first with an identified contact or use hard-to-understand anonymous contact concept.

##### The API was stateful and limited our scalability

* We can scale with our new stateless or state savvy APIs in the backend We now include anonymous inapp metrics support

* We would like to make sure we understand end to end the experience of your app users and give you some insight through the data platform

##### Kotlin first approach

* We have improved the interoperability of our SDK with Kotlin. Using our SDK from Kotlin is now more convenient.

#####  Repetition of arguments

* We have improved the implementation workflow, so the energy is spent during the initial integration but not repeated during the life time of the app

#####  Unification of github projects

* The Predict SDK, The Emarsys core SDK, the Mobile Engage SDK and the corresponding sample app are all now in a single repository. You can now find up to date and tested usage examples easily


## Emarsys SDK Android integration guide

### 1. Gradle
#### 1.1 Installation with Gradle

Gradle is a build system for Android, which automates and simplifies the process of using 3rd-party libraries.


#### 1.2 Add Gradle dependency

To integrate EmarsysSDK into your Android project using Gradle, specify it in your application's build.gradle file:

```groovy
dependencies {
	implementation 'com.emarsys:emarsys-sdk:2.0.0'
}
```

### 2. Requirements

* The minimum Android version should be api level 19 at least.
* Requires compileSdkVersion 28 or higher
* Emarsys SDK is using AndroidX

#### 2.1 Using AndroidX
See [Migrating to AndroidX](https://developer.android.com/jetpack/androidx/migrate "AndroidX Migration Documentation") to learn how to migrate an existing project.

If you want to use Emarsys SDK in a new project, you need to set the compile SDK to Android 9.0 (API level 28) or higher and set both of the following Android Gradle plugin flags to true in your gradle.properties file.

android.useAndroidX: When set to true, the Android plugin uses the appropriate AndroidX library instead of a Support Library. The flag is false by default if it is not specified.
android.enableJetifier: When set to true, the Android plugin automatically migrates existing third-party libraries to use AndroidX by rewriting their binaries. The flag is false by default if it is not specified.

#### 2.2 Firebase

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

### 3. Usage
#### 3.1 Initialization
To configure the SDK, we should do the following in the `onCreate` method in the application class:


```java
public class SampleApplication extends Application {

private static final String TAG = "SampleApplication";

@Override
public void onCreate() {
super.onCreate();
    EmarsysConfig config = new EmarsysConfig.Builder()
    .application(this)
    .mobileEngageCredentials(<applicationCode:String>, <applicationPassword: String>)
    .contactFieldId(<contactFieldId: Integer>)
    .merchantId(<predictMerchantId:String>)
    .inAppEventHandler(getInAppEventHandler())
    .notificationEventHandler(getNotificationEventHandler())
    .build();

    Emarsys.setup(config);
    }
}
```

```kotlin
class SampleApplication: Application() {

    override fun onCreate() {
    super.onCreate()
    val config = EmarsysConfig.Builder()
    .application(this)
    .mobileEngageCredentials(<applicationCode: String>, <applicationPassword: String>)
    .contactFieldId(<contactFieldId: Integer>)
    .merchantId(<predictMerchantId:String>)
    .inAppEventHandler(getInAppEventHandler())
    .notificationEventHandler(getNotificationEventHandler())
    .build()
    Emarsys.setup(config)
    }
}
```

#### 3.2 CompletionListener
Most calls can receive a `completionListener` as parameter, that we could use to track our calls. The `CompletionListener` defines one
method:

```java
@Override
public void onCompleted(@Nullable Throwable errorCause) {
    if(errorCause != null){
        Log.e(TAG, error.getMessage(), error);
    }
}
```

#### 3.3 setContact
After application setup is finished, you can use `setContact` method to identify the user with a `contactFieldValue`. Without `setContact` all event will be tracked as anonymous usage. Please note that `contactFieldValue` parameter is required but the [CompletionListener](#32-completionlistener) is optional

```java
Emarsys.setContact(String contactFieldValue, CompletionListener completionListener);
```

```kotlin
Emarsys.setContact(contactFieldValue: String, completionListener: CompletionListener? = null)
```

#### 3.4 clearContact
When the user sign out, we should use the `clearContact` method with the [CompletionListener](#32-completionlistener) which is optional. The method is going to automatically log in an anonymous user instead of the one leaving. 

```java
Emarsys.clearContact(CompletionListener completionListener);
```
```kotlin
Emarsys.clearContact(completionListener: CompletionListener? = null)
```

#### 3.5 trackCustomEvent
If you want to track custom events, the `trackCustomEvent` method should be used, where the `eventName` parameter is required, but the `attributes` and the [CompletionListener](#32-completionlistener) are optional.

```java
Emarsys.trackCustomEvent(String eventName, Map<String,String> attributes, CompletionListener completionListener);
```
```kotlin
Emarsys.trackCustomEvent(eventName: String, attributes: Map<String, String>?, completionListener: CompletionListener? = null)
```

### 4. Push

#### 4.1 setPushToken
Emarsys SDK automatically handles `setPushToken` for the device and it is recommended to leave this to the SDK. However if you have your custom implementation of [MessagingService](#22-firebase), please use `setPushToken()` method where [CompletionListener](#32-completionlistener) parameter is optional, to set the `pushToken`.

```java
Emarsys.Push.setPushToken(String pushToken,CompletionListener completionListener);
```
```kotlin
Emarsys.Push.setPushToken(pushToken:String, completionListener: CompletionListener? = null)
```

#### 4.2 clearPushToken
If you want to remove `pushToken` for the Contact, please use `clearPushToken()` method where [CompletionListener](#32-completionlistener) parameter is optional

```java
Emarsys.Push.removePushToken(CompletionListener completionListener);
```
```kotlin
Emarsys.Push.removePushToken(completionListener: CompletionListener? = null)
```

#### 4.3 trackMessageOpen
Emasrys SDK automatically handles whether the push messages have been opened, however if you want to track it manually the `trackMessageOpen` method should be used, where the `intent` parameter is required but the [CompletionListener](#32-completionlistener) is optional

```java
Emarsys.Push.trackMessageOpen(Intent intent, CompletionListener completionListener);
```
```kotlin
Emarsys.Push.trackMessageOpen(intent: Intent, completionListener: CompletionListener? = null)
```

### 5. Inbox

#### 5.1 fetchNotifications
In order to receive the inbox content, you can use the `fetchNotifications` method.

```java
Emarsys.Inbox.fetchNotifications(new ResultListener<Try<NotificationInboxStatus>>() {
    @Override
    public void onResult(@NonNull
    Try<NotificationInboxStatus> result) {
        if (result.getResult() != null) {
            NotificationInboxStatus inboxStatus = result.getResult();
            Log.i(TAG, "Badge count: " + inboxStatus.badgeCount);
        }
        if (result.getErrorCause() != null) {
            Throwable cause = result.getErrorCause();
            Log.e(TAG, "Error happened: " + cause.getMessage());
        }
    }
});
```

```kotlin 
    Emarsys.Inbox.fetchNotifications { result ->
        result.result?.let { inboxStatus ->
            Log.i(TAG, "Badge count: ${inboxStatus.badgeCount}")
        }
        result.errorCause?.let {
            Log.e(TAG, "Error happened: ${it.message}")
        }
    }
```

#### 5.2 resetBadgeCount

When your user opened the application inbox you might want to reset the unread count (badge). To do so you can use the `resetBadgeCount` method with an optional [CompletionListener](#32-completionlistener).

```java
Emarsys.Inbox.resetBadgeCount(CompletionListener completionListener);
```
```kotlin
Emarsys.Inbox.resetBadgeCount(completionListener: CompletionListener? = null)
```

#### 5.3 trackNotificationOpen

To track the notification opens in inbox, use the following `trackNotificationOpen` method Where the `notification` that's being viewed is required but the [CompletionListener](#32-completionlistener) is optional.

```java
Emarsys.Inbox.trackNotificationOpen(Notification notification, CompletionListener completionListener);
```
```kotlin
Emarsys.Inbox.trackNotificationOpen(notification: Notification, completionListener: CompletionListener? = null)
```

### 6. InApp
#### 6.1 pause
When a critical activity starts and should not be interrupted by InApp, use `pause` to pause InApp messages.

```java
Emarsys.InApp.pause();
```
```kotlin
Emarsys.InApp.pause()
```

#### 6.2 resume

In order to show inApp messages after being paused use the `resume` method.

```java
Emarsys.InApp.resume();
```
```kotlin
Emarsys.InApp.resume();
```

#### 6.3 setEventHandler

In order to get an event, triggered from the InApp message, you can register for it using the `setEventHandler` method.

```java
Emarsys.InApp.setEventHandler(EventHandler() {
    
    @Override
    public void handleEvent(String eventName, @Nullable JSONObject payload) {
        ...
    }
});
```
```kotlin
Emarsys.InApp.setEventHandler { eventName, payload -> ...}
```

### 7. Predict
> Please be informed that Predict is not available with the current version of the Emarsys SDK

We won't go into the details to introduce how Predict works, and what are the capabilities, but here we aim to explain the mapping between the Predict commands and our interface.
Please visit Predict's [documentation](https://dev.emarsys.com/v2/web-extend-command-reference "Predict documentation") for more details.

#### 7.1 Initialization
To use Predict functionality you have to setup your `merchantId` during the initialization of the SDK.
In order to track Predict events you can use the methods available on our Predict interface.


#### 7.2 trackCart
When you want to track the cart items in the basket you can call the `trackCart` method with a list of CartItems. `CartItem` is an interface
which can be used in your application for your own CartItems and then simply use the same items with the SDK.

```java
Emarsys.Predict.trackCart(List<CartItem> items);
```
```kotlin
Emarsys.Predict.trackCart(items: List<CartItem>)
```

#### 7.3 trackPurchase

To report a purchase event you should call `trackPurchase` with the items purchased and with an `orderId`.

```java
Emarsys.Predict.trackPurchase(String orderId, List<CartItem> items);
```
```kotlin
Emarsys.Predict.trackPurchase(orderId: String, items: List<CartItem>)
```

#### 7.4 trackItemView

If an item was viewed use the `trackItemView` method with an `itemId` as required parameter

```java
Emarsys.Predict.trackItemView(String itemId);
```
```kotlin
Emarsys.Predict.trackItemView(itemId: String)
```

#### 7.5 trackCategoryView

When the user navigates between the categories you should call `trackCategoryView` in every navigation. Be aware to send `categoryPath`
in the required format. Please visit [Predict's documentation]((https://dev.emarsys.com/v2/web-extend-command-reference) "Predict documentation") for more information .

```java
Emarsys.Predict.trackCategoryView(String categoryPath)
```
```kotlin
Emarsys.Predict.trackCategoryView(categoryPath: String)
```

#### 7.6 trackSearchTerm

To report search terms entered by the contact use `trackSearchTerm` method.

```java
Emarsys.Predict.trackSearchTerm(String searchTerm)
```
```kotlin
Emarsys.Predict.trackSearchTerm(searchTerm: String)
```

#### 7.7 trackCustomEvent

When we want to track custom events, we should use the `trackCustomEvent` method, where, the `eventName` parameter is required, but the `attributes` and [CompletionListener](#32-completionlistener) are optional.

```java
Emarsys.trackCustomEvent(String eventName, Map<String,String> attributes,CompletionListener completionListener);
```
```kotlin
Emarsys.trackCustomEvent(eventName: String, attributes: Map<String,String>?, completionListener: CompletionListener? = null)
```

### 8. DeepLink
#### 8.1 Handle DeepLinks
Emasrys SDK automatically handles deep link tracking with most use cases, with only one exception: manual tracking is needed when your Activity has onNewIntent overriden. In that case, you can track the deep link using the `trackDeepLink` method like below:

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Emarsys.trackDeepLink(this, intent, new CompletionListener(){
    
    	 @Override
         public void onCompleted(@Nullable Throwable errorCause) {
         ...               
         }
    );
}
```
```kotlin
override fun onNewIntent(intent:Intent) {
    super.onNewIntent(intent)
    Emarsys.trackDeepLink(this, intent) {throwable -> ...}
}
```

# FAQ

#### The Firebase dependencies are outdated in the Emarsys SDK.  When will you release a new version with up-to-date dependencies?

We are trying our best to keep our dependencies up-to-date by periodically releasing new SDK versions with updated dependency versions.

If it is urgent and you cannot wait till the new release, you can manually exclude the transitive dependencies in your module level gradle file and add the Firebase dependencies manually.

You should replace the + signs with the most recent concrete versions.

```groovy
dependencies {
    implementation('com.emarsys:emarsys-sdk:+', {
        exclude group: 'com.google.firebase', module: 'firebase-core'
        exclude group: 'com.google.firebase', module: 'firebase-messaging'
    })
 
    implementation 'com.google.firebase:firebase-core:+'
    implementation 'com.google.firebase:firebase-messaging:+'
 }
```

#### The default `EmarsysMessagingService` that ships with the Emarsys SDK does not suite our requirements, we need extra features. How can these features be included in the service?

The main purpose of the default FCM services in the SDK is to provide an implementation that works for you out of the box when integrating the SDK. We try to keep the services generic so that they suite the default use cases of our customers. If you have special requirements, we suggest you to implement your own Firebase services based on our default implementation. When implementing your own FCM service, make sure to handle messageOpens correctly.


#### I get a 'Could not find firebase-core' error in gradle when trying to integrate the SDK.

This is not a Emarsys, but a Firebase issue, consult the Firebase [documentation](https://firebase.google.com/docs/cloud-messaging/android/client "FCM Documentation") to solve the problem.


#### Push messages don't arrive to the device

Did you download `google-services.json`? If not please follow the Firebase [documentation](https://firebase.google.com/docs/cloud-messaging/android/client "FCM Documentation") to setup your app with firebase first.
Make sure the connection to Firebase is not limited by company firewall rules.
Make sure you see push tokens in your SDK Logs inside Mobile Engage UI


#### Missing Push tokens
Make sure you registered our `EmarsysMessagingService` in your `AndroidManifest.xml`.


#### Missing messageOpen events
Make sure you registered our `EmarsysMessagingService` in your `AndroidManifest.xml`.