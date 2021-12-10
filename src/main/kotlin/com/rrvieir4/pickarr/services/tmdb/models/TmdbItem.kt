package com.rrvieir4.pickarr.services.tmdb.models

data class TmdbItem(
    val tmdbId: String,
    val imdbId: String,
    val originalLanguage: String,
    val castList: List<String>
)
