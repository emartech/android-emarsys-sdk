plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
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
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        resources {
            excludes += arrayOf(
                "README.txt",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "**/attach_hotspot_windows.dll",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/licenses/ASM"
            )
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