package com.rrvieir4.pickarr.task

import com.rrvieir4.pickarr.config.Config.MediaRequirements
import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.onSuccess
import com.rrvieir4.pickarr.services.clients.success
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.servarr.ServarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.services.storage.models.RecommendedItem
import com.rrvieir4.pickarr.services.storage.models.toRecommendedItem

class PickarrTask(
    private val popularService: PopularService,
    private val moviesService: ServarrService<*>,
    private val tvShowsService: ServarrService<*>,
    private val dbClient: DBClient,
    private val movieRequirements: MediaRequirements,
    private val tvShowsRequirements: MediaRequirements
) {


    suspend fun getRecommendedMovies(): Response<List<RecommendedItem>, PickarrError> {
        return popularService.fetchPopularMovies().onSuccess { popularItems ->
            track(popularItems, movieRequirements, moviesService)
        }
    }

    suspend fun getRecommendedTVShows(): Response<List<RecommendedItem>, PickarrError> {
        return popularService.fetchPopularTV().onSuccess { popularItems ->
            track(popularItems, tvShowsRequirements, tvShowsService)
        }
    }

    private suspend fun track(
        popularItems: List<PopularItem>,
        mediaRequirements: MediaRequirements,
        servarrService: ServarrService<*>
    ): Response<List<RecommendedItem>, PickarrError> {

        return servarrService.getItems().onSuccess { servarrItemList ->

            val pastRecommendedItems =
                dbClient.updateRecommendedItems(servarrItemList.map { it.toRecommendedItem() })

            val relevantPopularItems = popularItems.filter { popularItem ->
                popularItem.year >= mediaRequirements.minYear &&
                        popularItem.rating >= mediaRequirements.minRating &&
                        popularItem.totalVotes >= mediaRequirements.minVotes &&
                        pastRecommendedItems.find { suggestedMedia ->
                            popularItem.id == suggestedMedia.id
                        } == null
            }

            servarrService.getItems(relevantPopularItems.map { it.id }).onSuccess { lookupItemList ->
                success(
                    lookupItemList.zip(relevantPopularItems) { servarrItem, popularItem ->
                        servarrItem.toRecommendedItem(popularItem)
                    }.sortedDescending()
                )
            }
        }
    }
}