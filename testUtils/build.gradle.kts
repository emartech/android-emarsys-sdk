plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "com.emarsys.testUtil"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        multiDexEnabled = true
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
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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
    api(libs.junit)
    api(libs.kotest.assertions.core)
    api(libs.kotest.runner.junit4)
    api(libs.mockito.android)
    api(libs.mockito.kotlin)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.multidex)
    api(libs.androidx.test.core)
    api(libs.mockk.android)
    api(libs.mockk.agent)

    coreLibraryDesugaring(libs.android.tools.desugar)
}
configurations {
    api {
        exclude(group = "io.mockk", module = "mockk-agent-jvm")
    }
}