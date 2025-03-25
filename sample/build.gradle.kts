plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)

//    alias(libs.plugins.huawei.agconnect)
}
val sdkVersion: GitVersion by rootProject.extra
group = "com.emarsys.sample"
version = sdkVersion.versionName
android {
    namespace = "com.emarsys.sample"
    defaultConfig {
        applicationId = "com.emarsys.sample"
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        multiDexEnabled = true

        lint {
            targetSdk = libs.versions.android.targetSdk.get().toInt()
        }
        multiDexEnabled = true
        versionCode = sdkVersion.versionCode
        versionName = sdkVersion.versionName

        testInstrumentationRunner = "com.emarsys.sample.testutils.SampleAppTestRunner"
        resValue("string", "sdk_version", sdkVersion.versionName)
        buildConfigField(
            "String",
            "GOOGLE_OAUTH_SERVER_CLIENT_ID",
            "\"${
                env.fetch(
                    "GOOGLE_OAUTH_SERVER_CLIENT_ID",
                    System.getenv("GOOGLE_OAUTH_SERVER_CLIENT_ID") ?: ""
                )
            }\""
        )
        buildConfigField(
            "String",
            "FIREBASE_WEB_SERVER_CLIENT_ID",
            "\"${
                env.fetch(
                    "FIREBASE_WEB_SERVER_CLIENT_ID",
                    System.getenv("FIREBASE_WEB_SERVER_CLIENT_ID") ?: ""
                )
            }\""
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    allOpen {
        annotation("kotlin.AnyClass")
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    if (env.fetch("RELEASE_MODE", (System.getenv("RELEASE_MODE") ?: "false")) == "true") {
        signingConfigs {
            create("release") {
                storePassword = env.fetch(
                    "ANDROID_RELEASE_STORE_PASSWORD",
                    (System.getenv("ANDROID_RELEASE_STORE_PASSWORD") ?: "")
                )
                keyAlias = env.fetch(
                    "ANDROID_RELEASE_KEY_ALIAS",
                    (System.getenv("ANDROID_RELEASE_KEY_ALIAS") ?: "")
                )
                keyPassword = env.fetch(
                    "ANDROID_RELEASE_KEY_PASSWORD",
                    (System.getenv("ANDROID_RELEASE_KEY_PASSWORD") ?: "")
                )
                storeFile = file(
                    "./mobile-team-android.jks"
                )
            }
        }
        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
                isMinifyEnabled = false
                isDebuggable = false
                proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            multiDexKeepProguard = file("proguard-multidex-rules.pro")
            multiDexEnabled = true
        }
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

dependencies {
//    if (env.fetch(
//            "USE_LOCAL_DEPENDENCY",
//            (System.getenv("USE_LOCAL_DEPENDENCY") ?: "false")
//        ) == "true"
//    ) {
        implementation(project(":emarsys-sdk"))
        implementation(project(":emarsys-firebase"))
        implementation(project(":emarsys-huawei"))
//    } else {
//        implementation("com.emarsys:emarsys-sdk:+")
//        implementation("com.emarsys:emarsys-firebase:+")
//        implementation("com.emarsys:emarsys-huawei:+")
//    }
    // Getting a "Could not find firebase-core" error? Make sure you have
    // the latest Google Repository in the Android SDK manager

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.material)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.appcompat)
    implementation(libs.io.coil)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.io.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotpref)
    implementation(libs.google.firebase.common)
    implementation(libs.google.firebase.messaging)
    implementation(libs.play.services.auth)
    implementation(libs.google.gson)
    debugImplementation(libs.androidx.compose.ui.tooling)

    coreLibraryDesugaring(libs.android.tools.desugar)
}
