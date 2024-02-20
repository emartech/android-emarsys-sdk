plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinAllOpen)
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core"))
    implementation(project(":mobile-engage"))
    implementation(project(":mobile-engage-api"))

    api(libs.huawei.agconnect.core)
    api(libs.huawei.hms.push)
    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.huawei"
}

kotlin {
    jvmToolchain(17)
}
