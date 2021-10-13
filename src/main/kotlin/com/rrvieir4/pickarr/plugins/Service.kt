package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.clients.popularmedia.MediaClient
import com.rrvieir4.pickarr.clients.popularmedia.imdb.ImdbClient
import com.rrvieir4.pickarr.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.clients.storage.DBClient
import com.rrvieir4.pickarr.config.Config
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.launch

fun Application.launchPickarrService() {
    log.info("Pickarr background service preparing to launch")

    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    val config = Config.setupFromEnv() ?: return //TODO

    launch {

        val telegramClient = TelegramClient(
            config.telegramConfig.telegramUserToken,
            config.telegramConfig.telegramChatId,
            "http://127.0.0.1:8080"
        )

        val mediaClient: MediaClient = ImdbClient(httpClient)
        val popularMoviesResponse = mediaClient.fetchPopularMovies()
        val popularTVResponse = mediaClient.fetchPopularTV()

        val movieItemsResponse = RadarrClient(config.radarrConfig, httpClient).getExistingMovies()
        if (popularMoviesResponse is Response.Success && movieItemsResponse is Response.Success) {
            val popularMovies = popularMoviesResponse.body
            val existingMovies = movieItemsResponse.body

            val suggestedMediaList = DBClient.updateWithMovieItems(existingMovies)

            val suggestionList = popularMovies.filter { popularMovie ->
                popularMovie.year >= config.movieRequirements.minYear &&
                        popularMovie.rating >= config.movieRequirements.minRating &&
                        popularMovie.totalVotes >= config.movieRequirements.minVotes &&
                        suggestedMediaList.find { suggestedMedia ->
                            popularMovie.imdbId == suggestedMedia.imdbId
                        } == null
            }

            DBClient.updateWithMediaItems(suggestionList)
            //println(suggestionList.sortedDescending().joinToString("\n"))
            telegramClient.notifyNewMovies(suggestionList.sortedDescending())
        }

        val tvItemsResponse = SonarrClient(config.sonarrConfig, httpClient).getExistingMedia()
        if (popularTVResponse is Response.Success && tvItemsResponse is Response.Success) {
            val popularTV = popularTVResponse.body
            val existingTV = tvItemsResponse.body

            val suggestedMediaList = DBClient.updateWithTVItems(existingTV)

            val suggestionList = popularTV.filter { popularTV ->
                popularTV.year >= config.tvRequirements.minYear &&
                        popularTV.rating >= config.tvRequirements.minRating &&
                        popularTV.totalVotes >= config.tvRequirements.minVotes &&
                        suggestedMediaList.find { suggestedMedia ->
                            popularTV.imdbId == suggestedMedia.imdbId
                        } == null
            }

            DBClient.updateWithMediaItems(suggestionList)
            //println(suggestionList.sortedDescending().joinToString("\n"))
            telegramClient.notifyNewTV(suggestionList.sortedDescending())
        }


    }

}
