package com.rrvieir4.pickarr.clients.storage

import com.rrvieir4.pickarr.clients.common.MediaItem
import com.rrvieir4.pickarr.clients.servarr.radarr.models.MovieItem
import com.rrvieir4.pickarr.clients.servarr.sonarr.models.TVItem
import com.rrvieir4.pickarr.clients.storage.models.SuggestedMediaItem
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object DBClient {
    private const val DB_NAME = "pickarr"

    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase(DB_NAME)
    private val suggestedMediaCollection = database.getCollection<SuggestedMediaItem>()

    suspend fun suggestedMediaList(): List<SuggestedMediaItem> {
        return suggestedMediaCollection.find().toList()
    }

    suspend fun updateWithMediaItems(mediaItems: List<MediaItem>): List<SuggestedMediaItem> {
        val newSuggestedMediaItems = mediaItems.map { SuggestedMediaItem(it.imdbId, it.title, it.year) }
        return addSuggestedItems(newSuggestedMediaItems)
    }

    suspend fun updateWithMovieItems(movieItems: List<MovieItem>): List<SuggestedMediaItem> {
        val newSuggestedMediaItems = movieItems.map { SuggestedMediaItem(it.imdbId, it.title, it.year) }
        return addSuggestedItems(newSuggestedMediaItems)
    }

    suspend fun updateWithTVItems(tvItems: List<TVItem>): List<SuggestedMediaItem> {
        val newSuggestedMediaItems = tvItems.map { SuggestedMediaItem(it.imdbId, it.title, it.year) }
        return addSuggestedItems(newSuggestedMediaItems)
    }

    private suspend fun addSuggestedItems(newSuggestedMediaItems: List<SuggestedMediaItem>): List<SuggestedMediaItem> {
        val suggestedMediaItem = suggestedMediaList().toSet()
        newSuggestedMediaItems.subtract(suggestedMediaItem).forEach {
            suggestedMediaCollection.save(it)
        }
        return suggestedMediaList()
    }
}