package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.config.Config
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        get("/add-movie/{imdbId}") {
            val imdbId = call.parameters["imdbId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val config = Config.setupFromEnv() ?: return@get call.respond(HttpStatusCode.InternalServerError)

            val httpClient = getHttpClient()
            val radarrClient = RadarrClient(config.radarrConfig, httpClient)

            when (val addMovieResponse = radarrClient.addMovieWithImdbId(imdbId)) {
                is Response.Success -> call.respondRedirect(
                    radarrClient.getWebDetailsUrlForMovie(addMovieResponse.body.tmdbId),
                    false
                )
                is Response.Failure -> call.respond(HttpStatusCode.InternalServerError, "Internal server error")
            }

            httpClient.close()
        }

        get("/add-tv/{imdbId}") {
            val imdbId = call.parameters["imdbId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val config = Config.setupFromEnv() ?: return@get call.respond(HttpStatusCode.InternalServerError)

            val httpClient = getHttpClient()
            val sonarrClient = SonarrClient(config.sonarrConfig, httpClient)

            when (val addTVResponse = sonarrClient.addSerieWithImdbId(imdbId)) {
                is Response.Success -> call.respondRedirect(
                    sonarrClient.getWebDetailsUrlForTVSeries(addTVResponse.body.titleSlug),
                    false
                )
                is Response.Failure -> call.respond(HttpStatusCode.InternalServerError, "Internal server error")
            }

            httpClient.close()
        }
    }
}

private fun getHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
}
