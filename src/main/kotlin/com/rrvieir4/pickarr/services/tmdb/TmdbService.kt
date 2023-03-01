package com.rrvieir4.pickarr.services.tmdb

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.tmdb.TmdbClient
import com.rrvieir4.pickarr.services.tmdb.models.TmdbItem
import com.rrvieir4.pickarr.services.utils.unwrapSuccess
import io.ktor.client.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TmdbService(
    config: Config.TmdbConfig,
    httpClient: HttpClient
) {

    private val tmdbClient = TmdbClient(config.apiKey, httpClient)

    suspend fun getItems(imdbIdList: List<String>): Response<Map<String, TmdbItem>, PickarrError> =
        coroutineScope {

            val deferredList = mutableListOf<Deferred<TmdbItem?>>()
            imdbIdList.forEach { imdbId ->
                deferredList.add(
                    async {
                        val tmdbFindResponse = tmdbClient.findMedia(imdbId).unwrapSuccess()
                        val tmdbMedia = tmdbFindResponse?.results?.firstOrNull()
                        val tmdbId = tmdbMedia?.id
                        val tmdbCredits = tmdbId?.let { id ->
                            if (tmdbFindResponse.isMovie) {
                                tmdbClient.getMovieCredits(id)
                            } else {
                                tmdbClient.getTVCredits(id)
                            }.unwrapSuccess()
                        }
                        if (tmdbMedia?.id != null && tmdbMedia.originalLanguage != null && tmdbCredits?.castNameList != null) {
                            TmdbItem(tmdbMedia.id, imdbId, tmdbMedia.originalLanguage, tmdbCredits.castNameList)
                        } else {
                            null
                        }
                    }
                )
            }

            val itemsMap = mutableMapOf<String, TmdbItem>()
            deferredList.forEach { deferred ->
                val tmdbItem = deferred.await()
                tmdbItem?.let {
                    itemsMap[tmdbItem.imdbId] = tmdbItem
                }
            }

            Response.Success(itemsMap)
        }
}
