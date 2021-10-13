package com.rrvieir4.pickarr.services.notification

import com.rrvieir4.pickarr.services.popular.PopularItem

interface NotificationClient {
    suspend fun notifyNewMovies(popularItemList : List<PopularItem>)
    suspend fun notifyNewTV(popularItemList : List<PopularItem>)
}