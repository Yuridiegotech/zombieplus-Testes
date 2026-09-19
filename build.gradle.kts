plugins {
    id("java")
    id("io.qameta.allure") version "2.12.0"
}

group = "org.zombieplus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val allureVersion = "2.29.0"

dependencies {
    implementation("com.microsoft.playwright:playwright:1.53.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.javafaker:javafaker:1.0.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.postgresql:postgresql:42.7.1")

    // Allure JUnit 5 & Attachments
    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-junit5")
    testImplementation("io.qameta.allure:allure-java-commons")
}

allure {
    version.set(allureVersion)
    adapter {
        frameworks {
            junit5 {
                adapterVersion.set(allureVersion)
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}