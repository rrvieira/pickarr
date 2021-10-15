package com.rrvieir4.pickarr.services.clients.tvdb

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.pickarrGet
import io.ktor.client.*

class TvdbClient(private val httpClient: HttpClient) {

    suspend fun getTvdbId(imdbId: String): Response<String, PickarrError> {
        return try {
            val tvdbResponse: Response<String, PickarrError> =
                httpClient.pickarrGet(TV_DB_GET_IMDB_ID_URL_TEMPLATE.format(imdbId))

            when (tvdbResponse) {
                is Response.Failure -> return tvdbResponse
                is Response.Success -> {
                    Response.Success(tvdbResponse.body.parseTvdbId())
                }
            }
        } catch (e: NullPointerException) {
            Response.Failure(PickarrError.ParseError("Could not parse tvdb XML response"))
        } catch (t: Throwable) {
            Response.Failure(PickarrError.GenericError(t.cause?.message))
        }
    }

    private companion object {
        private const val TV_DB_GET_IMDB_ID_URL_TEMPLATE = "https://thetvdb.com/api/GetSeriesByRemoteID.php?imdbid=%s"
    }
}