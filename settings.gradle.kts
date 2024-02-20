pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven(url = "https://developer.huawei.com/repo/")
        maven(url = "https://ajoberstar.org/bintray-backup/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.huawei") {
                if (requested.id.id == "com.huawei.agconnect") {
                    useModule("com.huawei.agconnect:agcp:${requested.version}")
                }
            }
        }
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.google.com")
        maven(url = "https://developer.huawei.com/repo/")
        maven(url = "https://ajoberstar.org/bintray-backup/")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
    }
}
rootProject.name = "Android Emarsys SDK"

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