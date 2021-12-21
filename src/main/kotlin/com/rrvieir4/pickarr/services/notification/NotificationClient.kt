package com.rrvieir4.pickarr.services.notification

import com.rrvieir4.pickarr.services.recommendation.models.RecommendedDetailsItem

interface NotificationClient {
    suspend fun notifyNewMovies(recommendedItemList: List<RecommendedDetailsItem>): Boolean
    suspend fun notifyNewTV(recommendedItemList: List<RecommendedDetailsItem>): Boolean

    suspend fun notifyTaskError(type: String?, error: String?)
}