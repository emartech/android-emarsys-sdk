plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "com.emarsys.testUtil"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        multiDexEnabled = true
        lint {
            targetSdk = libs.versions.android.targetSdk.get().toInt()
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArgument(
            "runnerBuilder",
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
        )

    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            multiDexEnabled = true
        }
    }
    packaging {
        resources {
            excludes += arrayOf("README.txt", "META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        abortOnError = false
    }
    kotlin {
        jvmToolchain(17)
    }

}
group = "com.emarsys.testUtil"

dependencies {
    implementation(libs.androidx.appcompat)
    api(libs.androidx.test.extensions)
    debugApi(libs.androidx.test.fragment)

    api(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    api(libs.kotest.assertions.core)
    api(libs.mockito.android)
    api(libs.mockito.kotlin)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.multidex)
    api(libs.androidx.test.core)
    api(libs.mockk.android)
    api(libs.mockk.agent)

    api("de.mannodermaus.junit5:android-test-core:1.4.0")
    api("de.mannodermaus.junit5:android-test-runner:1.4.0")
    api("de.mannodermaus.junit5:android-test-extensions:1.4.0")
    api("org.junit.jupiter:junit-jupiter-api:5.10.0")
    api("org.junit.jupiter:junit-jupiter:5.10.0")
    api("org.junit-pioneer:junit-pioneer:2.2.0")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    androidTestApi("org.junit.jupiter:junit-jupiter-api:5.10.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.4.0")

    coreLibraryDesugaring(libs.android.tools.desugar)
}
configurations {
    api {
        exclude(group = "io.mockk", module = "mockk-agent-jvm")
    }
}
junitPlatform {}