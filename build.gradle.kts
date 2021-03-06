val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val jsoupVersion: String by project
val kmongoVersion: String by project
val kapacheCommonsVersion: String by project
val ktelegrambotVersion: String by project
val koinVersion: String by project

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
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("org.litote.kmongo:kmongo:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongoVersion")
    implementation("org.apache.commons:commons-text:$kapacheCommonsVersion")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:$ktelegrambotVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}