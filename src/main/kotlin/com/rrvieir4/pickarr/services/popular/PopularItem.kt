package com.rrvieir4.pickarr.services.popular

interface PopularItem : Comparable<PopularItem> {
    val id: String
    val title: String
    val year: Int
    val link: String
    val rating: Float
    val totalVotes: Int
    val popularityPosition: Int
}