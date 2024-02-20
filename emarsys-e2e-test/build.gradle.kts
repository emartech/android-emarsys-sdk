plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
}

dependencies {
    implementation(project(":emarsys-sdk"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":predict"))

    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.test.e2e"
}

kotlin {
    jvmToolchain(17)
}

//task enableMockLocationForTestsOnDevice(type: Exec, dependsOn: 'installDebugAndroidTest') {
//    Properties properties = new Properties()
//    properties.load(project.rootProject.file('local.properties').newDataInputStream())
//    def sdkDir = properties.getProperty('sdk.dir')
//    def adb = "$sdkDir/platform-tools/adb"
//    description 'enable mock location on connected android device before executing any android test'
//    commandLine "$adb", 'shell', 'appops', 'set', 'com.emarsys.test.e2e.test', 'android:mock_location', 'allow'
//}
//
//afterEvaluate {
//    connectedDebugAndroidTest.dependsOn enableMockLocationForTestsOnDevice
//    connectedAndroidTest.dependsOn enableMockLocationForTestsOnDevice
//}
//
//tasks.whenTaskAdded { task ->
//    if (task.name == 'assembleDebug') {
//        task.dependsOn('enableMockLocationForTestsOnDevice')
//    }
//}