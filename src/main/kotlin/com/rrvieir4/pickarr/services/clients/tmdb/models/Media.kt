package com.rrvieir4.pickarr.services.clients.tmdb.models

import com.google.gson.annotations.SerializedName

data class Media(
    val id: String?,
    @SerializedName(value = "name", alternate = ["title"])
    val name: String?,
    @SerializedName(value = "original_language")
    val originalLanguage: String?
)
