package com.rrvieir4.pickarr.services.clients

sealed class PickarrError(val error: String? = null) {

    class GenericError(error: String? = null) : PickarrError(error)

    class ApiError(error: String? = null) : PickarrError(error)

    class ParseError(error: String? = null) : PickarrError(error)
}

inline fun <S : Any, G : Any> Response<S, PickarrError>.onSuccess(doOnSuccess: (S) -> Response<G, PickarrError>): Response<G, PickarrError> {
    return when (this) {
        is Response.Failure -> this
        is Response.Success -> doOnSuccess(this.body)
    }
}

inline fun Response<*, PickarrError>.onError(doOnError: (PickarrError) -> Unit) {
    return when (this) {
        is Response.Failure -> {
            doOnError(this.body)
        }
        is Response.Success -> {
        }
    }
}

inline fun <S : Any, G : Any> Response<S, PickarrError>.process(doOnSuccess: (S) -> Response<G, PickarrError>, doOnError: (PickarrError) -> Response.Failure<PickarrError>): Response<G, PickarrError> {
    return when (this) {
        is Response.Failure -> doOnError(this.body)
        is Response.Success -> doOnSuccess(this.body)
    }
}

fun <S : Any> success(body: S): Response.Success<S> = Response.Success(body)

fun genericFailure(error: String?): Response.Failure<PickarrError> =
    Response.Failure(PickarrError.GenericError(error))

fun apiFailure(error: String?): Response.Failure<PickarrError> = Response.Failure(PickarrError.ApiError(error))

fun parseFailure(error: String?): Response.Failure<PickarrError> =
    Response.Failure(PickarrError.ParseError(error))