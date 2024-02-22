plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

kotlin {
    jvmToolchain(17)
}