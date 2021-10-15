package com.rrvieir4.pickarr.services.servarr.radarr

import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.RadarrItem
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
        val rootFolderResponse = radarrClient.getRootFolders()
        val qualityProfileResponse = radarrClient.getQualityProfiles()
        val movieToAddResponse = radarrClient.lookupMovieWithImdbId(imdbId)
        val tagResponse = saveTag(config.tagName)

        return if (rootFolderResponse is Response.Success &&
            qualityProfileResponse is Response.Success &&
            movieToAddResponse is Response.Success &&
            tagResponse is Response.Success
        ) {
            val qualityProfile = qualityProfileResponse.body.find { it.name == config.qualityProfileName }
                ?: return Response.Failure(PickarrError.GenericError("Quality Profile does not exist: ${config.qualityProfileName}"))

            val movieBody = movieToAddResponse.body.first().copy(
                rootFolderPath = rootFolderResponse.body.first().path,
                qualityProfileId = qualityProfile.id,
                tags = listOf(tagResponse.body.id),
                monitored = true,
                addOptions = MovieAddOptions(true)
            )

            radarrClient.addMovie(movieBody)
        } else {
            Response.Failure(PickarrError.ApiError("Preparation to add movie failed"))
        }
    }

    override suspend fun lookupItemWithImdbId(imdbId: String): Response<List<RadarrItem>, PickarrError> =
        radarrClient.lookupMovieWithImdbId(imdbId)

    private companion object {
        const val MOVIE_WEB_DETAILS_URL_TEMPLATE = "%s/movie/%s"
    }
}