package com.rrvieir4.pickarr.services.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend inline fun <reified R : Any> HttpClient.pickarrGet(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Response<R, PickarrError> {
    return try {
        val httpResponse: HttpResponse = get(url) {
            block()
        }

        if (httpResponse.status == HttpStatusCode.OK) {
            Response.Success(httpResponse.receive())
        } else {
            Response.Failure(PickarrError.ApiError("Response status code not expected: ${httpResponse.status.value}. For 'get': $url"))
        }
    } catch (e: NoTransformationFoundException) {
        Response.Failure(PickarrError.ParseError(e.cause?.message))
    } catch (t: Throwable) {
        Response.Failure(PickarrError.GenericError(t.cause?.message))
    }
}

suspend inline fun <reified R : Any> HttpClient.pickarrPost(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Response<R, PickarrError> {
    return try {
        val httpResponse: HttpResponse = post(url) {
            block()
        }

        if (httpResponse.status == HttpStatusCode.Created) {
            Response.Success(httpResponse.receive())
        } else {
            Response.Failure(PickarrError.ApiError("Response status code not expected: ${httpResponse.status.value}. For 'post': $url"))
        }
    } catch (e: NoTransformationFoundException) {
        Response.Failure(PickarrError.ParseError(e.cause?.message))
    } catch (t: Throwable) {
        Response.Failure(PickarrError.GenericError(t.cause?.message))
    }
}