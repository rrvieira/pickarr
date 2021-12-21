package com.rrvieir4.pickarr.plugins

import com.rrvieir4.pickarr.config.Config
import com.rrvieir4.pickarr.services.notification.NotificationClient
import com.rrvieir4.pickarr.services.notification.telegram.TelegramClient
import com.rrvieir4.pickarr.services.popular.PopularService
import com.rrvieir4.pickarr.services.popular.sources.ImdbService
import com.rrvieir4.pickarr.services.recommendation.RecommendationService
import com.rrvieir4.pickarr.services.servarr.radarr.RadarrService
import com.rrvieir4.pickarr.services.servarr.sonarr.SonarrService
import com.rrvieir4.pickarr.services.storage.DBClient
import com.rrvieir4.pickarr.services.tmdb.TmdbService
import com.rrvieir4.pickarr.task.RecommendationTracker
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.ext.Koin

fun Application.setupDI() {
    install(Koin) {
        modules(appModule)
    }
}

private val appModule = module {
    single { Config.setupFromEnv() as Config }

    single {
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }

    factory<NotificationClient> {
        val config = get<Config>()
        TelegramClient(
            config.telegramConfig.telegramUserToken,
            config.telegramConfig.telegramChatId,
            config.actionUrlConfig.actionUrl,
            config.actionUrlConfig.addMovieMethod,
            config.actionUrlConfig.addTVMethod
        )
    }

    factory<PopularService> { ImdbService(get()) }

    factory {
        RadarrService(get<Config>().radarrConfig, get())
    }

    factory {
        SonarrService(get<Config>().sonarrConfig, get())
    }

    factory {
        TmdbService(get<Config>().tmdbConfig, get())
    }

    factory(named(MOVIES_RECOMMENDATION_SERVICE_NAME)) {
        RecommendationService(get(), get<RadarrService>())
    }

    factory(named(TV_RECOMMENDATION_SERVICE_NAME)) {
        RecommendationService(get(), get<SonarrService>())
    }

    factory {
        RecommendationService(get(), get<SonarrService>())
    }

    factory {
        val config = get<Config>()
        RecommendationTracker(
            get(),
            get(named(MOVIES_RECOMMENDATION_SERVICE_NAME)),
            get(named(TV_RECOMMENDATION_SERVICE_NAME)),
            DBClient,
            config.movieRequirements,
            config.tvRequirements
        )
    }
}

private const val MOVIES_RECOMMENDATION_SERVICE_NAME = "movies"
private const val TV_RECOMMENDATION_SERVICE_NAME = "tv"
