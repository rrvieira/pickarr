package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.utils.unwrap
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting(config: Config) {

    routing {
        get("/${config.actionUrlConfig.addMovieMethod}/{imdbId}") {
            val imdbId = call.parameters["imdbId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val httpClient = getHttpClient()
            val radarrService = RadarrService(config.radarrConfig, httpClient)

            radarrService.saveItem(imdbId).unwrap({
                call.respond(HttpStatusCode.InternalServerError, "Internal server error: ${it.message}")
            }, {
                call.respondRedirect(radarrService.getItemDetailWebpageUrl(it), false)
            })

            httpClient.close()
        }

        get("/${config.actionUrlConfig.addTVMethod}/{imdbId}") {
            val imdbId = call.parameters["imdbId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val httpClient = getHttpClient()
            val sonarrService = SonarrService(config.sonarrConfig, httpClient)

            sonarrService.saveItem(imdbId).unwrap({
                call.respond(HttpStatusCode.InternalServerError, "Internal server error: ${it.message}")
            }, {
                call.respondRedirect(sonarrService.getItemDetailWebpageUrl(it), false)
            })

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
