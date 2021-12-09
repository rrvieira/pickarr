package com.rrvieir4.pickarr.task

import com.rrvieir4.pickarr.config.Config.MediaRequirements
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.rewrap
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.clients.unwrapSuccess
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.reccommend.TmdbService
import com.rrvieir4.pickarr.services.servarr.ServarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.services.notification.RecommendedItem
import com.rrvieir4.pickarr.services.reccommend.models.TmdbItem
import com.rrvieir4.pickarr.services.utils.toRecommendedItem

class PickarrTask(
    private val popularService: PopularService,
    private val moviesService: ServarrService<*>,
    private val tvShowsService: ServarrService<*>,
    private val tmdbService: TmdbService,
    private val dbClient: DBClient,
    private val movieRequirements: MediaRequirements,
    private val tvShowsRequirements: MediaRequirements
) {
    suspend fun getRecommendedMovies(): Response<List<RecommendedItem>, PickarrError> {
        return popularService.fetchPopularMovies().rewrap { popularItems ->
            track(popularItems, movieRequirements, moviesService)
        }
    }

    suspend fun getRecommendedTVShows(): Response<List<RecommendedItem>, PickarrError> {
        return popularService.fetchPopularTV().rewrap { popularItems ->
            track(popularItems, tvShowsRequirements, tvShowsService)
        }
    }

    private suspend fun track(
        popularItems: List<PopularItem>,
        mediaRequirements: MediaRequirements,
        servarrService: ServarrService<*>
    ): Response<List<RecommendedItem>, PickarrError> {

        return servarrService.getItems().rewrap { servarrItemList ->

            val pastRecommendedItems =
                dbClient.updateRecommendedItems(servarrItemList.mapNotNull { it.toRecommendedItem() })

            val relevantPopularItems = popularItems.filter { popularItem ->
                popularItem.year >= mediaRequirements.minYear &&
                        popularItem.rating >= mediaRequirements.minRating &&
                        popularItem.totalVotes >= mediaRequirements.minVotes &&
                        pastRecommendedItems.find { suggestedMedia ->
                            popularItem.id == suggestedMedia.id
                        } == null
            }

            val relevantImdbIds = relevantPopularItems.map { it.id }
            val relevantTmdbItemsMap = tmdbService.getItems(relevantImdbIds).unwrapSuccess()
            val relevantServarrItemsMap = servarrService.getItems(relevantImdbIds).unwrapSuccess()

            if (relevantImdbIds.isNotEmpty() && (relevantTmdbItemsMap.isNullOrEmpty() || relevantServarrItemsMap.isNullOrEmpty())) {
                Response.Failure(
                    PickarrError.ApiError(
                        "${this::class.simpleName}: " +
                                "TmdbItemsMap is null: ${relevantTmdbItemsMap.isNullOrEmpty()} | " +
                                "ServarrItemsMap is null: ${relevantServarrItemsMap.isNullOrEmpty()}"
                    )
                )
            } else {
                val newRecommendedItems =
                    buildNewRecommendedItems(popularItems, relevantTmdbItemsMap!!, relevantServarrItemsMap!!)
                dbClient.updateRecommendedItems(newRecommendedItems)

                val relevantItems = newRecommendedItems.filterNot { recommendedItem ->
                    mediaRequirements.languageBlacklist.contains(recommendedItem.originalLanguageCode)
                }

                Response.Success(relevantItems)
            }
        }
    }

    private fun buildNewRecommendedItems(
        popularItems: List<PopularItem>,
        tmdbItems: Map<String, TmdbItem>,
        servarrItems: Map<String, ServarrItem>
    ): List<RecommendedItem> {
        return popularItems.mapNotNull { popularItem ->
            val id = popularItem.id
            val tmdbItem = tmdbItems[id]
            val servarrItem = servarrItems[id]
            if (tmdbItem != null && servarrItem != null) {
                RecommendedItem(popularItem, tmdbItem, servarrItem)
            } else {
                null
            }
        }
    }
}