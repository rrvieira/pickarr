package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.recommendation.RecommendationService
import com.rrvieir4.pickarr.services.tmdb.TmdbService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.task.PickarrTask
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

fun Application.launchPickarrTask(config: Config) {

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

    val pickarrTask = PickarrTask(
        popularService,
        moviesRecommendationService,
        tvRecommendationService,
        DBClient,
        config.movieRequirements,
        config.tvRequirements
    )

    launch {
        while (true) {
            val trackMoviesDeferred = async {
                pickarrTask.getRecommendedMovies()
            }
            val trackTVShowsDeferred = async {
                pickarrTask.getRecommendedTVShows()
            }

            val trackMoviesResponse = trackMoviesDeferred.await()
            val trackTVShowsResponse = trackTVShowsDeferred.await()
            val trackResponses = listOf(trackMoviesResponse, trackTVShowsResponse)

            val refreshInterval = if (trackResponses.filterIsInstance<Response.Failure<PickarrError>>().onEach {
                    it.notifyError(notificationClient)
                }.isNotEmpty()) {
                config.refreshInterval.retry
            } else {
                (trackMoviesResponse as? Response.Success)?.let {
                    notificationClient.notifyNewMovies(it.body)
                }
                (trackTVShowsResponse as? Response.Success)?.let {
                    notificationClient.notifyNewTV(it.body)
                }
                config.refreshInterval.default
            }

            delay(TimeUnit.SECONDS.toMillis(refreshInterval))
        }
    }
}

private suspend fun Response<*, PickarrError>.notifyError(notificationClient: NotificationClient) {
    (this as? Response.Failure)?.let {
        notificationClient.notifyTaskError(this.body::class.simpleName, this.body.error)
    }
}
