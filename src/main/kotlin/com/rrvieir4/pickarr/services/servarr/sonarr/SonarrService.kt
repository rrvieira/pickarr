package com.rrvieir4.pickarr.services.servarr.sonarr

import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.SeriesType
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.TVAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.TVItem
import com.rrvieir4.pickarr.services.clients.tvdb.TvdbClient
import io.ktor.client.*

class SonarrService(private val config: ServarrConfig, private val httpClient: HttpClient) {

    private val sonarrClient = SonarrClient(config.url, config.apiKey, httpClient)
    private val tvdbClient = TvdbClient(httpClient)

    suspend fun getTVShows(): Response<List<TVItem>, ClientError> = sonarrClient.getExistingTVShows()

    suspend fun saveTV(imdbId: String): Response<TVItem, ClientError> {
        return when (val seriesResponse = sonarrClient.getExistingTVShows()) {
            is Response.Success -> {
                val existingTV = seriesResponse.body.find { it.imdbId == imdbId }
                if (existingTV == null) {
                    addTV(imdbId)
                } else {
                    Response.Success(existingTV)
                }
            }
            is Response.Failure -> Response.Failure(ClientError.ApiError("Existing tv series list could not be retrieved"))
        }
    }

    private suspend fun addTV(imdbId: String): Response<TVItem, ClientError> {
        val rootFolderResponse = sonarrClient.getRootFolders()
        val qualityProfileResponse = sonarrClient.getQualityProfiles()
        val languageProfileResponse = sonarrClient.getLanguageProfiles()
        val serieToAddResponse = lookupSerieWithImdbId(imdbId)
        val tagResponse = saveTag(config.tagName)

        return if (rootFolderResponse is Response.Success &&
            qualityProfileResponse is Response.Success &&
            languageProfileResponse is Response.Success &&
            serieToAddResponse is Response.Success &&
            tagResponse is Response.Success
        ) {
            val qualityProfile = qualityProfileResponse.body.find { it.name == config.qualityProfileName }
                ?: return Response.Failure(ClientError.GenericError("Quality Profile does not exist: ${config.qualityProfileName}"))
            val languageProfile = languageProfileResponse.body.first()

            val tvItem = serieToAddResponse.body.first()
            val serieBody = tvItem.copy(
                seriesType = if (tvItem.hasGenre(SeriesType.anime.name)) SeriesType.anime else SeriesType.standard,
                rootFolderPath = rootFolderResponse.body.first().path,
                qualityProfileId = qualityProfile.id,
                languageProfileId = languageProfile.id,
                seasonFolder = true,
                tags = listOf(tagResponse.body.id),
                monitored = true,
                addOptions = TVAddOptions(true)
            )

            sonarrClient.addTV(serieBody)
        } else {
            Response.Failure(ClientError.ApiError("Preparation to add tv series failed"))
        }
    }

    suspend fun lookupSerieWithImdbId(imdbId: String): Response<List<TVItem>, ClientError> {
        return when (val tvdbIdResponse = tvdbClient.getTvdbId(imdbId)) {
            is Response.Success -> {
                return lookupSerieWithTvdbId(tvdbIdResponse.body)
            }
            is Response.Failure -> tvdbIdResponse
        }
    }

    suspend fun lookupSerieWithTvdbId(tvdbId: String): Response<List<TVItem>, ClientError> =
        sonarrClient.lookupSerieWithTvdbId(tvdbId)

    private suspend fun saveTag(tagName: String): Response<Tag, ClientError> {
        return when (val tagsResponse = sonarrClient.getTags()) {
            is Response.Success -> {
                val existingTag = tagsResponse.body.find { it.label == tagName }
                if (existingTag == null) {
                    sonarrClient.addTag(tagName)
                } else {
                    Response.Success(existingTag)
                }
            }
            is Response.Failure -> tagsResponse
        }
    }


    fun getWebDetailsUrlForTVSeries(titleSlug: String) =
        TV_SERIES_WEB_DETAILS_URL_TEMPLATE.format(config.url, titleSlug)

    private companion object {
        const val API_URL_PATH = "/api/v3/"
        const val TV_SERIES_WEB_DETAILS_URL_TEMPLATE = "%s/series/%s"

        const val API_KEY_PARAMETER = "apikey"
    }
}