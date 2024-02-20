plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-api"))
    implementation(project(":predict-api"))

    androidTestImplementation(project(":testUtils"))
}

android {
    namespace = "com.emarsys.predict"
}

kotlin {
    jvmToolchain(17)
}
