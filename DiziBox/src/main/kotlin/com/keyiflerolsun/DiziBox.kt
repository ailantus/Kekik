// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink


class DiziBox : MainAPI() {
    override var mainUrl            = "https://www.dizibox.tv"
    override var name               = "DiziBox"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    override val mainPage =
        mainPageOf(
            "/?tur[0]=aile&yil&imdb&orderby=imdb"     to "Aile",
            "/?tur[0]=aksiyon&yil&imdb&orderby=imdb"  to "Aksiyon",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url      = "$mainUrl/dizi-arsivi/page/" + page + request.data
        Log.d("DZB", "url » $url")

        val document = app.get(
            url,
            referer = "$mainUrl/",
            headers = mapOf(
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
            )
        ).document
        Log.d("DZB", "document » $document")

        val home     = document.select("article.detailed-article").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h3 a")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("h3 a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        Log.d("DZB", "url » $url")
        Log.d("DZB", "document » $document")

        return null
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZB", "data » $data")

            return true
    }
}
