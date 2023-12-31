// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class KoreanTurk : MainAPI() {
    override var mainUrl              = "https://www.koreanturk.com"
    override var name                 = "KoreanTurk"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.AsianDrama)

    override val mainPage = mainPageOf(
        "${mainUrl}/bolumler/page/" to "Son Eklenenler",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("div.standartbox").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun removeEpisodePart(url: String): String {
        val regex = "-[0-9]+-bolum-izle\\.html".toRegex()
        return regex.replace(url, "")
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val dizi      = this.selectFirst("h2 span")?.text()?.trim() ?: return null
        val bolum     = this.selectFirst("h2")?.ownText()?.substringBefore(".Bölüm")?.trim()
        val title     = "${dizi} | ${bolum}"

        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("div.resimcik img")?.attr("src"))

        return newTvSeriesSearchResponse(title, removeEpisodePart(href), TvType.AsianDrama) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.post("${mainUrl}/", data=mapOf("s" to "${query}")).document

        return document.select("div.standartbox").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("[property='og:title']")?.attr("content")?.substringAfter(" - Koreantürk Kore Dizileri")?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("div.resimcik img")?.attr("src"))
        val description     = document.selectFirst("[property='og:description']")?.attr("content")?.substringAfter(" Türler: ")?.trim()
        val tags            = document.selectFirst("[property='og:description']")?.attr("content")?.substringBefore(" Türler: ")?.trim()?.split(",")?.mapNotNull { it.trim() }
        val episodes        = document.select("div.standartbox a").mapNotNull {
            val ep_name    = it.selectFirst("h2")?.ownText()?.trim() ?: return@mapNotNull null
            val ep_href    = fixUrlNull(it.attr("href")) ?: return@mapNotNull null
            val ep_episode = Regex("""(\d+)\.Bölüm""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull()
            val ep_season  = Regex("""(\d+)\.Sezon""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            Episode(
                data    = ep_href,
                name    = ep_name,
                season  = ep_season,
                episode = ep_episode
            )
        }


        return newTvSeriesLoadResponse(title, url, TvType.AsianDrama, episodes) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("KRT", "data » ${data}")
        val document = app.get(data).document

        val iframes = mutableListOf<String>()

        document.select("div.filmcik div.tab-pane iframe").forEach {
            val iframe = fixUrlNull(it.attr("src")) ?: return@forEach
            iframes.add(iframe)
        }

        document.select("div.filmcik div.tab-pane a").forEach {
            val iframe = fixUrlNull(it.attr("href")) ?: return@forEach
            iframes.add(iframe)
        }

        iframes.forEach { iframe ->
            Log.d("KRT", "iframe » ${iframe}")
            loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        }

        return true
    }
}
