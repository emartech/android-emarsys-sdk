plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}
apply(from = "../gradle/release.gradle")

val sdkVersion: GitVersion by rootProject.extra

dependencies {
    implementation(project(":core-api"))
    implementation(libs.google.tink)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.activity)

    androidTestImplementation(project(":testUtils"))

    coreLibraryDesugaring(libs.android.tools.desugar)
}
android {
    namespace = "com.emarsys.core"

    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val sdkVersion: GitVersion by rootProject.extra
        buildConfigField("int", "VERSION_CODE", "${sdkVersion.versionCode}")
        buildConfigField("String", "VERSION_NAME", "\"${sdkVersion.versionName}\"")
    }

    buildFeatures {
        buildConfig = true
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