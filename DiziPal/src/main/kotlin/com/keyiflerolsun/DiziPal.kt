// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink


class DiziPal : MainAPI() {
    override var mainUrl            = "https://dizipal639.com"
    override var name               = "DiziPal"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    override val mainPage =
        mainPageOf(
            "$mainUrl/diziler?kelime=&durum=&tur=1&type=&siralama=" to "Aile",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
        Log.d("DZP", "document » $document")

        val home     = document.select("article.type2 ul li").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span.title")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        Log.d("DZP", "url » $url")
        Log.d("DZP", "document » $document")

        return null
    }

    override suspend fun search(query: String): List<SearchResponse> {
        Log.d("DZP", "query » $query")

        return emptyList()
        // val document = app.get("$mainUrl/arama/$query").document

        // return document.select("li.film").mapNotNull { it.toSearchResult() }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZP", "data » $data")

            return true
    }
}
