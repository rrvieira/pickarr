package com.rrvieir4.pickarr.services.clients.tmdb.models

data class Credits(
    val id: Int?,
    val cast: List<Actor>?
) {
    val castNameList: List<String>
        get() = cast?.sortedByDescending { it.popularity }?.mapNotNull {
            it.name
        } ?: emptyList()
}
