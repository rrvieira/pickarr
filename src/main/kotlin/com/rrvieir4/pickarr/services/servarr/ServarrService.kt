package com.rrvieir4.pickarr.services.servarr

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.rewrap
import com.rrvieir4.pickarr.services.clients.servarr.models.ServarrItem
import com.rrvieir4.pickarr.services.clients.servarr.models.Tag
import com.rrvieir4.pickarr.services.clients.unwrapSuccess
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun getItems(imdbIdList: List<String>): Response<Map<String, I>, PickarrError> = coroutineScope {
        val deferredList = mutableListOf<Deferred<Pair<String, I>?>>()
        imdbIdList.forEach { imdbId ->
            deferredList.add(
                async {
                    val item = lookupItemWithImdbId(imdbId).unwrapSuccess()?.firstOrNull()
                    item?.let {
                        imdbId to item
                    }
                }
            )
        }

        val itemsMap = deferredList.mapNotNull { deferred ->
            deferred.await()?.let {
                it.first to it.second
            }
        }.toMap()

        Response.Success(itemsMap)
    }

    suspend fun saveItem(imdbId: String): Response<I, PickarrError> {
        return getItems().rewrap { items ->
            val existingMovie = items.find { it.imdbId == imdbId }
            if (existingMovie == null) {
                addItem(imdbId)
            } else {
                Response.Success(existingMovie)
            }
        }
    }

    suspend fun saveTag(tagName: String): Response<Tag, PickarrError> {
        return getTags().rewrap { tags ->
            val existingTag = tags.find { it.label == tagName }
            if (existingTag == null) {
                addTag(tagName)
            } else {
                Response.Success(existingTag)
            }
        }
    }

    fun apiError(error: String): Response.Failure<PickarrError.ApiError> =
        Response.Failure(PickarrError.ApiError("${this::class.simpleName}: $error"))
}