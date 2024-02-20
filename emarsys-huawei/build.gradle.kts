plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
    //   alias(libs.plugins.huawei.agconnect)
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
