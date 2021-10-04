package com.rrvieir4.pickarr.models

data class MediaItem(
    val id: String,
    val title: String,
    val year: Int,
    val link: String,
    val rating: Float,
    val totalVotes: Int
)