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
            "$mainUrl/trend"        to "Bu Ay Öne Çıkanlar",
            "$mainUrl/imdb-top-100" to "Top Diziler",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
        val home     = document.select("a[href*='/dizi/'].gap-4").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h2")?.text() ?: return null
        val href      = fixUrlNull(this.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.w-full h1")?.text() ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.w-full img")?.attr("src")) ?: return null
        val year        = document.select("div.w-full span.text-cf")?.get(0)?.text()?.trim()?.toIntOrNull()
        val description = document.selectFirst("div.left-content-paragraf")?.text()?.trim()
        val tags        = document.select("[href*='dizi-turu']").map { it.text() }
        val rating      = document.selectFirst("a[href*='imdb.com'] span")?.text()?.split(".")?.first()?.trim()?.toIntOrNull()
        val duration    = Regex("(\d+)").find(document.selectFirst("span.bg-[#2e2e2e]")?.text() ?: "")?.value?.toIntOrNull()

        val actors = document.select("[href*='oyuncu']").map {
            Actor(it.text())
        }

        val episodes    = document.select("div.season-lists div.cursor-pointer").mapNotNull {
            val ep_name        = it.select("a")?.last()?.text()?.trim() ?: return@mapNotNull null
            val ep_href        = fixUrlNull(it.selectFirst("a.opacity-60")?.attr("href")) ?: return@mapNotNull null
            val ep_description = it.selectFirst("span.t-content")?.text()?.trim()
            val ep_episode     = it.selectFirst("a.opacity-60")?.text()?.toIntOrNull()

            val parent_div   = it.parent()
            val season_class = parent_div?.className()?.split(" ")?.find { it.startsWith("szn") }
            val ep_season    = season_class?.substringAfter("szn")?.toIntOrNull()

            Episode(
                data        = ep_href,
                name        = ep_name,
                season      = ep_season,
                episode     = ep_episode,
                posterUrl   = null,
                rating      = null,
                description = ep_description,
                date        = null
            )
        }

        Log.d("DZL", "duration » $duration")

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.rating          = rating
            this.duration        = duration
        //     this.recommendations = recommendations
            addActors(actors)
        //     addTrailer(trailer)
        }
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
