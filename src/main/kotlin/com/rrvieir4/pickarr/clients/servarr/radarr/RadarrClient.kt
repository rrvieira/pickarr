package com.rrvieir4.pickarr.clients.servarr.radarr

import com.rrvieir4.pickarr.clients.common.ClientError
import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.servarr.models.QualityProfile
import com.rrvieir4.pickarr.clients.servarr.models.RootFolder
import com.rrvieir4.pickarr.clients.servarr.models.Tag
import com.rrvieir4.pickarr.clients.servarr.radarr.models.MovieAddOptions
import com.rrvieir4.pickarr.clients.servarr.radarr.models.MovieItem
import com.rrvieir4.pickarr.clients.servarr.sonarr.SonarrClient
import com.rrvieir4.pickarr.config.Config.ServarrConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory

class RadarrClient(private val config: ServarrConfig, private val httpClient: HttpClient) {

    private val url = "${config.url}$API_URL_PATH"

    suspend fun addMovieWithImdbId(imdbId: String): Response<MovieItem, ClientError> {
        return when (val moviesResponse = getExistingMovies()) {
            is Response.Success -> {
                val existingMovie = moviesResponse.body.find { it.imdbId == imdbId }
                if (existingMovie == null) {
                    val rootFolderResponse = getRootFolders()
                    val qualityProfileResponse = getQualityProfiles()
                    val movieToAddResponse = lookupMovieWithImdbId(imdbId)
                    val tagResponse = createTag(config.tagName)

                    if (rootFolderResponse is Response.Success &&
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

                        postRequest("movie", movieBody)
                    } else {
                        Response.Failure(ClientError.ApiError("Preparation to add movie failed"))
                    }
                } else {
                    Response.Success(existingMovie)
                }
            }
            is Response.Failure -> Response.Failure(ClientError.ApiError("Existing movie list could not be retrieved"))
        }
    }

    suspend fun getExistingMovies(): Response<List<MovieItem>, ClientError> = getRequest("movie")

    private suspend fun getRootFolders(): Response<List<RootFolder>, ClientError> = getRequest("rootFolder")

    private suspend fun getQualityProfiles(): Response<List<QualityProfile>, ClientError> = getRequest("qualityProfile")

    private suspend fun getTags(): Response<List<Tag>, ClientError> = getRequest("tag")

    private suspend fun createTag(tagName: String): Response<Tag, ClientError> {
        return when (val tagsResponse = getTags()) {
            is Response.Success -> {
                val existingTag = tagsResponse.body.find { it.label == tagName }
                if (existingTag == null) {
                    postRequest("tag", Tag(tagName))
                } else {
                    Response.Success(existingTag)
                }
            }
            is Response.Failure -> Response.Failure(ClientError.ApiError("Tags could not be updated"))
        }
    }

    private suspend fun lookupMovieWithImdbId(imdbId: String): Response<List<MovieItem>, ClientError> {
        return getRequest("movie/lookup") {
            parameter("term", "imdb:$imdbId")
        }
    }

    fun getWebDetailsUrlForMovie(tmdbId: Int) = MOVIE_WEB_DETAILS_URL_TEMPLATE.format(config.url, tmdbId)

    private suspend inline fun <reified R : Any> getRequest(
        methodName: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Response<R, ClientError> {
        return try {
            val httpResponse: HttpResponse = httpClient.get("${url}${methodName}") {
                contentType(ContentType.Application.Json)
                parameter(API_KEY_PARAMETER, config.apiKey)
                block()
            }

            if (httpResponse.status == HttpStatusCode.OK) {
                Response.Success(httpResponse.receive())
            } else {
                Response.Failure(ClientError.GenericError())
            }
        } catch (t: Throwable) {
            Response.Failure(ClientError.GenericError())
        }
    }

    private suspend inline fun <reified B : Any, reified R : Any> postRequest(
        methodName: String,
        content: B
    ): Response<R, ClientError> {
        return try {
            val httpResponse: HttpResponse = httpClient.post("${url}${methodName}") {
                contentType(ContentType.Application.Json)
                parameter(API_KEY_PARAMETER, config.apiKey)
                body = content
            }

            if (httpResponse.status == HttpStatusCode.Created) {
                Response.Success(httpResponse.receive())
            } else {
                Response.Failure(ClientError.GenericError())
            }
        } catch (t: Throwable) {
            Response.Failure(ClientError.GenericError())
        }
    }

    private companion object {
        const val API_URL_PATH = "/api/v3/"
        const val API_KEY_PARAMETER = "apikey"

        const val MOVIE_WEB_DETAILS_URL_TEMPLATE = "%s/movie/%s"
    }
}