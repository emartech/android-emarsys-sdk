plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("maven-publish")
    signing
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