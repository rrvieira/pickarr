package com.rrvieir4.pickarr.services.storage.models

import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.popular.PopularItem
import org.bson.codecs.pojo.annotations.BsonId

data class RecommendedItem(
    @BsonId
    val id: String,
    val title: String,
    val year: Int,
    val genres: List<String> = listOf(),
    val from: String = "",
    val link: String = "",
    val rating: Float = 0f,
    val totalVotes: Int = 0,
    val popularityPosition: Int = 0,
)

fun ServarrItem.toRecommendedItem(popularItem: PopularItem? = null): RecommendedItem {
    return popularItem?.let {
        RecommendedItem(imdbId, title, year, genres, from, it.link, it.rating, it.totalVotes, it.popularityPosition)
    } ?: RecommendedItem(imdbId, title, year, genres, from)
}