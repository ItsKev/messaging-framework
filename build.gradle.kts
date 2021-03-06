plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("com.github.johnrengelman.shadow")
    }

    group = "net.itskev"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.20")
        annotationProcessor("org.projectlombok:lombok:1.18.20")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
        testImplementation("org.mockito:mockito-junit-jupiter:3.12.0")

        testCompileOnly("org.projectlombok:lombok:1.18.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
    }

    tasks.test {
        useJUnitPlatform()
    }

    val tokens = mapOf("VERSION" to project.version)

    tasks.withType<ProcessResources> {
        filesMatching("*.yml") {
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        withJavadocJar()
    }

    publishing {
        publications {
            create<MavenPublication>("java") {
                groupId = project.group.toString()
                artifactId = project.name.toLowerCase()
                version = project.version.toString()

                from(components["java"])
            }
        }
    }
}
