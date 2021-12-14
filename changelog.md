# What's changed
### [Emarsys SDK](https://github.com/emartech/android-emarsys-sdk/wiki)
* Kotlin version has been updated to 1.6.10

### [Predict](https://github.com/emartech/android-emarsys-sdk/wiki#4-predict)
* Added new validation to the `trackPurchase` method so that empty cartItems lists are no longer accepted as it would be an invalid request.

# What's fixed
### [Emarsys SDK](https://github.com/emartech/android-emarsys-sdk/wiki)
* Fixed an issue where in some edge-cases the SDK could crash below API level 28 when tried to decide if the device has internet connection
