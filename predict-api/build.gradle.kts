plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(project(":core"))
    api(project(":core-api"))
    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.predict.api"
}

kotlin {
    jvmToolchain(17)
}