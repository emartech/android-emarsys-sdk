import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.nexus.publish)
}
apply(from = "../gradle/release.gradle")

dependencies {
    implementation(project(":core"))
    implementation(project(":core-api"))
    implementation(project(":predict-api"))

    androidTestImplementation(project(":testUtils"))

    coreLibraryDesugaring(libs.android.tools.desugar)
}

android {
    namespace = "com.emarsys.predict"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        resources {
            excludes += arrayOf(
                "README.txt",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "**/attach_hotspot_windows.dll",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/licenses/ASM"
            )
        }
    }
}

allOpen {
    annotation("com.emarsys.core.Mockable")
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )
    coordinates(group.toString(), "predict", version.toString())

    pom {
        name = "predict"
        description = "predict module of the EmarsysSDK"
        url.set("https://github.com/emartech/android-emarsys-sdk")
        licenses {
            license {
                name.set("Mozilla Public License 2.0")
                url.set("https://github.com/emartech/android-emarsys-sdk/blob/master/LICENSE")
            }
        }
        organization {
            name.set("Emarsys")
            url.set("https://emarsys.com")
        }
        developers {
            developer {
                organization.set("Emarsys")
                organizationUrl.set("https://emarsys.com")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/emartech/android-emarsys-sdk.git")
            developerConnection.set("scm:git:https://github.com/emartech/android-emarsys-sdk.git")
            url.set("https://github.com/emartech/android-emarsys-sdk")
        }
    }
}
