package com.rrvieir4.pickarr.services.storage.models

import org.bson.codecs.pojo.annotations.BsonId

//Proposed?
data class SuggestedMediaItem(
    @BsonId
    val imdbId: String,
    val title: String,
    val year: Int
)