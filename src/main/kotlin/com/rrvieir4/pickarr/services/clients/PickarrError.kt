package com.rrvieir4.pickarr.services.clients

sealed class PickarrError(val error: String? = null) {

    class GenericError(error: String? = null) : PickarrError(error)

    class ApiError(error: String? = null) : PickarrError(error)

    class ParseError(error: String? = null) : PickarrError(error)
}

inline fun <S : Any, G : Any> Response<S, PickarrError>.unwrapAndRewrap(doOnSuccess: (S) -> Response<G, PickarrError>): Response<G, PickarrError> {
    return when (this) {
        is Response.Failure -> this
        is Response.Success -> doOnSuccess(this.body)
    }
}

inline fun <S : Any> Response<S, PickarrError>.unwrap(onSuccess: (S) -> Unit, onError: (PickarrError) -> Unit) {
    return when (this) {
        is Response.Failure -> onError(this.body)
        is Response.Success -> onSuccess(this.body)
    }
}