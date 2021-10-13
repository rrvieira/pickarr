package com.rrvieir4.pickarr.services.clients.tvdb

fun String.parseTvdbId(): String =
    Regex("(?<=<seriesid>)\\d+(?=</seriesid>)").find(this)!!.value