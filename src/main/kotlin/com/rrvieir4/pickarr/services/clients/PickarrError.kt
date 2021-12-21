package com.rrvieir4.pickarr.services.clients

sealed class PickarrError(val message: String? = null) {

    class GenericError(message: String? = null) : PickarrError(message)

    class ApiError(message: String? = null) : PickarrError(message)

    class ParseError(message: String? = null) : PickarrError(message)
}
