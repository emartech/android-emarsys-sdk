import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.google.services) apply false
//    alias(libs.plugins.huawei.agconnect) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.grgit)
    alias(libs.plugins.dotenv)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.nexus.publish) apply false
    alias(libs.plugins.compose.compiler) apply false
}

versionData()

allprojects {
    val sdkVersion: GitVersion by rootProject.extra
    group = "com.emarsys"
    version = sdkVersion.versionName

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
        compilerOptions.apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
    }

    plugins.withId("com.android.library") {
        val kotlinExtension = extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java)
            ?: error("Kotlin extension not found on $path; cannot pin coreLibrariesVersion")
        kotlinExtension.coreLibrariesVersion = libs.versions.kotlinCoreLibraries.get()
    }
}

fun versionData() {
    val git = Grgit.open(
        mapOf("currentDir" to project.rootDir)
    )
    val v = try {
        git.fetch()
        if (git.describe() == null) {
            throw RuntimeException("Couldn't get Version Name")
        }
        GitVersion(
            versionName =
                (if (System.getenv("RELEASE_VERSION") == null) git.describe() else System.getenv(
                    "RELEASE_VERSION"
                )),
            versionCode = ((System.currentTimeMillis() - 1602845230) / 10000).toInt(),
            versionCodeTime = git.head().dateTime.toEpochSecond()
        )

    } catch (ignored: Exception) {
        GitVersion(
            versionName = "0.0.0",
            versionCode = 1,
            versionCodeTime = 0
        )
    }
    val sdkVersion by extra(v)

    println("versionName: ${sdkVersion.versionName}")
    println("versionCode: ${sdkVersion.versionCode}")
}

allprojects {
    // Exclude Kotlin files from Javadoc generation because Kotlin files are not supported by Dokka
    tasks.withType(Javadoc::class).all {
        enabled = false
    }
}