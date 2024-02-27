plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":core-api"))
    implementation(project(":mobile-engage-api"))

    implementation(libs.androidx.webkit)

    androidTestImplementation(project(":testUtils"))

    coreLibraryDesugaring(libs.android.tools.desugar)
}
android {
    namespace = "com.emarsys.mobileengage"
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