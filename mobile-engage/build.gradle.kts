plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
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
}

kotlin {
    jvmToolchain(17)
}