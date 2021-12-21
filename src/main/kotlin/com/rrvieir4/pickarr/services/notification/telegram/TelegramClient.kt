package com.rrvieir4.pickarr.services.notification.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.recommendation.models.RecommendedDetailsItem
import java.util.*


class TelegramClient(
    apiKey: String,
    chatIdKey: Long,
    private val serverAddress: String,
    private val serverAddMovieMethod: String,
    private val serverAddTVMethod: String
) : NotificationClient {

    private val bot = bot {
        token = apiKey
    }
    private val chatId = ChatId.fromId(chatIdKey)

    override suspend fun notifyNewMovies(recommendedItemList: List<RecommendedDetailsItem>): Boolean {
        return sendMessage(NotificationConfig.RADARR, serverAddMovieMethod, recommendedItemList)
    }

    override suspend fun notifyNewTV(recommendedItemList: List<RecommendedDetailsItem>): Boolean {
        return sendMessage(NotificationConfig.SONARR, serverAddTVMethod, recommendedItemList)
    }

    override suspend fun notifyTaskError(type: String?, error: String?) {
        sendMessage(ERROR_TEMPLATE.format(type ?: ERROR_MISSING_DETAIL, error ?: ERROR_MISSING_DETAIL))
    }

    private fun sendMessage(
        notificationConfig: NotificationConfig,
        addMethod: String,
        recommendedItemList: List<RecommendedDetailsItem>
    ): Boolean {
        if (recommendedItemList.isEmpty()) {
            return false
        }

        sendMessage(
            MESSAGE_TITLE_TEMPLATE.format(
                notificationConfig.icon,
                if (recommendedItemList.size > 1) "${recommendedItemList.size} " else "",
                notificationConfig.title,
                if (recommendedItemList.size > 1) "s" else "",
            )
        )
        recommendedItemList.onEach {
            sendMessage(it.formatMessage(notificationConfig, addMethod))
        }

        return true
    }

    private fun sendMessage(html: String) {
        bot.sendMessage(chatId, html, parseMode = ParseMode.HTML, disableWebPagePreview = false)
    }

    private fun RecommendedDetailsItem.formatMessage(notificationConfig: NotificationConfig, addMethod: String): String {
        return MEDIA_ITEM_TEMPLATE.format(
            posterUrl,
            link,
            title,
            year,
            from,
            overview,
            rating,
            totalVotes,
            popularityPosition,
            Locale(originalLanguageCode).getDisplayLanguage(Locale.ENGLISH),
            genres.joinToString(", "),
            castList.take(4).joinToString(", "),
            "$serverAddress/$addMethod/$imdbId",
            notificationConfig.addToServarrName
        )
    }

    private companion object {
        const val MESSAGE_TITLE_TEMPLATE = "<b>%s New %s%s%s</b>"
        const val MEDIA_ITEM_TEMPLATE = """<a href="%s">&#8205;</a><a href="%s"><b>%s (%s)</b></a>
<i>by: %s</i>
%s

<b>Rating:</b> <code>%s (%s)</code>
<b>Popularity:</b> <code>#%s</code>
<b>Language:</b> <code>%s</code>
<b>Genres:</b>
<code>%s</code>
<b>Main cast:</b>
<code>%s</code>

<a href="%s">⬇️ Add to %s</a>"""

        const val ERROR_TEMPLATE = """<b>Something went wrong</b>
<b>Type:</b> <code>%s</code>
<b>Details:</b> <code>%s</code>"""

        const val ERROR_MISSING_DETAIL = "?"
    }

    private enum class NotificationConfig(val title: String, val icon: String, val addToServarrName: String) {
        RADARR("Movie", "🎥", "Radarr"),
        SONARR("TV Show", "📺", "Sonarr")
    }
}