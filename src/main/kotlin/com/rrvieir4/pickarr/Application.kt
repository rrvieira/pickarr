package com.rrvieir4.pickarr

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.plugins.configureRouting
import com.rrvieir4.pickarr.plugins.launchRecommendationTracker
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlin.system.exitProcess

fun main() {

    embeddedServer(Netty, port = 7877, host = "0.0.0.0") {
        log.info("Pickarr is booting...")
        val config = Config.setupFromEnv()
        if (config == null) {
            log.info("Invalid config. Going to exit...")
            exitProcess(1)
        }

        configureRouting(config)
        launchRecommendationTracker(config)
    }.start(wait = true)
}
