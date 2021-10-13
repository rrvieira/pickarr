package com.rrvieir4.pickarr.clients.common

sealed class ClientError(val error: String? = null) {

    class GenericError(error: String? = null) : ClientError(error)

    class ApiError(error: String? = null) : ClientError(error)

    class ParseError(error: String? = null) : ClientError(error)
}