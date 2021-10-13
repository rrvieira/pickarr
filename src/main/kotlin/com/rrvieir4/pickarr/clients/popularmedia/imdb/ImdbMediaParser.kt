package com.rrvieir4.pickarr.clients.popularmedia.imdb

import com.rrvieir4.pickarr.clients.common.MediaItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger

fun String.parseImdbMediaList(imdbUrl: String, log: Logger? = null): List<MediaItem>? {

    val doc: Document = Jsoup.parse(this)

    val trList = doc.select("tbody.lister-list > tr")

    val mediaItems = try {
        trList.mapIndexed { index, tr ->

            val titleTd = tr.select("td.titleColumn")
            val titleA = titleTd.select("a")
            val titleHref = titleA.attr("href").substringBefore("?")
            val titleSpan = titleTd.select("td.titleColumn > span")

            val ratingStrong = tr.select("td.imdbRating > strong")

            val title = titleA.text()
            val imdbLink = "$imdbUrl$titleHref"
            val imdbId = Regex("tt\\d+").find(titleHref)!!.value
            val year = (Regex("(\\d+)").find(titleSpan.text())?.value ?: "0").toInt()
            val (rating, totalVotes) = if (ratingStrong.isNotEmpty()) {
                val rating = ratingStrong.text().toFloat()
                val totalVotes = Regex("(?<=\\s)([\\d,.]+)(?=\\suser\\sratings)")
                    .find(ratingStrong.attr("title"))!!.value
                    .replace(",", "")
                    .toInt()

                Pair(rating, totalVotes)
            } else {
                Pair(0f, 0)
            }

            MediaItem(imdbId, title, year, imdbLink, rating, totalVotes, index + 1)
        }
    } catch (exception: Exception) {
        log?.error("Imdb media items parse failed.")
        null
    }

    mediaItems?.let {
        log?.info("Imdb media items parse success.")
    }
    return mediaItems
}