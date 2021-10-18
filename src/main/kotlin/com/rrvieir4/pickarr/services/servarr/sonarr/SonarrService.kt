package com.rrvieir4.pickarr.services.servarr.sonarr

import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.SeriesType
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.TVAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.SonarrItem
import com.rrvieir4.pickarr.services.clients.tvdb.TvdbClient
import com.rrvieir4.pickarr.services.clients.unwrapSuccess
import com.rrvieir4.pickarr.services.servarr.ServarrService
import io.ktor.client.*

class SonarrService(private val config: ServarrConfig, private val httpClient: HttpClient) :
    ServarrService<SonarrItem> {

    private val sonarrClient = SonarrClient(config.url, config.apiKey, httpClient)
    private val tvdbClient = TvdbClient(httpClient)

    override fun getItemDetailWebpageUrl(item: SonarrItem): String =
        TV_SERIES_WEB_DETAILS_URL_TEMPLATE.format(config.url, item.titleSlug)

    override suspend fun getTags(): Response<List<Tag>, PickarrError> = sonarrClient.getTags()

    override suspend fun addTag(tagName: String): Response<Tag, PickarrError> = sonarrClient.addTag(tagName)

    override suspend fun getItems(): Response<List<SonarrItem>, PickarrError> = sonarrClient.getExistingTVShows()

    override suspend fun addItem(imdbId: String): Response<SonarrItem, PickarrError> {
        val rootFolder =
            sonarrClient.getRootFolders().unwrapSuccess()?.firstOrNull() ?: return apiError("no root folder")
        val qualityProfile =
            sonarrClient.getQualityProfiles().unwrapSuccess()?.find { it.name == config.qualityProfileName }
                ?: return apiError("invalid quality profile")
        val languageProfile =
            sonarrClient.getLanguageProfiles().unwrapSuccess()?.firstOrNull() ?: return apiError("no language profile")
        val serieToAdd =
            lookupItemWithImdbId(imdbId).unwrapSuccess()?.firstOrNull() ?: return apiError("no tv show to add")
        val tag = saveTag(config.tagName).unwrapSuccess() ?: return apiError("invalid tag")

        return sonarrClient.addTV(
            serieToAdd.copy(
                seriesType = if (serieToAdd.hasGenre(SeriesType.anime.name)) SeriesType.anime else SeriesType.standard,
                rootFolderPath = rootFolder.path,
                qualityProfileId = qualityProfile.id,
                languageProfileId = languageProfile.id,
                seasonFolder = true,
                tags = listOf(tag.id),
                monitored = true,
                addOptions = TVAddOptions(true)
            )
        )
    }

    override suspend fun lookupItemWithImdbId(imdbId: String): Response<List<SonarrItem>, PickarrError> {
        return when (val tvdbIdResponse = tvdbClient.getTvdbId(imdbId)) {
            is Response.Success -> {
                return sonarrClient.lookupSerieWithTvdbId(tvdbIdResponse.body)
            }
            is Response.Failure -> tvdbIdResponse
        }
    }

    private companion object {
        const val API_URL_PATH = "/api/v3/"
        const val TV_SERIES_WEB_DETAILS_URL_TEMPLATE = "%s/series/%s"

        const val API_KEY_PARAMETER = "apikey"
    }
}