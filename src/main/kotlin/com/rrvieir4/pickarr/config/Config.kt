package com.rrvieir4.pickarr.config

data class Config(
    val refreshInterval: RefreshInterval,
    val radarrConfig: ServarrConfig,
    val sonarrConfig: ServarrConfig,
    val movieRequirements: MediaRequirements,
    val tvRequirements: MediaRequirements,
    val tagName: String,
    val telegramConfig: TelegramConfig,
) {
    data class RefreshInterval(val default: Long, val retry: Long)
    data class ServarrConfig(val url: String, val apiKey: String, val qualityProfileName: String, val tagName: String)
    data class MediaRequirements(val minYear: Int, val minVotes: Int, val minRating: Float)
    data class TelegramConfig(val telegramUserToken: String, val telegramChatId: Long, val telegramActionUrl: String)

    companion object {
        private const val REFRESH_INTERVAL = "refreshInterval"

        private const val RADARR_URL = "radarrUrl"
        private const val RADARR_API_KEY = "radarrApiKey"
        private const val RADARR_QUALITY_PROFILE_NAME = "radarrQualityProfileName"

        private const val SONARR_URL = "sonarrUrl"
        private const val SONARR_API_KEY = "sonarrApiKey"
        private const val SONARR_QUALITY_PROFILE_NAME = "sonarrQualityProfileName"

        private const val TAG_NAME = "tagName"

        private const val MOVIE_MIN_YEAR = "movieMinYear"
        private const val MOVIE_MIN_VOTES = "movieMinVotes"
        private const val MOVIE_MIN_RATING = "movieMinRating"

        private const val TV_MIN_YEAR = "tvMinYear"
        private const val TV_MIN_VOTES = "tvMinVotes"
        private const val TV_MIN_RATING = "tvMinRating"

        private const val TELEGRAM_USER_TOKEN = "telegramUserToken"
        private const val TELEGRAM_CHAT_ID = "telegramChatId"
        private const val NOTIFICATION_ACTION_URL = "actionAddress"

        private const val RETRY_REFRESH_INTERVAL = 1800L

        private const val DEFAULT_TAG_NAME = "pickarr"

        private const val DEFAULT_MOVIE_MIN_YEAR = 2021
        private const val DEFAULT_MOVIE_MIN_VOTES = 3000
        private const val DEFAULT_MOVIE_MIN_RATING = 5.7f

        private const val DEFAULT_TV_MIN_YEAR = 2021
        private const val DEFAULT_TV_MIN_VOTES = 3000
        private const val DEFAULT_TV_MIN_RATING = 7.5f

        fun setupFromEnv(): Config? {
            val refreshInterval = try {
                System.getenv(REFRESH_INTERVAL).toLong()
            } catch (e: NumberFormatException) {
                (24 * 60 * 60)
            }

            val radarrUrl = System.getenv(RADARR_URL) ?: return null
            val radarrApiKey = System.getenv(RADARR_API_KEY) ?: return null
            val radarQualityProfileName = System.getenv(RADARR_QUALITY_PROFILE_NAME) ?: return null

            val sonarrUrl = System.getenv(SONARR_URL) ?: return null
            val sonarrApiKey = System.getenv(SONARR_API_KEY) ?: return null
            val sonarrQualityProfileName = System.getenv(SONARR_QUALITY_PROFILE_NAME) ?: return null

            val tagName = System.getenv(TAG_NAME) ?: DEFAULT_TAG_NAME

            val (movieMinYear, movieMinVotes, movieMinRating) = try {
                Triple(
                    System.getenv(MOVIE_MIN_YEAR)?.toInt() ?: DEFAULT_MOVIE_MIN_YEAR,
                    System.getenv(MOVIE_MIN_VOTES)?.toInt() ?: DEFAULT_MOVIE_MIN_VOTES,
                    System.getenv(MOVIE_MIN_RATING)?.toFloat() ?: DEFAULT_MOVIE_MIN_RATING
                )
            } catch (e: NumberFormatException) {
                return null
            }

            val (tvMinYear, tvMinVotes, tvMinRating) = try {
                Triple(
                    System.getenv(TV_MIN_YEAR)?.toInt() ?: DEFAULT_TV_MIN_YEAR,
                    System.getenv(TV_MIN_VOTES)?.toInt() ?: DEFAULT_TV_MIN_VOTES,
                    System.getenv(TV_MIN_RATING)?.toFloat() ?: DEFAULT_TV_MIN_RATING
                )
            } catch (e: NumberFormatException) {
                return null
            }

            val telegramActionUrl = System.getenv(NOTIFICATION_ACTION_URL) ?: return null
            val telegramUserToken = System.getenv(TELEGRAM_USER_TOKEN) ?: return null
            val telegramChatId = try {
                System.getenv(TELEGRAM_CHAT_ID)?.toLong() ?: return null
            } catch (e: NumberFormatException) {
                return null
            }

            return Config(
                RefreshInterval(refreshInterval, RETRY_REFRESH_INTERVAL),
                ServarrConfig(radarrUrl, radarrApiKey, radarQualityProfileName, tagName),
                ServarrConfig(sonarrUrl, sonarrApiKey, sonarrQualityProfileName, tagName),
                MediaRequirements(movieMinYear, movieMinVotes, movieMinRating),
                MediaRequirements(tvMinYear, tvMinVotes, tvMinRating),
                tagName,
                TelegramConfig(telegramUserToken, telegramChatId, telegramActionUrl)
            )
        }
    }
}