package com.rrvieir4.pickarr.services.servarr.radarr

import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.radarr.RadarrClient
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieItem
import io.ktor.client.*

class RadarrService(private val config: ServarrConfig, httpClient: HttpClient) {

    private val radarrClient = RadarrClient(config.url, config.apiKey, httpClient)

    fun getWebDetailsUrlForMovie(tmdbId: Int) = MOVIE_WEB_DETAILS_URL_TEMPLATE.format(config.url, tmdbId)

    suspend fun getMovies(): Response<List<MovieItem>, ClientError> = radarrClient.getExistingMovies()

    suspend fun saveMovie(imdbId: String): Response<MovieItem, ClientError> {
        return when (val moviesResponse = radarrClient.getExistingMovies()) {
            is Response.Success -> {
                val existingMovie = moviesResponse.body.find { it.imdbId == imdbId }
                if (existingMovie == null) {
                    addMovie(imdbId)
                } else {
                    Response.Success(existingMovie)
                }
            }
            is Response.Failure -> moviesResponse
        }
    }

    private suspend fun addMovie(imdbId: String): Response<MovieItem, ClientError> {
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
                ?: return Response.Failure(ClientError.GenericError("Quality Profile does not exist: ${config.qualityProfileName}"))

            val movieBody = movieToAddResponse.body.first().copy(
                rootFolderPath = rootFolderResponse.body.first().path,
                qualityProfileId = qualityProfile.id,
                tags = listOf(tagResponse.body.id),
                monitored = true,
                addOptions = MovieAddOptions(true)
            )

            radarrClient.addMovie(movieBody)
        } else {
            Response.Failure(ClientError.ApiError("Preparation to add movie failed"))
        }
    }

    private suspend fun saveTag(tagName: String): Response<Tag, ClientError> {
        return when (val tagsResponse = radarrClient.getTags()) {
            is Response.Success -> {
                val existingTag = tagsResponse.body.find { it.label == tagName }
                if (existingTag == null) {
                    radarrClient.addTag(tagName)
                } else {
                    Response.Success(existingTag)
                }
            }
            is Response.Failure -> tagsResponse
        }
    }


    private companion object {
        const val MOVIE_WEB_DETAILS_URL_TEMPLATE = "%s/movie/%s"
    }
}