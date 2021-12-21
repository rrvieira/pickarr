package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.recommendation.RecommendationService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.services.tmdb.TmdbService
import com.rrvieir4.pickarr.services.utils.unwrap
import com.rrvieir4.pickarr.task.RecommendationTracker
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

fun Application.launchRecommendationTracker(config: Config) {

    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    val notificationClient = TelegramClient(
        config.telegramConfig.telegramUserToken,
        config.telegramConfig.telegramChatId,
        config.actionUrlConfig.actionUrl,
        config.actionUrlConfig.addMovieMethod,
        config.actionUrlConfig.addTVMethod
    )

    val popularService = ImdbService(httpClient)
    val moviesService = RadarrService(config.radarrConfig, httpClient)
    val tvShowsService = SonarrService(config.sonarrConfig, httpClient)
    val tmdbService = TmdbService(config.tmdbConfig, httpClient)

    val moviesRecommendationService = RecommendationService(tmdbService, moviesService)
    val tvRecommendationService = RecommendationService(tmdbService, tvShowsService)

    val recommendationTracker = RecommendationTracker(
        popularService,
        moviesRecommendationService,
        tvRecommendationService,
        DBClient,
        config.movieRequirements,
        config.tvRequirements
    )

    launch {
        while (true) {
            val recommendedMoviesDeferred = async {
                recommendationTracker.getRecommendedMovies()
            }
            val recommendedTVShowsDeferred = async {
                recommendationTracker.getRecommendedTVShows()
            }

            val (recommendedMovies, trackMoviesError) = recommendedMoviesDeferred.await().unwrap()
            val (recommendedTVShows, trackTVShowsError) = recommendedTVShowsDeferred.await().unwrap()
            val errorsList = listOf(trackMoviesError, trackTVShowsError)

            val refreshInterval = if (errorsList.filterNotNull().onEach {
                    notificationClient.notifyTaskError(it::class.simpleName, it.message)
                }.isNotEmpty()) {
                config.refreshInterval.retry
            } else {
                recommendedMovies?.let {
                    notificationClient.notifyNewMovies(it)
                }
                recommendedTVShows?.let {
                    notificationClient.notifyNewTV(it)
                }
                config.refreshInterval.default
            }

            delay(TimeUnit.SECONDS.toMillis(refreshInterval))
        }
    }
}
