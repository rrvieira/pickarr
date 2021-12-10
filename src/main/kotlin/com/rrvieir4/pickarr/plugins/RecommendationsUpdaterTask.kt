package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.recommendation.RecommendationNotifier
import io.ktor.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

fun Application.launchRecommendationsUpdater(config: Config, recommendationNotifier: RecommendationNotifier) {

    launch {
        while (true) {
            if (recommendationNotifier.run(true)) {
                delay(TimeUnit.SECONDS.toMillis(config.refreshInterval.default))
            } else {
                delay(TimeUnit.SECONDS.toMillis(config.refreshInterval.retry))
            }
        }
    }
}
