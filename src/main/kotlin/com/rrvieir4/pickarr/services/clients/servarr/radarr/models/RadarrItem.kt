package com.rrvieir4.pickarr.services.clients.servarr.radarr.models

import com.rrvieir4.pickarr.services.clients.servarr.models.Collection
import com.rrvieir4.pickarr.services.clients.servarr.models.Image
import com.rrvieir4.pickarr.services.clients.servarr.models.Rating
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem

data class RadarrItem(
    val id: String?,
    override val title: String,
    val sortTitle: String,
    val sizeOnDisk: Long,
    override val overview: String,
    val inCinemas: String?,
    val physicalRelease: String?,
    override val images: List<Image>,
    val website: String,
    override val year: Int,
    val hasFile: Boolean,
    val youTubeTrailerId: String,
    val studio: String,
    val rootFolderPath: String?,
    val qualityProfileId: Int,
    val monitored: Boolean,
    val minimumAvailability: String,
    val isAvailable: Boolean,
    val folderName: String,
    val runtime: Int,
    val cleanTitle: String,
    override val imdbId: String,
    val tmdbId: Int,
    val titleSlug: String,
    val certification: String,
    override val genres: List<String>,
    val tags: List<Int>,
    val added: String,
    val ratings: Rating,
    val collection: Collection?,
    val status: String,
    val addOptions: MovieAddOptions?
) : ServarrItem {
    override val from: String
        get() = studio
}