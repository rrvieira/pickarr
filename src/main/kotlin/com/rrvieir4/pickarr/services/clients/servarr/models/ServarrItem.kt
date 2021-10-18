package com.rrvieir4.pickarr.services.clients.servarr.models

interface ServarrItem {
    val imdbId: String
    val title: String
    val year: Int
    val images: List<Image>
    val genres: List<String>
    val from: String

    val posterUrl: String
        get() = images.first { it.coverType == CoverType.poster }.remoteUrl

    fun hasGenre(genre: String): Boolean {
        return genres.firstOrNull { it.equals(genre, true) } != null
    }
}