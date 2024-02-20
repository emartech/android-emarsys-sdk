plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
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
}
kotlin { jvmToolchain(17) }