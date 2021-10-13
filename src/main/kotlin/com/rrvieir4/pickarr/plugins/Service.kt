package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
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

        val popularService: PopularService = ImdbService(httpClient)
        val popularMoviesResponse = popularService.fetchPopularMovies()
        val popularTVResponse = popularService.fetchPopularTV()

        val movieItemsResponse = RadarrService(config.radarrConfig, httpClient).getMovies()
        if (popularMoviesResponse is Response.Success && movieItemsResponse is Response.Success) {
            val popularMovies = popularMoviesResponse.body
            val existingMovies = movieItemsResponse.body

            val suggestedMediaList = DBClient.updateWithMovieItems(existingMovies)

            val suggestionList = popularMovies.filter { popularMovie ->
                popularMovie.year >= config.movieRequirements.minYear &&
                        popularMovie.rating >= config.movieRequirements.minRating &&
                        popularMovie.totalVotes >= config.movieRequirements.minVotes &&
                        suggestedMediaList.find { suggestedMedia ->
                            popularMovie.id == suggestedMedia.imdbId
                        } == null
            }

            DBClient.updateWithMediaItems(suggestionList)
            //println(suggestionList.sortedDescending().joinToString("\n"))
            telegramClient.notifyNewMovies(suggestionList.sortedDescending())
        }

        val tvItemsResponse = SonarrService(config.sonarrConfig, httpClient).getTVShows()
        if (popularTVResponse is Response.Success && tvItemsResponse is Response.Success) {
            val popularTV = popularTVResponse.body
            val existingTV = tvItemsResponse.body

            val suggestedMediaList = DBClient.updateWithTVItems(existingTV)

            val suggestionList = popularTV.filter { popularTV ->
                popularTV.year >= config.tvRequirements.minYear &&
                        popularTV.rating >= config.tvRequirements.minRating &&
                        popularTV.totalVotes >= config.tvRequirements.minVotes &&
                        suggestedMediaList.find { suggestedMedia ->
                            popularTV.id == suggestedMedia.imdbId
                        } == null
            }

            DBClient.updateWithMediaItems(suggestionList)
            //println(suggestionList.sortedDescending().joinToString("\n"))
            telegramClient.notifyNewTV(suggestionList.sortedDescending())
        }


    }

}
