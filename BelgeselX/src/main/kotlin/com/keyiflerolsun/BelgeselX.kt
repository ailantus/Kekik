// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BelgeselX : MainAPI() {
    override var mainUrl              = "https://belgeselx.com"
    override var name                 = "BelgeselX"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/konu/turk-tarihi-belgeselleri&page=" to "Türk Tarihi Belgeselleri",
        "${mainUrl}/konu/tarih-belgeselleri&page="		 to "Tarih Belgeselleri",
        "${mainUrl}/konu/seyehat-belgeselleri&page="	 to "Seyehat Belgeselleri",
        "${mainUrl}/konu/seri-belgeseller&page="		 to "Seri Belgeseller",
        "${mainUrl}/konu/savas-belgeselleri&page="		 to "Savaş Belgeselleri",
        "${mainUrl}/konu/sanat-belgeselleri&page="		 to "Sanat Belgeselleri",
        "${mainUrl}/konu/psikoloji-belgeselleri&page="	 to "Psikoloji Belgeselleri",
        "${mainUrl}/konu/polisiye-belgeselleri&page="	 to "Polisiye Belgeselleri",
        "${mainUrl}/konu/otomobil-belgeselleri&page="	 to "Otomobil Belgeselleri",
        "${mainUrl}/konu/nazi-belgeselleri&page="		 to "Nazi Belgeselleri",
        "${mainUrl}/konu/muhendislik-belgeselleri&page=" to "Mühendislik Belgeselleri",
        "${mainUrl}/konu/kultur-din-belgeselleri&page="	 to "Kültür Din Belgeselleri",
        "${mainUrl}/konu/kozmik-belgeseller&page="		 to "Kozmik Belgeseller",
        "${mainUrl}/konu/hayvan-belgeselleri&page="		 to "Hayvan Belgeselleri",
        "${mainUrl}/konu/eski-tarih-belgeselleri&page="	 to "Eski Tarih Belgeselleri",
        "${mainUrl}/konu/egitim-belgeselleri&page="		 to "Eğitim Belgeselleri",
        "${mainUrl}/konu/dunya-belgeselleri&page="		 to "Dünya Belgeselleri",
        "${mainUrl}/konu/doga-belgeselleri&page="		 to "Doğa Belgeselleri",
        "${mainUrl}/konu/cizgi-film&page="				 to "Çizgi Film",
        "${mainUrl}/konu/bilim-belgeselleri&page="		 to "Bilim Belgeselleri"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("gen-movie-contain").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h3 a")?.text()?.trim() ?: return null
        val href      = fixUrlNull(this.selectFirst("h3 a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return mutableListOf<SearchResponse>()
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("h2.gen-title")?.text()?.trim() ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.gen-tv-show-top img")?.attr("src")) ?: return null
        val description = document.selectFirst("div.gen-single-tv-show-info p")?.text()?.trim()


        val episodes = document.select("div.tab-pane").mapNotNull {
            val ep_name     = it.selectFirst("div.gen-movie-info h3 a")?.text()?.trim() ?: return@mapNotNull null
            val ep_href     = fixUrlNull(it.selectFirst("div.gen-movie-info h3 a")?.attr("href")) ?: return@mapNotNull null

            val season_name = it.selectFirst("div.gen-single-meta-holder ul li")?.text()?.trim() ?: ""
            val ep_episode  = Regex("""Bölüm (\d+)""").find(season_name)?.groupValues?.get(1)?.toIntOrNull()
            val ep_season   = Regex("""Sezon (\d+)""").find(season_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            Episode(
                data    = ep_href,
                name    = ep_name,
                season  = ep_season,
                episode = ep_episode
            )
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("BLX", "data » ${data}")
        val i_source = app.get(data)

        Regex("""<iframe\s+[^>]*src=\\\"([^\\\"']+)\\\"""").findAll(i_source.text).forEach { matchResult ->
            val iframe = matchResult.groupValues[1]
            Log.d("BLX", "iframe » ${iframe}")

            loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        }

        return true
    }
}
