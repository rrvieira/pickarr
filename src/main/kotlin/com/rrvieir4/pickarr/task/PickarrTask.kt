package com.rrvieir4.pickarr.task

import com.rrvieir4.pickarr.config.Config.MediaRequirements
import com.rrvieir4.pickarr.services.clients.*
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.recommendation.RecommendationService
import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedDetailsItem
import com.rrvieir4.pickarr.services.storage.DBClient

class PickarrTask(
    private val popularService: PopularService,
    private val moviesRecommendationService: RecommendationService,
    private val tvRecommendationService: RecommendationService,
    private val dbClient: DBClient,
    private val movieRequirements: MediaRequirements,
    private val tvShowsRequirements: MediaRequirements
) {
    suspend fun getRecommendedMovies(): Response<List<IRecommendedDetailsItem>, PickarrError> {
        return popularService.fetchPopularMovies().rewrap { popularItems ->
            track(popularItems, movieRequirements, moviesRecommendationService)
        }
    }

    suspend fun getRecommendedTVShows(): Response<List<IRecommendedDetailsItem>, PickarrError> {
        return popularService.fetchPopularTV().rewrap { popularItems ->
            track(popularItems, tvShowsRequirements, tvRecommendationService)
        }
    }

    private suspend fun track(
        popularItems: List<PopularItem>,
        mediaRequirements: MediaRequirements,
        recommendationService: RecommendationService
    ): Response<List<IRecommendedDetailsItem>, PickarrError> {

        val existingItemsResponse = recommendationService.getExistingItems()
        val existingItems = existingItemsResponse.unwrapSuccess() ?: return existingItemsResponse as Response.Failure

        val pastRecommendedItems = dbClient.updateRecommendedItems(existingItems)

        val relevantPopularItems = popularItems.filter { popularItem ->
            popularItem.year >= mediaRequirements.minYear &&
                    popularItem.rating >= mediaRequirements.minRating &&
                    popularItem.totalVotes >= mediaRequirements.minVotes &&
                    pastRecommendedItems.find { suggestedMedia ->
                        popularItem.id == suggestedMedia.id
                    } == null
        }

        val detailsItemsResponse = recommendationService.getDetailsItems(relevantPopularItems)
        val detailsItems = detailsItemsResponse.unwrapSuccess() ?: return detailsItemsResponse

        dbClient.updateRecommendedItems(detailsItems)

        val newRecommendedDetailsItemList = detailsItems.filterNot { recommendedItem ->
            mediaRequirements.languageBlacklist.contains(recommendedItem.originalLanguageCode)
        }

        return Response.Success(newRecommendedDetailsItemList)
    }
}