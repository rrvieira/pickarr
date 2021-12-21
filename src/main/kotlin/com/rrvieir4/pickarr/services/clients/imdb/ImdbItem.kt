package com.rrvieir4.pickarr.services.clients.imdb

import com.rrvieir4.pickarr.services.popular.PopularItem

data class ImdbItem(
    override val imdbId: String,
    override val title: String,
    override val year: Int,
    override val link: String,
    override val rating: Float,
    override val totalVotes: Int,
    override val popularityPosition: Int
) : PopularItem