package com.rrvieir4.pickarr.services.notification

interface NotificationClient {
    suspend fun notifyNewMovies(recommendedItemList: List<RecommendedItem>): Boolean
    suspend fun notifyNewTV(recommendedItemList: List<RecommendedItem>): Boolean

    suspend fun notifyTaskError(type: String?, error: String?)
}