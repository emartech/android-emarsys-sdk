import org.ajoberstar.grgit.Grgit
import java.util.Base64

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.google.services) apply false
//    alias(libs.plugins.huawei.agconnect) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.grgit)
    alias(libs.plugins.dotenv)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.android.junit5) apply false
}

versionData()

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
            if (System.getenv("RELEASE_VERSION") == null) git.describe() else System.getenv(
                "RELEASE_VERSION"
            ),
            versionCode = ((System.currentTimeMillis() - 1602845230) / 10000).toInt(),
            versionCodeTime = git.head().dateTime.toEpochSecond()
        )

    } catch (ignored: Exception) {
        GitVersion(
            versionName = "0.0.0",
            versionCode = 0,
            versionCodeTime = 0
        )
    }
    val version by extra(v)

    println("versionName: ${version.versionName}")
    println("versionCode: ${version.versionCode}")
}

tasks {
    register("base64EnvToFile") {
        val propertyName = project.property("propertyName") as String?
            ?: throw IllegalArgumentException("Property 'propertyName' is not provided.")
        val file = project.property("file") as String?
            ?: throw IllegalArgumentException("Property 'file' is not provided.")

        doLast {
            val base64String = env.fetch(propertyName)
            val decoder = Base64.getDecoder()
            val decodedString = String(decoder.decode(base64String))

            val outputFile = file(file)
            outputFile.writeText(decodedString)
        }
    }
}