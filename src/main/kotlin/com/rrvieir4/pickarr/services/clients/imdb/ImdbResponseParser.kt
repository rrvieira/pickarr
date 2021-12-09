package com.rrvieir4.pickarr.services.clients.imdb

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun String.parseImdbMediaList(imdbUrl: String): List<ImdbItem>? {
    val doc: Document = Jsoup.parse(this)

    val trList = doc.select("tbody.lister-list > tr")
    val imdbItems = try {
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

                rating to totalVotes
            } else {
                0f to 0
            }

            ImdbItem(imdbId, title, year, imdbLink, rating, totalVotes, index + 1)
        }
    } catch (exception: Exception) {
        null
    }

    return imdbItems
}