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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
}

tasks.named("build") {
    dependsOn(gradle.includedBuild("plugin").task(":build"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
