plugins{
    kotlin("jvm") version "1.6.10"
    id("java-gradle-plugin")
}

group = "io.github.justinsalzburg.med-plugin"
version = "0.0.1"

repositories {
    gradlePluginPortal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin: 1.4.21")
}