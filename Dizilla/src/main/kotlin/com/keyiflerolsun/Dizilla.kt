// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities


class Dizilla : MainAPI() {
    override var mainUrl            = "https://dizilla.club"
    override var name               = "Dizilla"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = false
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    override val mainPage           =
        mainPageOf(
            "$mainUrl/altyazili-bolumler?page=" to "Altyazılı Bölümler",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("a.gap-4").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h2")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-srcset")?.split(" ")?.first())

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        return null

        // return newMovieLoadResponse(title, url, TvType.Movie, url) {
        //     this.posterUrl       = poster
        //     this.year            = year
        //     this.plot            = description
        //     this.tags            = tags
        //     this.rating          = rating
        //     this.duration        = duration
        //     this.recommendations = recommendations
        //     addActors(actors)
        //     addTrailer(trailer)
        // }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZL", "data » $data")
            val document = app.get(data).document

            // callback.invoke(
            //     ExtractorLink(
            //         source  = this.name,
            //         name    = this.name,
            //         url     = m3u_link,
            //         referer = "$mainUrl/",
            //         quality = Qualities.Unknown.value,
            //         isM3u8  = true
            //     )
            // )

            return true
    }
}
