package com.rrvieir4.pickarr.clients.notification.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.rrvieir4.pickarr.clients.common.MediaItem
import com.rrvieir4.pickarr.clients.notification.NotificationClient


class TelegramClient(apiKey: String, chatIdKey: Long, private val serverAddress: String) : NotificationClient {

    private val bot = bot {
        token = apiKey
    }
    private val chatId = ChatId.fromId(chatIdKey)

    override suspend fun notifyNewMovies(mediaItemList: List<MediaItem>) {
        sendMessage(MOVIES_TITLE, MOVIES_ADD_METHOD, mediaItemList)
    }

    override suspend fun notifyNewTV(mediaItemList: List<MediaItem>) {
        sendMessage(TV_TITLE, TV_ADD_METHOD, mediaItemList)
    }

    private fun sendMessage(title: String, addMethod: String, mediaItemList: List<MediaItem>) {
        if (mediaItemList.isEmpty()) {
            return
        }

        val messageBuilder = StringBuilder(MESSAGE_TITLE_TEMPLATE.format(title, mediaItemList.size))

        mediaItemList.forEach {
            messageBuilder.append(it.formatMessage(addMethod))
        }

        bot.sendMessage(chatId, messageBuilder.toString(), parseMode = ParseMode.HTML, disableWebPagePreview = true)
    }

    private fun MediaItem.formatMessage(addMethod: String): String {
        return MEDIA_ITEM_TEMPLATE.format(
            link,
            title,
            year,
            rating,
            totalVotes,
            popularityPosition,
            "$serverAddress/$addMethod/$imdbId",//TODO
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