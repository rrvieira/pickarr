package com.rrvieir4.pickarr.clients.common

data class MediaItem(
    val imdbId: String,
    val title: String,
    val year: Int,
    val link: String,
    val rating: Float,
    val totalVotes: Int,
    val popularityPosition: Int
) : Comparable<MediaItem> {
    override fun compareTo(other: MediaItem): Int {
        return if (rating == other.rating && totalVotes == other.totalVotes) {
            other.popularityPosition.compareTo(popularityPosition)
        } else if (rating == other.rating) {
            totalVotes.compareTo(other.totalVotes)
        } else {
            rating.compareTo(other.rating)
        }
    }
}