pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven(url = "https://developer.huawei.com/repo/")
        maven(url = "https://ajoberstar.org/bintray-backup/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.google.com")
        maven(url = "https://developer.huawei.com/repo/")

    }
}

include(
    ":common",
    "core",
    ":core-api",
    "emarsys",
    ":emarsys-e2e-test",
    "emarsys-firebase",
    ":emarsys-huawei",
    "emarsys-sdk",
    ":mobile-engage",
    "mobile-engage-api",
    ":predict",
    "predict-api",
    ":sample",
    "testUtils"
)