plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
}
apply(from = "../gradle/release.gradle")

dependencies {
    api(project(":core-api"))
    api(project(":mobile-engage-api"))
    api(project(":predict-api"))

    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":predict"))
    implementation(project(":emarsys"))

    androidTestImplementation(project(":testUtils"))

    coreLibraryDesugaring(libs.android.tools.desugar)
}

android {
    namespace = "com.emarsys"
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
