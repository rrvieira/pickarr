package com.rrvieir4.pickarr

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.plugins.configureRouting
import com.rrvieir4.pickarr.plugins.launchRecommendationsUpdater
import com.rrvieir4.pickarr.server.TelegramServer
import com.rrvieir4.pickarr.services.recommendation.RecommendationNotifier
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

fun main() {

    embeddedServer(Netty, port = 7877, host = "0.0.0.0") {
        log.info("Pickarr is booting...")
        val config = Config.setupFromEnv()
        if (config == null) {
            log.info("Invalid config. Going to exit...")
            exitProcess(1)
        }

        val recommendationNotifier = RecommendationNotifier(config)

        configureRouting(config)
        launchRecommendationsUpdater(config, recommendationNotifier)

        //TODO
        launch {
            TelegramServer(config.telegramConfig.telegramUserToken, recommendationNotifier).run()
        }


    }.start(wait = true)
}
