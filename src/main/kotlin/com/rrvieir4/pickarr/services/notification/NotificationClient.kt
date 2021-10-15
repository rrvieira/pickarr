package com.rrvieir4.pickarr.services.notification

import com.rrvieir4.pickarr.services.storage.models.RecommendedItem

interface NotificationClient {
    suspend fun notifyNewMovies(recommendedItemList: List<RecommendedItem>): Boolean
    suspend fun notifyNewTV(recommendedItemList: List<RecommendedItem>): Boolean

    suspend fun notifyTaskError(type: String?, error: String?)
}