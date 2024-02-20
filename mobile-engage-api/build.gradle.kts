plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
}

dependencies {
    implementation(project(":core-api"))

    api(libs.google.location)

    androidTestImplementation(project(":testUtils"))
}

android {
    namespace = "com.emarsys.mobileengage.api"
}

kotlin {
    jvmToolchain(17)
}
