plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":emarsys-sdk"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":predict"))

    androidTestImplementation(project(":testUtils"))

    coreLibraryDesugaring(libs.android.tools.desugar)
}
android {
    namespace = "com.emarsys.test.e2e"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        resources {
            excludes += arrayOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }
}

allOpen {
    annotation("com.emarsys.core.Mockable")
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