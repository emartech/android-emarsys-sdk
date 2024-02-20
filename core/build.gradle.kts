plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(project(":core-api"))
    implementation(libs.google.tink)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(project(":testUtils"))
}
android {
    namespace = "com.emarsys.core"
    defaultConfig {
        val version: GitVersion by rootProject.extra

        compileSdk = libs.versions.android.compileSdk.get().toInt()
        buildConfigField("int", "VERSION_CODE", "${version.versionCode}")
        buildConfigField("String", "VERSION_NAME", "\"${version.versionName}\"")
    }
}

allOpen {
    annotation("com.emarsys.core.Mockable")
}

kotlin {
    jvmToolchain(17)
}