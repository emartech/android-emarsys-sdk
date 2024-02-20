plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":core"))
    api(project(":core-api"))
    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.predict.api"
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