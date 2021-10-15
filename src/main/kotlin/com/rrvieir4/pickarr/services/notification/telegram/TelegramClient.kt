package com.rrvieir4.pickarr.services.notification.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.rrvieir4.pickarr.services.clients.imdb.ImdbItem
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.storage.models.RecommendedItem


class TelegramClient(apiKey: String, chatIdKey: Long, private val serverAddress: String) : NotificationClient {

    private val bot = bot {
        token = apiKey
    }
    private val chatId = ChatId.fromId(chatIdKey)

    override suspend fun notifyNewMovies(recommendedItemList: List<RecommendedItem>): Boolean {
        return sendMessage(MOVIES_TITLE, MOVIES_ADD_METHOD, recommendedItemList)
    }

    override suspend fun notifyNewTV(recommendedItemList: List<RecommendedItem>): Boolean {
        return sendMessage(TV_TITLE, TV_ADD_METHOD, recommendedItemList)
    }

    override suspend fun notifyTaskError(type: String?, error: String?) {
        sendMessage(ERROR_TEMPLATE.format(type ?: ERROR_MISSING_DETAIL, error ?: ERROR_MISSING_DETAIL))
    }

    private fun sendMessage(title: String, addMethod: String, recommendedItemList: List<RecommendedItem>): Boolean {
        if (recommendedItemList.isEmpty()) {
            return false
        }

        sendMessage(MESSAGE_TITLE_TEMPLATE.format(title, recommendedItemList.size))

        val chunkedRecommendedItemLists = recommendedItemList.chunked(MAX_RECOMMENDATIONS_PER_MESSAGE)
        chunkedRecommendedItemLists.forEach { recommendations ->
            sendMessagePart(addMethod, recommendations)
        }
        return true
    }

    private fun sendMessagePart(
        addMethod: String,
        recommendedItemList: List<RecommendedItem>
    ) {
        val messageBuilder = StringBuilder()

        recommendedItemList.forEach {
            messageBuilder.append(it.formatMessage(addMethod))
        }

        sendMessage(messageBuilder.toString())
    }

    private fun sendMessage(html: String) {
        bot.sendMessage(chatId, html, parseMode = ParseMode.HTML, disableWebPagePreview = true)
    }

    private fun RecommendedItem.formatMessage(addMethod: String): String {
        return MEDIA_ITEM_TEMPLATE.format(
            link,
            title,
            year,
            from,
            genres.joinToString(", "),
            rating,
            totalVotes,
            popularityPosition,
            "$serverAddress/$addMethod/$id",//TODO
        )
    }

    private companion object {
        const val MOVIES_TITLE = "Movies"
        const val TV_TITLE = "TV"

        const val MOVIES_ADD_METHOD = "add-movie"
        const val TV_ADD_METHOD = "add-tv"

        const val MAX_RECOMMENDATIONS_PER_MESSAGE = 5

        const val MESSAGE_TITLE_TEMPLATE = "<b>Pickarr - New %s [%d]</b>"
        const val MEDIA_ITEM_TEMPLATE = """

<a href="%s"><b>%s (%s)</b></a>
<b>From:</b> <code>%s</code>
<b>Genres:</b> <code>%s</code>
<b>Rating:</b> <code>%s (%s)</code>
<b>Popularity:</b> <code>#%s</code>
<a href="%s">Add to library</a>"""

        const val ERROR_TEMPLATE = """<b>Something went wrong</b>
<b>Type:</b> <code>%s</code>
<b>Details:</b> <code>%s</code>"""

        const val ERROR_MISSING_DETAIL = "?"
    }
}