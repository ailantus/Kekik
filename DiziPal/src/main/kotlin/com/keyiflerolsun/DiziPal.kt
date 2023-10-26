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
            "$mainUrl/diziler?kelime=&durum=&tur=1&type=&siralama="  to "Aile",
            "$mainUrl/diziler?kelime=&durum=&tur=2&type=&siralama="  to "Aksiyon",
            "$mainUrl/diziler?kelime=&durum=&tur=3&type=&siralama="  to "Animasyon",
            "$mainUrl/diziler?kelime=&durum=&tur=26&type=&siralama=" to "Anime",
            "$mainUrl/diziler?kelime=&durum=&tur=4&type=&siralama="  to "Belgesel",
            "$mainUrl/diziler?kelime=&durum=&tur=5&type=&siralama="  to "Bilimkurgu",
            "$mainUrl/diziler?kelime=&durum=&tur=6&type=&siralama="  to "Biyografi",
            "$mainUrl/diziler?kelime=&durum=&tur=7&type=&siralama="  to "Dram",
            "$mainUrl/diziler?kelime=&durum=&tur=28&type=&siralama=" to "Editörün Seçtikleri",
            "$mainUrl/diziler?kelime=&durum=&tur=25&type=&siralama=" to "Erotik",
            "$mainUrl/diziler?kelime=&durum=&tur=8&type=&siralama="  to "Fantastik",
            "$mainUrl/diziler?kelime=&durum=&tur=9&type=&siralama="  to "Gerilim",
            "$mainUrl/diziler?kelime=&durum=&tur=10&type=&siralama=" to "Gizem",
            "$mainUrl/diziler?kelime=&durum=&tur=11&type=&siralama=" to "Komedi",
            "$mainUrl/diziler?kelime=&durum=&tur=12&type=&siralama=" to "Korku",
            "$mainUrl/diziler?kelime=&durum=&tur=13&type=&siralama=" to "Macera",
            "$mainUrl/diziler?kelime=&durum=&tur=27&type=&siralama=" to "Mubi",
            "$mainUrl/diziler?kelime=&durum=&tur=14&type=&siralama=" to "Müzik",
            "$mainUrl/diziler?kelime=&durum=&tur=16&type=&siralama=" to "Romantik",
            "$mainUrl/diziler?kelime=&durum=&tur=17&type=&siralama=" to "Savaş",
            "$mainUrl/diziler?kelime=&durum=&tur=18&type=&siralama=" to "Spor",
            "$mainUrl/diziler?kelime=&durum=&tur=19&type=&siralama=" to "Suç",
            "$mainUrl/diziler?kelime=&durum=&tur=20&type=&siralama=" to "Tarih",
            "$mainUrl/diziler?kelime=&durum=&tur=21&type=&siralama=" to "Western",
            "$mainUrl/diziler?kelime=&durum=&tur=24&type=&siralama=" to "Yerli",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
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

        val title       = document.selectFirst("div.cover h5")?.text() ?: return null
        val cover_style = document.selectFirst("div.cover")?.attr("style") ?: return null
        val poster      = Regex("""url\(['"]?(.*?)['"]?\)""").find(cover_style)?.groups?.get(1)?.value ?: return null

        val year        = document.selectXpath("//div[text()='Yapım Yılı']//following-sibling::div")?.text()?.trim()?.toIntOrNull()
        val description = document.selectFirst("div.summary p")?.text()?.trim()
        val tags        = document.selectXpath("//div[text()='Türler']//following-sibling::div")?.text()?.trim()?.split(" ")?.mapNotNull { it.trim() }
        val rating      = document.selectFirst("//div[text()='IMDB Puanı']//following-sibling::div")?.text()?.split(".")?.first()?.trim()?.toIntOrNull()
        val duration    = Regex("(\\d+)").find(document.selectXpath("//div[text()='Ortalama Süre']//following-sibling::div")?.text() ?: "")?.value?.toIntOrNull()

        val episodes    = document.select("div.episode-item").mapNotNull {
            val ep_name    = it.selectFirst("div.name")?.text()?.trim() ?: return@mapNotNull null
            val ep_href    = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            // val ep_description = it.selectFirst("span.t-content")?.text()?.trim()
            val ep_episode = it.selectFirst("div.episode")?.text()?.trim()?.split(" ")?.get(2)?.replace(".", "")?.toIntOrNull()
            val ep_season  = it.selectFirst("div.episode")?.text()?.trim()?.split(" ")?.get(0)?.replace(".", "")?.toIntOrNull()

            Episode(
                data        = ep_href,
                name        = ep_name,
                season      = ep_season,
                episode     = ep_episode,
                posterUrl   = null,
                rating      = null,
                // description = ep_description,
                date        = null
            )
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year      = year
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
            this.duration  = duration
        }
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
