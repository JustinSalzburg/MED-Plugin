plugins {
    kotlin("jvm") version "1.6.10"
    id("io.github.justinsalzburg.med-gradle-plugin") version "0.0.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {

    implementation("io.github.justinsalzburg.med-gradle-plugin:plugin:1.1.8")
}
