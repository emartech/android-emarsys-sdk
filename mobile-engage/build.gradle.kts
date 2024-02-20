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
}
android {
    namespace = "com.emarsys.mobileengage"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}

allOpen {
    annotation("com.emarsys.core.Mockable")
}

kotlin {
    jvmToolchain(17)
}