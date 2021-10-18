package com.rrvieir4.pickarr.services.clients.servarr.models

data class Image(
    val coverType: CoverType,
    val url: String,
    val remoteUrl: String
)

enum class CoverType {
    poster,
    fanart,
    banner
}