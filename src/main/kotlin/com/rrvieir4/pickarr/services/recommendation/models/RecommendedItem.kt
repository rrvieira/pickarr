package com.rrvieir4.pickarr.services.recommendation.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.bson.codecs.pojo.annotations.BsonId

data class RecommendedItem(
    @BsonId
    override val id: String,
    override val title: String,
    override val overview: String,
    override val year: Int,
    override val posterUrl: String,
    override val genres: List<String>,
    override val from: String,
    override val link: String = "",
    override val rating: Float = 0f,
    override val totalVotes: Int = 0,
    override val popularityPosition: Int = 0,
    override val originalLanguageCode: String = "",
    override val castList: List<String> = emptyList()
) : IRecommendedDetailsItem, Comparable<RecommendedItem> {
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

@JsonDeserialize(`as` = RecommendedItem::class)
interface IRecommendedItem {
    val id: String
    val title: String
    val overview: String
    val year: Int
    val posterUrl: String
    val genres: List<String>
    val from: String
}

@JsonDeserialize(`as` = RecommendedItem::class)
interface IRecommendedDetailsItem : IRecommendedItem {
    val link: String
    val rating: Float
    val totalVotes: Int
    val popularityPosition: Int
    val originalLanguageCode: String
    val castList: List<String>
}