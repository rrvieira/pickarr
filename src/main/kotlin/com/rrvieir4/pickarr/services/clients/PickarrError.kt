package com.rrvieir4.pickarr.services.clients

sealed class PickarrError(val error: String? = null) {

    class GenericError(error: String? = null) : PickarrError(error)

    class ApiError(error: String? = null) : PickarrError(error)

    class ParseError(error: String? = null) : PickarrError(error)
}

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