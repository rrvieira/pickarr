package com.rrvieir4.pickarr.services.clients.servarr

import com.rrvieir4.pickarr.services.clients.ClientError
import com.rrvieir4.pickarr.services.clients.Response
import com.rrvieir4.pickarr.services.clients.pickarrGet
import com.rrvieir4.pickarr.services.clients.pickarrPost
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

abstract class ServarrClient(protected val baseUrl: String, protected val apiKey: String, protected val httpClient: HttpClient) {

    protected val apiUrl = "$baseUrl$API_URL_PATH"

    protected suspend inline fun <reified R : Any> get(
        methodName: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Response<R, ClientError> {
        return httpClient.pickarrGet("${apiUrl}${methodName}") {
            contentType(ContentType.Application.Json)
            parameter(API_KEY_PARAMETER, apiKey)
            block()
        }
    }

    protected suspend inline fun <reified B : Any, reified R : Any> post(
        methodName: String,
        content: B,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Response<R, ClientError> {
        return httpClient.pickarrPost("${apiUrl}${methodName}") {
            contentType(ContentType.Application.Json)
            parameter(API_KEY_PARAMETER, apiKey)
            body = content
            block()
        }
    }

    protected companion object {
        const val API_URL_PATH = "/api/v3/"
        const val API_KEY_PARAMETER = "apikey"
    }
}