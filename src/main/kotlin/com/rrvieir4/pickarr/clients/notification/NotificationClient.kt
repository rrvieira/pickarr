package com.rrvieir4.pickarr.clients.notification

import com.rrvieir4.pickarr.clients.common.MediaItem

interface NotificationClient {
    suspend fun notifyNewMovies(mediaItemList : List<MediaItem>)
    suspend fun notifyNewTV(mediaItemList : List<MediaItem>)
}