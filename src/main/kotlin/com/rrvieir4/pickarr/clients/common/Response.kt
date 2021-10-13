package com.rrvieir4.pickarr.clients.common

sealed class Response<out S : Any, out F : Any?> {

    data class Success<S : Any>(val body: S) : Response<S, Nothing>()

    data class Failure<F : Any?>(val body: F) : Response<Nothing, F>()
}
