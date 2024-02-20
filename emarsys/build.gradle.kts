plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}

dependencies {
    api(project(":core-api"))
    api(project(":mobile-engage-api"))
    api(project(":predict-api"))

    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":predict"))

    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.inner"
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