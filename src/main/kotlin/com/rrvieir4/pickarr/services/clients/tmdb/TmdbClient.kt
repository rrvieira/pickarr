package com.rrvieir4.pickarr.services.clients.tmdb

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.pickarrGet
import com.rrvieir4.pickarr.services.clients.tmdb.models.Credits
import com.rrvieir4.pickarr.services.clients.tmdb.models.FindResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class TmdbClient(private val apiKey: String, private val httpClient: HttpClient) {

    suspend fun findMedia(imdbId: String): Response<FindResponse, PickarrError> {
        return httpClient.pickarrGet("${API_URL}find/${imdbId}") {
            contentType(ContentType.Application.Json)
            parameter(API_KEY_PARAMETER, apiKey)
            parameter("external_source", "imdb_id")
        }
    }

    suspend fun getMovieCredits(tmdbId: String) = getCredits("movie", tmdbId)

    suspend fun getTVCredits(tmdbId: String) = getCredits("tv", tmdbId)

    private suspend fun getCredits(baseMethod: String, tmdbId: String): Response<Credits, PickarrError> {
        return httpClient.pickarrGet("${API_URL}${baseMethod}/${tmdbId}/credits") {
            contentType(ContentType.Application.Json)
            parameter(API_KEY_PARAMETER, apiKey)
        }
    }

    private companion object {
        const val API_URL = "https://api.themoviedb.org/3/"
        const val API_KEY_PARAMETER = "api_key"
    }
}