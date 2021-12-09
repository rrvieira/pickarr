package com.rrvieir4.pickarr.services.reccommend.models

data class TmdbItem(
    val tmdbId: String,
    val imdbId: String,
    val originalLanguage: String,
    val castList: List<String>
)
