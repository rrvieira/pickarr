package com.rrvieir4.pickarr.services.clients.tmdb.models

import com.google.gson.annotations.SerializedName

data class FindResponse(
    @SerializedName(value = "movie_results")
    val movieResults: List<Media>?,
    @SerializedName(value = "tv_results")
    val tvResults: List<Media>?
) {
    val results: List<Media>?
        get() = if (isMovie) {
            movieResults
        } else {
            tvResults
        }

    val isMovie: Boolean
        get() = movieResults?.isNotEmpty() == true

    val isTV: Boolean
        get() = tvResults?.isNotEmpty() == true
}
