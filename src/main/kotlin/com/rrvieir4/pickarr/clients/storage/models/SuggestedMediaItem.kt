package com.rrvieir4.pickarr.clients.storage.models

import org.bson.codecs.pojo.annotations.BsonId

data class SuggestedMediaItem(
    @BsonId
    val imdbId: String,
    val title: String,
    val year: Int
)