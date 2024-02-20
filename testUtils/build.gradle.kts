plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.emarsys.testUtil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        lint {
            targetSdk = libs.versions.android.targetSdk.get().toInt()
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            multiDexEnabled = true
        }
    }
    packagingOptions {
        resources {
            excludes += arrayOf("README.txt", "META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }

    lint {
        abortOnError = false
    }
    kotlin {
        jvmToolchain(17)
    }
}

group = "com.emarsys.testUtil"

dependencies {
    implementation(libs.androidx.appcompat)
    api(libs.androidx.test.extensions)
    debugApi(libs.androidx.test.fragment)

    api(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    api(libs.kotest.assertions.core)
    api(libs.mockito.android)
    api(libs.mockito.core)
    api(libs.mockito.kotlin)
    api(libs.byte.buddy)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.multidex)
    api(libs.mockk.android)
    api(libs.mockk.agent)
}
configurations {
    api {
        exclude(group = "io.mockk", module = "mockk-agent-jvm")
    }
}