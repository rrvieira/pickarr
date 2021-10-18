package com.rrvieir4.pickarr.config

data class Config(
    val refreshInterval: RefreshInterval,
    val radarrConfig: ServarrConfig,
    val sonarrConfig: ServarrConfig,
    val movieRequirements: MediaRequirements,
    val tvRequirements: MediaRequirements,
    val tagName: String,
    val actionUrlConfig: ActionUrlConfig,
    val telegramConfig: TelegramConfig
) {
    data class RefreshInterval(val default: Long, val retry: Long)
    data class ServarrConfig(val url: String, val apiKey: String, val qualityProfileName: String, val tagName: String)
    data class MediaRequirements(val minYear: Int, val minVotes: Int, val minRating: Float)
    data class TelegramConfig(val telegramUserToken: String, val telegramChatId: Long)
    data class ActionUrlConfig(val actionUrl: String, val addMovieMethod: String, val addTVMethod: String)

    companion object {
        private const val REFRESH_INTERVAL = "REFRESH_INTERVAL"

        private const val RADARR_URL = "RADARR_URL"
        private const val RADARR_API_KEY = "RADARR_API_KEY"
        private const val RADARR_QUALITY_PROFILE_NAME = "RADARR_QUALITY_PROFILE_NAME"

        private const val SONARR_URL = "SONARR_URL"
        private const val SONARR_API_KEY = "SONARR_API_KEY"
        private const val SONARR_QUALITY_PROFILE_NAME = "SONARR_QUALITY_PROFILE_NAME"

        private const val TAG_NAME = "TAG_NAME"

        private const val MOVIE_MIN_YEAR = "MOVIE_MIN_YEAR"
        private const val MOVIE_MIN_VOTES = "MOVIE_MIN_VOTES"
        private const val MOVIE_MIN_RATING = "MOVIE_MIN_RATING"

        private const val TV_MIN_YEAR = "TV_SHOW_MIN_YEAR"
        private const val TV_MIN_VOTES = "TV_SHOW_MIN_VOTES"
        private const val TV_MIN_RATING = "TV_SHOW_MIN_RATING"

        private const val TELEGRAM_USER_TOKEN = "TELEGRAM_USER_TOKEN"
        private const val TELEGRAM_CHAT_ID = "TELEGRAM_CHAT_ID"

        private const val ACTION_URL = "ACTION_ADDRESS"

        private const val ACTION_ADD_MOVIE_METHOD = "add-movie"
        private const val ACTION_ADD_TV_METHOD = "add-tv"

        private const val DEFAULT_REFRESH_INTERVAL = 24 * 60 * 60L
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
                System.getenv(REFRESH_INTERVAL)?.toLong() ?: DEFAULT_REFRESH_INTERVAL
            } catch (e: NumberFormatException) {
                DEFAULT_REFRESH_INTERVAL
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

            val actionUrl = System.getenv(ACTION_URL) ?: return null

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
                ActionUrlConfig(actionUrl, ACTION_ADD_MOVIE_METHOD, ACTION_ADD_TV_METHOD),
                TelegramConfig(telegramUserToken, telegramChatId)
            )
        }
    }
}