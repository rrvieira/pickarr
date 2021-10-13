package com.rrvieir4.pickarr.services.clients.servarr.radarr

import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.QualityProfile
import com.rrvieir4.pickarr.services.clients.servarr.models.RootFolder
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieAddOptions
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.MovieItem
import com.rrvieir4.pickarr.config.Config.ServarrConfig
import com.rrvieir4.pickarr.services.clients.servarr.ServarrClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class RadarrClient(baseUrl: String, apiKey: String, httpClient: HttpClient) :
    ServarrClient(baseUrl, apiKey, httpClient) {

    suspend fun addMovie(movieItem: MovieItem): Response<MovieItem, ClientError> = post("movie", movieItem)

    suspend fun getExistingMovies(): Response<List<MovieItem>, ClientError> = get("movie")

    suspend fun getRootFolders(): Response<List<RootFolder>, ClientError> = get("rootFolder")

    suspend fun getQualityProfiles(): Response<List<QualityProfile>, ClientError> = get("qualityProfile")

    suspend fun getTags(): Response<List<Tag>, ClientError> = get("tag")

    suspend fun addTag(tagName: String): Response<Tag, ClientError> = post("tag", Tag(tagName))

    suspend fun lookupMovieWithImdbId(imdbId: String): Response<List<MovieItem>, ClientError> {
        return get("movie/lookup") {
            parameter("term", "imdb:$imdbId")
        }
    }
}