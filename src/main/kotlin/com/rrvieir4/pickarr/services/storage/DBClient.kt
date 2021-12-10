package com.rrvieir4.pickarr.services.storage

import com.rrvieir4.pickarr.services.recommendation.models.IRecommendedItem
import com.rrvieir4.pickarr.services.recommendation.models.RecommendedItem
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object DBClient {
    private const val DB_NAME = "pickarr"

    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase(DB_NAME)
    private val suggestedMediaCollection = database.getCollection<IRecommendedItem>()

    private suspend fun recommendedItems(): List<IRecommendedItem> {
        return suggestedMediaCollection.find().toList()
    }

    suspend fun updateRecommendedItems(newRecommendedItems: List<IRecommendedItem>): List<IRecommendedItem> {
        val recommendedItemsSet = recommendedItems().toSet()
        newRecommendedItems.subtract(recommendedItemsSet).forEach {
            suggestedMediaCollection.save(it)
        }
        return recommendedItems()
    }
}