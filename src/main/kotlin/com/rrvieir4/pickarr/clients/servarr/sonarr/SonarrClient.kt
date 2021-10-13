package com.rrvieir4.pickarr.clients.servarr.sonarr

import com.rrvieir4.pickarr.clients.common.ClientError
import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.servarr.models.QualityProfile
import com.rrvieir4.pickarr.clients.servarr.models.RootFolder
import com.rrvieir4.pickarr.clients.servarr.models.Tag
import com.rrvieir4.pickarr.clients.servarr.sonarr.models.LanguageProfile
import com.rrvieir4.pickarr.clients.servarr.sonarr.models.SeriesType
import com.rrvieir4.pickarr.clients.servarr.sonarr.models.TVAddOptions
import com.rrvieir4.pickarr.clients.servarr.sonarr.models.TVItem
import com.rrvieir4.pickarr.config.Config.ServarrConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SonarrClient(private val config: ServarrConfig, private val httpClient: HttpClient) {

    private val url = "${config.url}${API_URL_PATH}"

    suspend fun getExistingMedia(): Response<List<TVItem>, ClientError> = getRequest("series")

    suspend fun getRootFolders(): Response<List<RootFolder>, ClientError> = getRequest("rootFolder")

    suspend fun getQualityProfiles(): Response<List<QualityProfile>, ClientError> = getRequest("qualityprofile")

    suspend fun getLanguageProfiles(): Response<List<LanguageProfile>, ClientError> = getRequest("languageprofile")

    suspend fun getTags(): Response<List<Tag>, ClientError> = getRequest("tag")

    suspend fun createTag(tagName: String): Response<Tag, ClientError> {
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

    suspend fun lookupSerieWithTvdbId(tvdbId: String): Response<List<TVItem>, ClientError> {
        return getRequest("series/lookup") {
            parameter("term", "tvdb:$tvdbId")
        }
    }

    suspend fun lookupSerieWithImdbId(imdbId: String): Response<List<TVItem>, ClientError> {
        return when (val tvdbIdResponse = TvdbClient(httpClient).getTvdbIdFromImdbId(imdbId)) {
            is Response.Success -> {
                return lookupSerieWithTvdbId(tvdbIdResponse.body)
            }
            is Response.Failure -> tvdbIdResponse
        }
    }

    suspend fun addSerieWithImdbId(imdbId: String): Response<TVItem, ClientError> {
        return when (val seriesResponse = getExistingMedia()) {
            is Response.Success -> {
                val existingSerie = seriesResponse.body.find { it.imdbId == imdbId }
                if (existingSerie == null) {
                    val rootFolderResponse = getRootFolders()
                    val qualityProfileResponse = getQualityProfiles()
                    val languageProfileResponse = getLanguageProfiles()
                    val serieToAddResponse = lookupSerieWithImdbId(imdbId)
                    val tagResponse = createTag(config.tagName)

                    if (rootFolderResponse is Response.Success &&
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

                        postRequest("series", serieBody)
                    } else {
                        Response.Failure(ClientError.ApiError("Preparation to add tv series failed"))
                    }
                } else {
                    Response.Success(existingSerie)
                }
            }
            is Response.Failure -> Response.Failure(ClientError.ApiError("Existing tv series list could not be retrieved"))
        }
    }

    fun getWebDetailsUrlForTVSeries(titleSlug: String) = TV_SERIES_WEB_DETAILS_URL_TEMPLATE.format(config.url, titleSlug)

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
                Response.Failure(ClientError.ApiError("Response status code not expected: ${httpResponse.status.value} / ${httpResponse.status.description}"))
            }
        } catch (t: Throwable) {
            Response.Failure(ClientError.GenericError(t.cause?.message))
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
        const val TV_SERIES_WEB_DETAILS_URL_TEMPLATE = "%s/series/%s"

        const val API_KEY_PARAMETER = "apikey"
    }
}