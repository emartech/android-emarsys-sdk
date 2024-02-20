plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":mobile-engage-api"))

    api(libs.google.fcm, { exclude(group = "androidx") })

    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.firebase"
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