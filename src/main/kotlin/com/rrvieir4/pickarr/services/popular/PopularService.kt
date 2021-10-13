package com.rrvieir4.pickarr.services.popular

import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response

interface PopularService {
    suspend fun fetchPopularMovies(): Response<List<PopularItem>, ClientError>
    suspend fun fetchPopularTV(): Response<List<PopularItem>, ClientError>
}