package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.onSuccess
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.storage.models.RecommendedItem
import com.rrvieir4.pickarr.task.PickarrTask
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

fun Application.launchPickarrService(): Boolean {

    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    val config = Config.setupFromEnv() ?: return false


    val notificationClient = TelegramClient(
        config.telegramConfig.telegramUserToken,
        config.telegramConfig.telegramChatId,
        config.telegramConfig.telegramActionUrl
    )

    val popularService = ImdbService(httpClient)
    val moviesService = RadarrService(config.radarrConfig, httpClient)
    val tvShowsService = SonarrService(config.sonarrConfig, httpClient)

    val pickarrTask = PickarrTask(
        popularService,
        moviesService,
        tvShowsService,
        DBClient,
        config.movieRequirements,
        config.tvRequirements
    )

    launch {
        while (true) {
            val trackMoviesResponse = pickarrTask.getRecommendedMovies()
            val trackTVShowsResponse = pickarrTask.getRecommendedTVShows()
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

    return true
}

private suspend fun Response<*, PickarrError>.notifyError(notificationClient: NotificationClient) {
    (this as? Response.Failure)?.let {
        notificationClient.notifyTaskError(this.body::class.simpleName, this.body.error)
    }
}
