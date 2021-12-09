package com.rrvieir4.pickarr.services.notification

import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.reccommend.models.TmdbItem
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
    val originalLanguageCode: String = "",
    val castList: List<String> = emptyList()
) : Comparable<RecommendedItem> {
    override fun compareTo(other: RecommendedItem): Int {
        return if (rating == other.rating && totalVotes == other.totalVotes) {
            other.popularityPosition.compareTo(popularityPosition)
        } else if (rating == other.rating) {
            totalVotes.compareTo(other.totalVotes)
        } else {
            rating.compareTo(other.rating)
        }
    }

    constructor(popularItem: PopularItem, tmdbItem: TmdbItem, servarrItem: ServarrItem) : this(
        popularItem.id,
        servarrItem.title,
        servarrItem.overview,
        servarrItem.year,
        servarrItem.posterUrl,
        servarrItem.genres,
        servarrItem.from,
        popularItem.link,
        popularItem.rating,
        popularItem.totalVotes,
        popularItem.popularityPosition,
        tmdbItem.originalLanguage,
        tmdbItem.castList
    )
}