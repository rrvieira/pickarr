package com.rrvieir4.pickarr.services.servarr

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.servarr.sonarr.models.SonarrItem
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

interface ServarrService<I : ServarrItem> {
    fun getItemDetailWebpageUrl(item: I): String

    suspend fun getItems(): Response<List<I>, PickarrError>

    suspend fun addItem(imdbId: String): Response<I, PickarrError>

    suspend fun getTags(): Response<List<Tag>, PickarrError>

    suspend fun addTag(tagName: String): Response<Tag, PickarrError>

    suspend fun lookupItemWithImdbId(imdbId: String): Response<List<I>, PickarrError>

    suspend fun getItems(imdbIdList: List<String>): Response<List<I>, PickarrError> {
        val items = imdbIdList.asFlow().map { imdbId ->
            when (val lookupResponse = lookupItemWithImdbId(imdbId)) {
                is Response.Failure -> null
                is Response.Success -> lookupResponse.body.first()
            }
        }.toList().filterNotNull()

        return if (items.size == imdbIdList.size) {
            Response.Success(items)
        } else {
            Response.Failure(PickarrError.ApiError("Failed to get items info from servarr"))
        }
    }

    suspend fun saveItem(imdbId: String): Response<I, PickarrError> {
        return when (val itemsResponse = getItems()) {
            is Response.Success -> {
                val existingMovie = itemsResponse.body.find { it.imdbId == imdbId }
                if (existingMovie == null) {
                    addItem(imdbId)
                } else {
                    Response.Success(existingMovie)
                }
            }
            is Response.Failure -> itemsResponse
        }
    }

    suspend fun saveTag(tagName: String): Response<Tag, PickarrError> {
        return when (val tagsResponse = getTags()) {
            is Response.Success -> {
                val existingTag = tagsResponse.body.find { it.label == tagName }
                if (existingTag == null) {
                    addTag(tagName)
                } else {
                    Response.Success(existingTag)
                }
            }
            is Response.Failure -> tagsResponse
        }
    }
}