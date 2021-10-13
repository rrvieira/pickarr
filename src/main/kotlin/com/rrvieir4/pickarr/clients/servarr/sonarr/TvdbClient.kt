package com.rrvieir4.pickarr.clients.servarr.sonarr

import com.rrvieir4.pickarr.clients.common.ClientError
import com.rrvieir4.pickarr.clients.common.Response
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TvdbClient(private val httpClient: HttpClient) {

    suspend fun getTvdbIdFromImdbId(imdbId: String): Response<String, ClientError> {
        return try {
            val httpResponse: HttpResponse = httpClient.get(TV_DB_GET_IMDB_ID_URL_TEMPLATE.format(imdbId))

            if (httpResponse.status == HttpStatusCode.OK) {
                val tvdbId: String = httpResponse.receive<String>().parseTvdbId()
                Response.Success(tvdbId)
            } else {
                Response.Failure(ClientError.ApiError("Response status code not expected: ${httpResponse.status.value} / ${httpResponse.status.description}"))
            }
        } catch (t: Throwable) {
            Response.Failure(ClientError.ParseError(t.cause?.message))
        }
    }

    private fun String.parseTvdbId(): String =
        Regex("(?<=<seriesid>)\\d+(?=</seriesid>)").find(this)!!.value

    private companion object {
        private const val TV_DB_GET_IMDB_ID_URL_TEMPLATE = "https://thetvdb.com/api/GetSeriesByRemoteID.php?imdbid=%s"
    }
}