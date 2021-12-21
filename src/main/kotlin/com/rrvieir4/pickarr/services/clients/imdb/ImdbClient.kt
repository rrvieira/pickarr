package com.rrvieir4.pickarr.services.clients.imdb

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.pickarrGet
import com.rrvieir4.pickarr.services.utils.rewrap
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class ImdbClient(private val httpClient: HttpClient) {

    suspend fun getPopularMovies(): Response<List<ImdbItem>, PickarrError> {
        return getPopular(POPULAR_MOVIES_URL)
    }

    suspend fun getPopularTV(): Response<List<ImdbItem>, PickarrError> {
        return getPopular(POPULAR_TV_URL)
    }

    private suspend fun getPopular(imdbUrl: String): Response<List<ImdbItem>, PickarrError> {
        return getPopularMediaHtml(imdbUrl).rewrap { htmlResponse ->
            htmlResponse.parseImdbMediaList(IMDB_URL)?.let {
                Response.Success(it)
            } ?: Response.Failure(PickarrError.ParseError("Could not parse imdb html response"))
        }
    }

    private suspend fun getPopularMediaHtml(url: String): Response<String, PickarrError> {
        return httpClient.pickarrGet(url) {
            headers {
                accept(ContentType.Text.Html)
                userAgent(USER_AGENT)
                header(HttpHeaders.AcceptLanguage, HEADER_ACCEPT_LANGUAGE)
            }
        }
    }

    private companion object {
        private const val IMDB_URL = "https://www.imdb.com"
        private const val POPULAR_TV_URL = "$IMDB_URL/chart/tvmeter"
        private const val POPULAR_MOVIES_URL = "$IMDB_URL/chart/moviemeter"

        private const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:92.0) Gecko/20100101 Firefox/92.0"
        private const val HEADER_ACCEPT_LANGUAGE = "en-US"
    }

}