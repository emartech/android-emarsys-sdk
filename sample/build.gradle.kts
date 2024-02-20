plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.google.services)
//    alias(libs.plugins.huawei.agconnect)
}

group = "com.emarsys.sample"

android {
    namespace = "com.emarsys.sample"
    defaultConfig {
        applicationId = "com.emarsys.sample"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        lint {
            targetSdk = libs.versions.android.targetSdk.get().toInt()
        }
        multiDexEnabled = true
        val version: GitVersion by rootProject.extra
        versionCode = version.versionCode
        versionName = version.versionName

        testInstrumentationRunner = "com.emarsys.sample.testutils.SampleAppTestRunner"
        resValue("string", "sdk_version", version.versionName)
        buildConfigField(
            "String",
            "GOOGLE_OAUTH_SERVER_CLIENT_ID",
            "\"${env.GOOGLE_OAUTH_SERVER_CLIENT_ID.value}\""
        )
    }

    buildFeatures {
        compose = true
    }

    allOpen {
        annotation("com.emarsys.core.Mockable")
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get().toString()
    }

    if (env.RELEASE_MODE.orElse("false") == "true") {
        signingConfigs {
            create("release") {
                storePassword = env.ANDROID_RELEASE_STORE_PASSWORD.value
                keyAlias = env.ANDROID_RELEASE_KEY_ALIAS.value
                keyPassword = env.ANDROID_RELEASE_KEY_PASSWORD.value
                storeFile = file(env.ANDROID_RELEASE_STORE_FILE_BASE64.value)
            }
        }
        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
                isMinifyEnabled = false
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
    packagingOptions {
        resources {
            excludes += arrayOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }
}

dependencies {
    if (env.USE_LOCAL_DEPENDENCY.orElse("false") == "true") {
        implementation(project(":emarsys-sdk"))
        implementation(project(":emarsys-firebase"))
        implementation(project(":emarsys-huawei"))
    } else {
        implementation("com.emarsys:emarsys-sdk:+")
        implementation("com.emarsys:emarsys-firebase:+")
        implementation("com.emarsys:emarsys-huawei:+")
    }
    // Getting a "Could not find firebase-core" error? Make sure you have
    // the latest Google Repository in the Android SDK manager

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.21")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.android.support:cardview-v7:28.0.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("io.coil-kt:coil:2.5.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.chibatching.kotpref:kotpref:2.13.2")
    implementation("com.google.firebase:firebase-common-ktx:20.4.2")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    implementation("com.google.code.gson:gson:2.10.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
}