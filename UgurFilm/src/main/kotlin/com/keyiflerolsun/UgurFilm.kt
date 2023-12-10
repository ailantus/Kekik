// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class UgurFilm : MainAPI() {
    override var mainUrl              = "https://ugurfilm7.com"
    override var name                 = "UgurFilm"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/turkce-altyazili-filmler/page/" to "Türkçe Altyazılı Filmler",
        "${mainUrl}/yerli-filmler/page/"            to "Yerli Filmler",
        "${mainUrl}/category/kisa-film/page/"       to "Kısa Film",
        "${mainUrl}/category/kara-film/page/"       to "Kara Film",
        "${mainUrl}/category/bilim-kurgu/page/"     to "Bilim Kurgu",
        "${mainUrl}/category/belgesel/page/"        to "Belgesel",
        "${mainUrl}/category/erotik/page/"          to "Erotik",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("div.icerik div").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span:nth-child(1)")?.text()?.trim() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/?s=${query}").document

        return document.select("div.icerik div").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.bilgi h2")?.text()?.trim() ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.resim img")?.attr("src"))
        val year        = document.selectFirst("a[href*='/yil/']")?.text()?.trim()?.toIntOrNull()
        val description = document.selectFirst("div.slayt-aciklama")?.text()?.trim()
        val tags        = document.select("p.tur a[href*='/category/']").map { it.text() }
        val rating      = document.selectFirst("span.puan")?.text()?.split(" ")?.last()?.toRatingInt()
        val duration    = document.selectXpath("//span[contains(text(), 'Süre:')]//following-sibling::b").text().split(" ").get(0).trim().toIntOrNull()
        val actors      = document.select("li.oyuncu-k").map {
            Actor(it.selectFirst("span")!!.text(), it.selectFirst("img")?.attr("src"))
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.year      = year
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
            this.duration  = duration
            addActors(actors)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("UGF", "data » ${data}")
        val document = app.get(data).document
        var iframe   = fixUrlNull(document.selectFirst("div#vast iframe")?.attr("src")) ?: return false
        Log.d("UGF", "iframe » ${iframe}")

        if (iframe.contains("${mainUrl}")) {
            val vid_id = iframe.substringAfter("/play.php?vid=").trim()
            Log.d("UGF", "vid_id » ${vid_id}")

            val player_api = app.post(
                "${mainUrl}/wp-admin/admin-ajax.php",
                data = mapOf(
                    "vid"         to vid_id,
                    "alternative" to "vidmoly",
                    "ord"         to "0"
                )
            ).text
            val player_data = AppUtils.tryParseJson<AjaxSource>(player_api) ?: return false
            Log.d("UGF", "player_data » ${player_data}")

            loadExtractor(player_data.iframe, "${mainUrl}/", subtitleCallback, callback)
        } else {
            loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        }

        return true
    }
}