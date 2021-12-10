package com.rrvieir4.pickarr.services.notification

import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedDetailsItem

interface NotificationClient {
    suspend fun notifyNewMovies(recommendedItemList: List<IRecommendedDetailsItem>): Boolean
    suspend fun notifyNewTV(recommendedItemList: List<IRecommendedDetailsItem>): Boolean

    suspend fun notifyTaskError(type: String?, error: String?)
}