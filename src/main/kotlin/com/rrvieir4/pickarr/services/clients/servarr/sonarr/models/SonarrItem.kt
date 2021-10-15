package com.rrvieir4.pickarr.services.clients.servarr.sonarr.models

import com.rrvieir4.pickarr.services.clients.servarr.models.Image
import com.rrvieir4.pickarr.services.clients.servarr.models.Rating
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.storage.models.RecommendedItem

data class SonarrItem(
    override val title: String,
    val sortTitle: String,
    val status: String,
    val ended: Boolean,
    val overview: String,
    val network: String,
    val airTime: String,
    val images: List<Image>,
    val remotePoster: String,
    val seasons: List<Season>,
    override val year: Int,
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
    override val imdbId: String,
    val titleSlug: String,
    val certification: String,
    override val genres: List<String>,
    val tags: List<Int>,
    val added: String,
    val ratings: Rating?,
    val statistics: Statistics?,
    val qualityProfileId: Int,
    val languageProfileId: Int,
    val folder: String,
    val rootFolderPath: String?,
    val addOptions: TVAddOptions?
) : ServarrItem {
    override val from: String
        get() = network
}
