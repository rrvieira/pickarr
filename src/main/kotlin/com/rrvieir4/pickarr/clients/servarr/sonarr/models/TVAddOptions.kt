package com.rrvieir4.pickarr.clients.servarr.sonarr.models

data class TVAddOptions(
    val searchForMissingEpisodes: Boolean,
    val searchForCutoffUnmetEpisodes: Boolean = false,
    val monitor: String = "all"
)