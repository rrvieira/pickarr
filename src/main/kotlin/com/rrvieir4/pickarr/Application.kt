package com.rrvieir4.pickarr

import com.rrvieir4.pickarr.plugins.configureRouting
import com.rrvieir4.pickarr.plugins.launchPickarrService
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        log.info("Pickarr is booting...")
        configureRouting()
        launchPickarrService()
    }.start(wait = true)
}
