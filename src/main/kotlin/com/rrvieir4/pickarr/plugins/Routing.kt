package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
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
            val radarrService = RadarrService(config.radarrConfig, httpClient)

            when (val addMovieResponse = radarrService.saveItem(imdbId)) {
                is Response.Success -> call.respondRedirect(
                    radarrService.getItemDetailWebpageUrl(addMovieResponse.body),
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
            val sonarrService = SonarrService(config.sonarrConfig, httpClient)

            when (val addTVResponse = sonarrService.saveItem(imdbId)) {
                is Response.Success -> call.respondRedirect(
                    sonarrService.getItemDetailWebpageUrl(addTVResponse.body),
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
