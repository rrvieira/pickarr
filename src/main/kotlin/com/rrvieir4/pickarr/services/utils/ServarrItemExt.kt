package com.rrvieir4.pickarr.services.utils

import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.notification.RecommendedItem

fun ServarrItem.toRecommendedItem(): RecommendedItem? {
    return imdbId?.let {
        RecommendedItem(it, title, overview, year, posterUrl, genres, from)
    }
}