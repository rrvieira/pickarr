package com.rrvieir4.pickarr.clients.servarr.sonarr.models

import com.rrvieir4.pickarr.clients.servarr.models.Image
import com.rrvieir4.pickarr.clients.servarr.models.Rating

data class TVItem(
    val title: String,
    val sortTitle: String,
    val status: String,
    val ended: Boolean,
    val overview: String,
    val network: String,
    val airTime: String,
    val images: List<Image>,
    val remotePoster: String,
    val seasons: List<Season>,
    val year: Int,
    val seasonFolder: Boolean,
    val monitored: Boolean,
    val useSceneNumbering: Boolean,
    val runtime: Int,
    val tvdbId: Int,
    val tvRageId: Int,
    val tvMazeId: Int,
    val firstAired: String,
    val seriesType: SeriesType,
    val cleanTitle: String,
    val imdbId: String,
    val titleSlug: String,
    val certification: String,
    val genres: List<String>,
    val tags: List<Int>,
    val added: String,
    val ratings: Rating?,
    val statistics: Statistics?,
    val qualityProfileId: Int,
    val languageProfileId: Int,
    val folder: String,
    val rootFolderPath: String?,
    val addOptions: TVAddOptions?
) {
    fun hasGenre(genre: String): Boolean {
        return genres.firstOrNull { it.equals(genre, true) } != null
    }
}
