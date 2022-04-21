plugins{
    kotlin("jvm") version "1.6.10"
    id("java-gradle-plugin")
}

group = "io.github.justinsalzburg.med-gradle-plugin"
version = "0.0.1"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
}

gradlePlugin{
    plugins{
        create("medPlugin"){
            id = "io.github.justinsalzburg.med-gradle-plugin"
            implementationClass = "io.github.justinsalzburg.medgradleplugin.MEDPlugin"
        }
    }
}

