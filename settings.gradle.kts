pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        maven("https://developer.huawei.com/repo/")
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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        maven("https://developer.huawei.com/repo/")
    }
}

rootProject.name = "Android Emarsys SDK"

include(
    ":common",
    ":core",
    ":core-api",
    ":emarsys",
    ":emarsys-e2e-test",
    ":emarsys-firebase",
    ":emarsys-huawei",
    ":emarsys-sdk",
    ":mobile-engage",
    ":mobile-engage-api",
    ":predict",
    ":predict-api",
    ":sample",
    ":testUtils"
)