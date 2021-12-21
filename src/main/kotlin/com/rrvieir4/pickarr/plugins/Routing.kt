package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.servarr.ServarrService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.utils.unwrap
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject

fun Application.setupRouting() {
    val config by inject<Config>()
    val radarrService by inject<RadarrService>()
    val sonarrService by inject<SonarrService>()

    routing {
        get("/${config.actionUrlConfig.addMovieMethod}/{imdbId}") {
            saveItem(radarrService)
        }

        get("/${config.actionUrlConfig.addTVMethod}/{imdbId}") {
            saveItem(sonarrService)
        }
    }
}

private suspend fun <I : ServarrItem> PipelineContext<*, ApplicationCall>.saveItem(servarrService: ServarrService<I>
) {
    val imdbId = call.parameters["imdbId"] ?: return call.respond(HttpStatusCode.BadRequest)

    servarrService.saveItem(imdbId).unwrap({
        call.respond(HttpStatusCode.InternalServerError, "Internal server error: ${it.message}")
    }, {
        call.respondRedirect(servarrService.getItemDetailWebpageUrl(it), false)
    })
}

