package com.rrvieir4.pickarr.clients.servarr.sonarr.models

data class Statistics(
    val seasonCount: Int,
    val episodeFileCount: Int,
    val episodeCount: Int,
    val totalEpisodeCount: Int,
    val sizeOnDisk: Long,
    val percentOfEpisodes: Double
)