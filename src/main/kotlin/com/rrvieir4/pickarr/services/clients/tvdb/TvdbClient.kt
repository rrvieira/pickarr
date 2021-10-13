package com.rrvieir4.pickarr.services.clients.tvdb

import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.imdb.ImdbClient
import com.rrvieir4.pickarr.services.clients.pickarrGet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TvdbClient(private val httpClient: HttpClient) {

    suspend fun getTvdbId(imdbId: String): Response<String, ClientError> {
        return try {
            val tvdbResponse: Response<String, ClientError> =
                httpClient.pickarrGet(TV_DB_GET_IMDB_ID_URL_TEMPLATE.format(imdbId))

            when (tvdbResponse) {
                is Response.Failure -> return tvdbResponse
                is Response.Success -> {
                    Response.Success(tvdbResponse.body.parseTvdbId())
                }
            }
        } catch (e: NullPointerException) {
            Response.Failure(ClientError.ParseError("Could not parse tvdb XML response"))
        } catch (t: Throwable) {
            Response.Failure(ClientError.GenericError(t.cause?.message))
        }
    }

    private companion object {
        private const val TV_DB_GET_IMDB_ID_URL_TEMPLATE = "https://thetvdb.com/api/GetSeriesByRemoteID.php?imdbid=%s"
    }
}