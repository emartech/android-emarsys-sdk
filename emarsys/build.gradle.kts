plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
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
}

kotlin {
    jvmToolchain(17)
}