package com.rrvieir4.pickarr.services.clients.imdb

import com.rrvieir4.pickarr.services.popular.PopularItem

data class ImdbItem(
    override val id: String,
    override val title: String,
    override val year: Int,
    override val link: String,
    override val rating: Float,
    override val totalVotes: Int,
    override val popularityPosition: Int
) : PopularItem {
    override fun compareTo(other: PopularItem): Int {
        return if (rating == other.rating && totalVotes == other.totalVotes) {
            other.popularityPosition.compareTo(popularityPosition)
        } else if (rating == other.rating) {
            totalVotes.compareTo(other.totalVotes)
        } else {
            rating.compareTo(other.rating)
        }
    }
}