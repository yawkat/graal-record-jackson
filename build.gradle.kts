plugins {
    java
    id("org.graalvm.buildtools.native") version "0.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0-SNAPSHOT")
    implementation("org.graalvm.sdk:graal-sdk:22.0.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}