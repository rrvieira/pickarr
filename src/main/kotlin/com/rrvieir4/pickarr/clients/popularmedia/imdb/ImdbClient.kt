package com.rrvieir4.pickarr.clients.popularmedia.imdb

import com.rrvieir4.pickarr.clients.common.ClientError
import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.popularmedia.MediaClient
import com.rrvieir4.pickarr.clients.common.MediaItem

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory


class ImdbClient(private val httpClient: HttpClient) : MediaClient {

    private val log = LoggerFactory.getLogger(ImdbClient::class.java)

    override suspend fun fetchPopularMovies(): Response<List<MediaItem>, ClientError> {
        log.info("Fetch popular movies trigger")
        return fetchPopular(POPULAR_MOVIES_URL)
    }

    override suspend fun fetchPopularTV(): Response<List<MediaItem>, ClientError> {
        log.info("Fetch popular tv trigger")
        return fetchPopular(POPULAR_TV_URL)
    }

    private suspend fun fetchPopular(imdbUrl: String): Response<List<MediaItem>, ClientError> {
        val htmlResponse = getPopularMediaHtml(imdbUrl) ?: return Response.Failure(ClientError.ApiError())
        val mediaList =
            htmlResponse.parseImdbMediaList(IMDB_URL, log) ?: return Response.Failure(ClientError.ParseError())
        return Response.Success(mediaList)
    }

    private suspend fun getPopularMediaHtml(url: String): String? {
        return try {
            val httpResponse: HttpResponse = httpClient.get(url) {
                headers {
                    append(HttpHeaders.Accept, ContentType.Text.Html)
                    append(HttpHeaders.AcceptLanguage, HEADER_ACCEPT_LANGUAGE)
                    append(HttpHeaders.UserAgent, USER_AGENT)
                }
            }

            if (httpResponse.status == HttpStatusCode.OK) {
                log.info("Fetch popular successful")
                httpResponse.receive()
            } else {
                log.error("Fetch popular error: ${httpResponse.status}")
                null
            }
        } catch (t: Throwable) {
            log.error("Fetch popular error: ${t.cause}")
            null
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