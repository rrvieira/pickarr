package com.rrvieir4.pickarr.services.notification.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.rrvieir4.pickarr.services.clients.imdb.ImdbItem
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.popular.PopularItem


class TelegramClient(apiKey: String, chatIdKey: Long, private val serverAddress: String) : NotificationClient {

    private val bot = bot {
        token = apiKey
    }
    private val chatId = ChatId.fromId(chatIdKey)

    override suspend fun notifyNewMovies(popularItemList: List<PopularItem>) {
        sendMessage(MOVIES_TITLE, MOVIES_ADD_METHOD, popularItemList)
    }

    override suspend fun notifyNewTV(popularItemList: List<PopularItem>) {
        sendMessage(TV_TITLE, TV_ADD_METHOD, popularItemList)
    }

    private fun sendMessage(title: String, addMethod: String, popularItemList: List<PopularItem>) {
        if (popularItemList.isEmpty()) {
            return
        }

        val messageBuilder = StringBuilder(MESSAGE_TITLE_TEMPLATE.format(title, popularItemList.size))

        popularItemList.forEach {
            messageBuilder.append(it.formatMessage(addMethod))
        }

        bot.sendMessage(chatId, messageBuilder.toString(), parseMode = ParseMode.HTML, disableWebPagePreview = true)
    }

    private fun PopularItem.formatMessage(addMethod: String): String {
        return MEDIA_ITEM_TEMPLATE.format(
            link,
            title,
            year,
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

        const val MESSAGE_TITLE_TEMPLATE = "<b>Pickarr - New %s [%d]</b>"
        const val MEDIA_ITEM_TEMPLATE = """

<a href="%s"><b>%s (%s)</b></a>
<b>Rating:</b> <code>%s</code>
<b>Votes:</b> <code>%s</code>
<b>Popularity:</b> <code>#%s</code>
<a href="%s">Add to library</a>"""
    }
}