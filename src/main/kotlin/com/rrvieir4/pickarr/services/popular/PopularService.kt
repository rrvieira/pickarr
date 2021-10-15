package com.rrvieir4.pickarr.services.popular

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response

interface PopularService {
    suspend fun fetchPopularMovies(): Response<List<PopularItem>, PickarrError>
    suspend fun fetchPopularTV(): Response<List<PopularItem>, PickarrError>
}