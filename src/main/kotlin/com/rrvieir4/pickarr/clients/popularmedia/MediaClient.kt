package com.rrvieir4.pickarr.clients.popularmedia

import com.rrvieir4.pickarr.clients.common.ClientError
import com.rrvieir4.pickarr.clients.common.Response
import com.rrvieir4.pickarr.clients.common.MediaItem

interface MediaClient {

    suspend fun fetchPopularMovies(): Response<List<MediaItem>, ClientError>
    suspend fun fetchPopularTV(): Response<List<MediaItem>, ClientError>
}