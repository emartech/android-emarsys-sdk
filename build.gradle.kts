import org.ajoberstar.grgit.Grgit
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.navigationSafeArgs) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.kotlinAllOpen) apply false
    alias(libs.plugins.grGit)
    id("com.github.ben-manes.versions") version "0.46.0"
}

versionData()
loadDevConfig()

fun loadDevConfig() {

    ext["devConfig"] = Properties()

    try {
        val inputStream = FileInputStream("$projectDir/localConfig.properties")
        (ext["devConfig"] as Properties).load(inputStream)

    } catch (ignore: Exception) {
    }

    if (ext["devConfig"] != null) {
        println("Using devConfig: ${ext["devConfig"]}")
    }
}

fun versionData() {
    val git = Grgit.open(
        mapOf("currentDir" to project.rootDir)
    )

    if ((if (System.getenv("BLACKDUCK") == null) false else System.getenv("BLACKDUCK")) == false) {
        git.fetch()
    }
    if (git.describe() == null) {
        throw RuntimeException("Couldn't get Version Name")
    }
    val v = GitVersion(
        versionName =
        if (System.getenv("RELEASE_VERSION") == null) git.describe() else System.getenv(
            "RELEASE_VERSION"
        ),
        versionCode = ((System.currentTimeMillis() - 1602845230) / 10000).toInt(),
        versionCodeTime = git.head().time
    )
    ext["gitVersionName"] = v.versionName
    ext["gitVersionCode"] = v.versionCode
    ext["gitVersionCodeTime"] = v.versionCodeTime

    println("versionName: ${v.versionName}")
    println("versionCode: ${v.versionCode}")

}