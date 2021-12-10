package com.rrvieir4.pickarr.services.recommendation

import com.rrvieir4.pickarr.config.Config.MediaRequirements
import com.rrvieir4.pickarr.services.clients.*
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedDetailsItem
import com.rrvieir4.pickarr.services.storage.DBClient

class RecommendationTracker(
    private val popularService: PopularService,
    private val moviesRecommendationMapperService: RecommendationItemListProvider,
    private val tvRecommendationMapperService: RecommendationItemListProvider,
    private val dbClient: DBClient,
    private val movieRequirements: MediaRequirements,
    private val tvShowsRequirements: MediaRequirements
) {
    suspend fun getRecommendedMovies(excludeAndUpdatePastRecommendations: Boolean): Response<List<IRecommendedDetailsItem>, PickarrError> {
        return popularService.fetchPopularMovies().rewrap { popularItems ->
            track(
                popularItems,
                movieRequirements,
                moviesRecommendationMapperService,
                excludeAndUpdatePastRecommendations
            )
        }
    }

    suspend fun getRecommendedTVShows(excludeAndUpdatePastRecommendations: Boolean): Response<List<IRecommendedDetailsItem>, PickarrError> {
        return popularService.fetchPopularTV().rewrap { popularItems ->
            track(popularItems, tvShowsRequirements, tvRecommendationMapperService, excludeAndUpdatePastRecommendations)
        }
    }

    private suspend fun track(
        popularItems: List<PopularItem>,
        mediaRequirements: MediaRequirements,
        recommendationService: RecommendationItemListProvider,
        excludeAndUpdatePastRecommendations: Boolean
    ): Response<List<IRecommendedDetailsItem>, PickarrError> {

        val existingItemsResponse = recommendationService.getLocalRecommendationItems()
        val existingItems = existingItemsResponse.unwrapSuccess() ?: return existingItemsResponse as Response.Failure

        val pastRecommendedItems = if (excludeAndUpdatePastRecommendations) {
            dbClient.updateRecommendedItems(existingItems)
        } else {
            existingItems
        }

        val relevantPopularItems = popularItems.filter { popularItem ->
            popularItem.year >= mediaRequirements.minYear &&
                    popularItem.rating >= mediaRequirements.minRating &&
                    popularItem.totalVotes >= mediaRequirements.minVotes &&
                    pastRecommendedItems.find { suggestedMedia ->
                        popularItem.id == suggestedMedia.id
                    } == null
        }

        val detailsItemsResponse = recommendationService.getRecommendationDetailItems(relevantPopularItems)
        val detailsItems = detailsItemsResponse.unwrapSuccess() ?: return detailsItemsResponse

        if (excludeAndUpdatePastRecommendations) {
            dbClient.updateRecommendedItems(detailsItems)
        }

        val newRecommendedDetailsItemList = detailsItems.filterNot { recommendedItem ->
            mediaRequirements.languageBlacklist.contains(recommendedItem.originalLanguageCode)
        }

        return Response.Success(newRecommendedDetailsItemList)
    }
}