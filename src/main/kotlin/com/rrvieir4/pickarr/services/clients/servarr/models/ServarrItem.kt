package com.rrvieir4.pickarr.services.clients.servarr.models

interface ServarrItem {
    val imdbId: String
    val title: String
    val year: Int
    val genres: List<String>
    val from: String

    fun hasGenre(genre: String): Boolean {
        return genres.firstOrNull { it.equals(genre, true) } != null
    }
}