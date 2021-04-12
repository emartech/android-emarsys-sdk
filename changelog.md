# What's new
### [mavenCentral](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)
* Since the sunset of the jCenter, from now on the SDK is also accessible from [mavenCentral](https://search.maven.org/artifact/com.emarsys/emarsys-sdk).
> __Important: new versions won't be available on jCenter after the 1st of May!__

# What's fixed
### ClientService
* Fixed an issue, where trackDeviceInfo was still called on UI thread.
### [Dependencies](https://github.com/emartech/android-emarsys-sdk/wiki/FAQ#emarsys-sdk-uses-an-other-version-of-kotlin-than-my-application-should-i-be-concerned)
* Updated Kotlin 1.4.31 -> 1.4.32, and some minor dependencies. Related commit is [here](https://github.com/emartech/android-emarsys-sdk/commit/9db012a1985b75e82b1c9130600cee8d27095544).
### [In-App](https://github.com/emartech/android-emarsys-sdk/wiki#3-inapp)
* SDK now displays In-Apps even when if FragmentActivity is used instead of just AppCompatActivity.


# Important Notes
* Deprecated methods are going to be removed in our next major release (3.0.0)