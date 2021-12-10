package com.rrvieir4.pickarr.services.recommendation

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.services.tmdb.TmdbService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RecommendationNotifier(config: Config) {

    private val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private val notificationClient = TelegramClient(
        config.telegramConfig.telegramUserToken,
        config.telegramConfig.telegramChatId,
        config.actionUrlConfig.actionUrl,
        config.actionUrlConfig.addMovieMethod,
        config.actionUrlConfig.addTVMethod
    )

    private val popularService = ImdbService(httpClient)
    private val tmdbService = TmdbService(config.tmdbConfig, httpClient)

    private val moviesRecommendationMapperService =
        RecommendationItemListProvider(tmdbService, RadarrService(config.radarrConfig, httpClient))
    private val tvRecommendationMapperService =
        RecommendationItemListProvider(tmdbService, SonarrService(config.sonarrConfig, httpClient))

    private val recommendationTracker = RecommendationTracker(
        popularService,
        moviesRecommendationMapperService,
        tvRecommendationMapperService,
        DBClient,
        config.movieRequirements,
        config.tvRequirements
    )

    suspend fun run(excludeAndUpdatePastRecommendations: Boolean): Boolean = coroutineScope {
        val trackMoviesDeferred = async {
            recommendationTracker.getRecommendedMovies(excludeAndUpdatePastRecommendations)
        }
        val trackTVShowsDeferred = async {
            recommendationTracker.getRecommendedTVShows(excludeAndUpdatePastRecommendations)
        }

        val trackMoviesResponse = trackMoviesDeferred.await()
        val trackTVShowsResponse = trackTVShowsDeferred.await()
        val trackResponses = listOf(trackMoviesResponse, trackTVShowsResponse)

        if (trackResponses.filterIsInstance<Response.Failure<PickarrError>>().onEach {
                it.notifyError(notificationClient)
            }.isNotEmpty()) {
            false
        } else {
            (trackMoviesResponse as? Response.Success)?.let {
                notificationClient.notifyNewMovies(it.body)
            }
            (trackTVShowsResponse as? Response.Success)?.let {
                notificationClient.notifyNewTV(it.body)
            }
            true
        }
    }

    private suspend fun Response<*, PickarrError>.notifyError(notificationClient: NotificationClient) {
        (this as? Response.Failure)?.let {
            notificationClient.notifyTaskError(this.body::class.simpleName, this.body.error)
        }
    }
}