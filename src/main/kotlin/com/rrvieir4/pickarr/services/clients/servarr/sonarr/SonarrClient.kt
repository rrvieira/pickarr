package com.rrvieir4.pickarr.services.clients.servarr.sonarr

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.ServarrClient
import com.rrvieir4.pickarr.services.clients.servarr.models.QualityProfile
import com.rrvieir4.pickarr.services.clients.servarr.models.RootFolder
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.LanguageProfile
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.SonarrItem
import io.ktor.client.*
import io.ktor.client.request.*

class SonarrClient(baseUrl: String, apiKey: String, httpClient: HttpClient) :
    ServarrClient(baseUrl, apiKey, httpClient) {

    suspend fun getExistingTVShows(): Response<List<SonarrItem>, PickarrError> = get("series")

    suspend fun getRootFolders(): Response<List<RootFolder>, PickarrError> = get("rootFolder")

    suspend fun getQualityProfiles(): Response<List<QualityProfile>, PickarrError> = get("qualityprofile")

    suspend fun getLanguageProfiles(): Response<List<LanguageProfile>, PickarrError> = get("languageprofile")

    suspend fun getTags(): Response<List<Tag>, PickarrError> = get("tag")

    suspend fun addTag(tagName: String): Response<Tag, PickarrError> = post("tag", Tag(tagName))

    suspend fun lookupSerieWithTvdbId(tvdbId: String): Response<List<SonarrItem>, PickarrError> {
        return get("series/lookup") {
            parameter("term", "tvdb:$tvdbId")
        }
    }

    /*TODO
    suspend fun lookupSerieWithImdbId(imdbId: String): Response<List<TVItem>, ClientError> {
        return when (val tvdbIdResponse = TvdbClient(httpClient).getTvdbIdFromImdbId(imdbId)) {
            is Response.Success -> {
                return lookupSerieWithTvdbId(tvdbIdResponse.body)
            }
            is Response.Failure -> tvdbIdResponse
        }
    }*/

    suspend fun addTV(sonarrItem: SonarrItem): Response<SonarrItem, PickarrError> = post("series", sonarrItem)
}