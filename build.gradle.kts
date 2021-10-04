val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val jsoupVersion : String by project

plugins {
    application
    kotlin("jvm") version "1.5.30"
}

group = "com.rrvieir4"
version = "0.0.1"
application {
    mainClass.set("com.rrvieir4.pickarr.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation ("org.jsoup:jsoup:$jsoupVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}