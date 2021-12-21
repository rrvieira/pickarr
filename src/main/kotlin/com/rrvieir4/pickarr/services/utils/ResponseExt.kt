package com.rrvieir4.pickarr.services.utils

import com.rrvieir4.pickarr.services.clients.PickarrError
import com.rrvieir4.pickarr.services.clients.Response

inline fun <S : Any, G : Any> Response<S, PickarrError>.rewrap(rewrap: (S) -> Response<G, PickarrError>): Response<G, PickarrError> {
    return when (this) {
        is Response.Failure -> this
        is Response.Success -> rewrap(this.body)
    }
}

inline fun <S : Any> Response<S, PickarrError>.unwrap(onError: (PickarrError) -> Unit = {}, onSuccess: (S) -> Unit) {
    return when (this) {
        is Response.Failure -> onError(this.body)
        is Response.Success -> onSuccess(this.body)
    }
}

fun <S : Any> Response<S, PickarrError>.unwrapSuccess(): S? {
    return when (this) {
        is Response.Failure -> null
        is Response.Success -> this.body
    }
}

fun <S : Any> Response<S, PickarrError>.unwrap(): Pair<S?, PickarrError?> {
    return when (this) {
        is Response.Failure -> null to this.body
        is Response.Success -> this.body to null
    }
}