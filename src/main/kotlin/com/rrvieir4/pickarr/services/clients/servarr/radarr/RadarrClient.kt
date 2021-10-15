package com.rrvieir4.pickarr.services.clients.servarr.radarr

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.QualityProfile
import com.rrvieir4.pickarr.services.clients.servarr.models.RootFolder
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.radarr.models.RadarrItem
import com.rrvieir4.pickarr.services.clients.servarr.ServarrClient
import io.ktor.client.*
import io.ktor.client.request.*

class RadarrClient(baseUrl: String, apiKey: String, httpClient: HttpClient) :
    ServarrClient(baseUrl, apiKey, httpClient) {

    suspend fun addMovie(radarrItem: RadarrItem): Response<RadarrItem, PickarrError> = post("movie", radarrItem)

    suspend fun getExistingMovies(): Response<List<RadarrItem>, PickarrError> = get("movie")

    suspend fun getRootFolders(): Response<List<RootFolder>, PickarrError> = get("rootFolder")

    suspend fun getQualityProfiles(): Response<List<QualityProfile>, PickarrError> = get("qualityProfile")

    suspend fun getTags(): Response<List<Tag>, PickarrError> = get("tag")

    suspend fun addTag(tagName: String): Response<Tag, PickarrError> = post("tag", Tag(tagName))

    suspend fun lookupMovieWithImdbId(imdbId: String): Response<List<RadarrItem>, PickarrError> {
        return get("movie/lookup") {
            parameter("term", "imdb:$imdbId")
        }
    }
}