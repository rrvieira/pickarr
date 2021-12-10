package com.rrvieir4.pickarr.services.recommendation

import com.rrvieir4.pickarr.services.clients.*
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedDetailsItem
import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedItem
import com.rrvieir4.pickarr.services.recommendation.models.RecommendedItem
import com.rrvieir4.pickarr.services.servarr.ServarrService
import com.rrvieir4.pickarr.services.tmdb.TmdbService
import com.rrvieir4.pickarr.services.tmdb.models.TmdbItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RecommendationItemListProvider(private val tmdbService: TmdbService, private val servarrService: ServarrService<*>) {

    suspend fun getLocalRecommendationItems(): Response<List<IRecommendedItem>, PickarrError> {
        return servarrService.getItems().rewrap { servarrItemList ->
            Response.Success(servarrItemList.mapNotNull { it.toRecommendedItem() })
        }
    }

    suspend fun getRecommendationDetailItems(popularItems: List<PopularItem>): Response<List<IRecommendedDetailsItem>, PickarrError> =
        coroutineScope {
            val imdbIdList = popularItems.map { it.id }
            val tmdbItemsDeferred = async { tmdbService.getItems(imdbIdList) }
            val servarrItemsDeferred = async { servarrService.getItems(imdbIdList) }

            val relevantTmdbItemsMapResponse = tmdbItemsDeferred.await()
            val relevantServarrItemsMapResponse = servarrItemsDeferred.await()

            val relevantTmdbItemsMap = relevantTmdbItemsMapResponse.unwrapSuccess()
                ?: return@coroutineScope relevantTmdbItemsMapResponse as Response.Failure
            val relevantServarrItemsMap = relevantServarrItemsMapResponse.unwrapSuccess()
                ?: return@coroutineScope relevantServarrItemsMapResponse as Response.Failure

            Response.Success(
                buildNewRecommendedItems(popularItems, relevantTmdbItemsMap, relevantServarrItemsMap)
            )
        }

    private fun ServarrItem.toRecommendedItem(): IRecommendedItem? {
        return imdbId?.let {
            RecommendedItem(it, title, overview, year, posterUrl, genres, from)
        }
    }

    private fun buildNewRecommendedItems(
        popularItems: List<PopularItem>,
        tmdbItems: Map<String, TmdbItem>,
        servarrItems: Map<String, ServarrItem>
    ): List<IRecommendedDetailsItem> {
        return popularItems.mapNotNull { popularItem ->
            val id = popularItem.id
            val tmdbItem = tmdbItems[id]
            val servarrItem = servarrItems[id]
            if (tmdbItem != null && servarrItem != null) {
                RecommendedItem(
                    popularItem.id,
                    servarrItem.title,
                    servarrItem.overview,
                    servarrItem.year,
                    servarrItem.posterUrl,
                    servarrItem.genres,
                    servarrItem.from,
                    popularItem.link,
                    popularItem.rating,
                    popularItem.totalVotes,
                    popularItem.popularityPosition,
                    tmdbItem.originalLanguage,
                    tmdbItem.castList
                )
            } else {
                null
            }
        }
    }
}