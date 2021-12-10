package com.rrvieir4.pickarr.server

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.rrvieir4.pickarr.services.recommendation.RecommendationNotifier
import kotlinx.coroutines.*

class TelegramServer(
    private val apiKey: String,
    private val recommendationNotifier: RecommendationNotifier
) {

    suspend fun run(): Nothing = coroutineScope {

        val bot = bot {
            token = apiKey
            dispatch {
                command("popular") {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Wait a moment...")
                    launch {
                        recommendationNotifier.run(false)
                    }
                }
            }
        }
        bot.startPolling()
        awaitCancellation()
    }
}