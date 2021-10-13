package com.rrvieir4.pickarr.services.clients.servarr.models

data class Collection(
    val name: String,
    val tmdbId: Int,
    val images: List<Image>
)