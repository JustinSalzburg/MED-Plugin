plugins{
    kotlin("jvm") version "1.6.10"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "io.github.justinsalzburg.med-gradle-plugin"
version = "0.0.1"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2")

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

pluginBundle {
    website = "https://github.com/JustinSalzburg/MED-Plugin"
    vcsUrl = "https://github.com/JustinSalzburg/MED-Plugin"
    description = "Docs plugin to support documentation of a project"

    (plugins) {
        "medPlugin" {
            displayName = "Gradle MED plugin"
            tags = listOf("docs", "documentation")
        }
    }

    mavenCoordinates {
        groupId = "io.github.justinsalzburg"
        artifactId = "med-gradle-plugin"
        version = "0.0.1"
    }
}


