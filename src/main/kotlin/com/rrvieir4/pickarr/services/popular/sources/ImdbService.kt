package com.rrvieir4.pickarr.services.popular.sources

import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.imdb.ImdbClient
import com.rrvieir4.pickarr.services.popular.PopularItem
import com.rrvieir4.pickarr.services.popular.PopularService
import io.ktor.client.*

class ImdbService(httpClient: HttpClient) : PopularService {
    private val imdbClient = ImdbClient(httpClient)

    override suspend fun fetchPopularMovies(): Response<List<PopularItem>, ClientError> = imdbClient.getPopularMovies()
    override suspend fun fetchPopularTV(): Response<List<PopularItem>, ClientError> = imdbClient.getPopularTV()
}