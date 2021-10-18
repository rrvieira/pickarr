package com.rrvieir4.pickarr.services.storage.models

import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.popular.PopularItem
import org.bson.codecs.pojo.annotations.BsonId

data class RecommendedItem(
    @BsonId
    val id: String,
    val title: String,
    val overview: String,
    val year: Int,
    val posterUrl: String,
    val genres: List<String>,
    val from: String,
    val link: String = "",
    val rating: Float = 0f,
    val totalVotes: Int = 0,
    val popularityPosition: Int = 0,
): Comparable<RecommendedItem> {
    override fun compareTo(other: RecommendedItem): Int {
        return if (rating == other.rating && totalVotes == other.totalVotes) {
            other.popularityPosition.compareTo(popularityPosition)
        } else if (rating == other.rating) {
            totalVotes.compareTo(other.totalVotes)
        } else {
            rating.compareTo(other.rating)
        }
    }
}

fun ServarrItem.toRecommendedItem(popularItem: PopularItem? = null): RecommendedItem {
    return popularItem?.let {
        RecommendedItem(
            imdbId,
            title,
            overview,
            year,
            posterUrl,
            genres,
            from,
            it.link,
            it.rating,
            it.totalVotes,
            it.popularityPosition
        )
    } ?: RecommendedItem(imdbId, title, overview, year, posterUrl, genres, from)
}