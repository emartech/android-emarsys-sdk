plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
}

dependencies {
    implementation(project(":core-api"))
    implementation(libs.google.tink)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.core"
}

kotlin {
    jvmToolchain(17)
}