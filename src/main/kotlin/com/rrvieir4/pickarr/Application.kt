package com.rrvieir4.pickarr

import com.rrvieir4.pickarr.plugins.setupRouting
import com.rrvieir4.pickarr.plugins.runRecommendationTrackerJob
import com.rrvieir4.pickarr.plugins.setupDI
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

    embeddedServer(Netty, port = 7877, host = "0.0.0.0") {
        log.info("Pickarr is booting...")

        setupDI()
        setupRouting()

        runRecommendationTrackerJob()
    }.start(wait = true)
}
