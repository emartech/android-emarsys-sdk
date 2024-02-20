plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}
dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.appcompat)
    api(libs.kotlin.stdlib)
    api(libs.androidx.espresso.idling.resource)
    api(libs.androidx.lifecycle.common.java8)
    api(libs.androidx.lifecycle.process)
    api(libs.androidx.security.crypto)
    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.core.api"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}

allOpen {
    annotation("com.emarsys.core.Mockable")
}

kotlin { jvmToolchain(17) }