package com.rrvieir4.pickarr.services.servarr.radarr

import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.RadarrItem
import com.rrvieir4.pickarr.services.clients.unwrapSuccess
import com.rrvieir4.pickarr.services.servarr.ServarrService
import io.ktor.client.*

class RadarrService(private val config: ServarrConfig, httpClient: HttpClient) : ServarrService<RadarrItem> {

    private val radarrClient = RadarrClient(config.url, config.apiKey, httpClient)

    override fun getItemDetailWebpageUrl(item: RadarrItem): String =
        MOVIE_WEB_DETAILS_URL_TEMPLATE.format(config.url, item.tmdbId)

    override suspend fun getItems(): Response<List<RadarrItem>, PickarrError> = radarrClient.getExistingMovies()

    override suspend fun getTags(): Response<List<Tag>, PickarrError> = radarrClient.getTags()

    override suspend fun addTag(tagName: String): Response<Tag, PickarrError> = radarrClient.addTag(tagName)

    override suspend fun addItem(imdbId: String): Response<RadarrItem, PickarrError> {
        val rootFolder =
            radarrClient.getRootFolders().unwrapSuccess()?.firstOrNull() ?: return apiError("no root folder")
        val qualityProfile =
            radarrClient.getQualityProfiles().unwrapSuccess()?.find { it.name == config.qualityProfileName }
                ?: return apiError("invalid quality profile")
        val movieToAdd = radarrClient.lookupMovieWithImdbId(imdbId).unwrapSuccess()?.firstOrNull()
            ?: return apiError("no movie to add")
        val tag = saveTag(config.tagName).unwrapSuccess() ?: return apiError("no tag")

        return radarrClient.addMovie(
            movieToAdd.copy(
                rootFolderPath = rootFolder.path,
                qualityProfileId = qualityProfile.id,
                tags = listOf(tag.id),
                monitored = true,
                addOptions = MovieAddOptions(true)
            )
        )
    }

    override suspend fun lookupItemWithImdbId(imdbId: String): Response<List<RadarrItem>, PickarrError> =
        radarrClient.lookupMovieWithImdbId(imdbId)

    private companion object {
        const val MOVIE_WEB_DETAILS_URL_TEMPLATE = "%s/movie/%s"
    }
}