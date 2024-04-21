// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import android.util.Base64
import org.jsoup.Jsoup

class KultFilmler : MainAPI() {
    override var mainUrl              = "https://kultfilmler.com"
    override var name                 = "KultFilmler"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/page/"                                      to "Son Filmler",
        "${mainUrl}/category/aile-filmleri-izle/page/"		    to "Aile",
        "${mainUrl}/category/aksiyon-filmleri-izle/page/"	    to "Aksiyon",
        "${mainUrl}/category/animasyon-filmleri-izle/page/"	    to "Animasyon",
        "${mainUrl}/category/belgesel-izle/page/"			    to "Belgesel",
        "${mainUrl}/category/bilim-kurgu-filmleri-izle/page/"   to "Bilim Kurgu",
        "${mainUrl}/category/biyografi-filmleri-izle/page/"	    to "Biyografi",
        "${mainUrl}/category/dram-filmleri-izle/page/"		    to "Dram",
        "${mainUrl}/category/fantastik-filmleri-izle/page/"	    to "Fantastik",
        "${mainUrl}/category/gerilim-filmleri-izle/page/"	    to "Gerilim",
        "${mainUrl}/category/gizem-filmleri-izle/page/"		    to "Gizem",
        "${mainUrl}/category/kara-filmleri-izle/page/"		    to "Kara",
        "${mainUrl}/category/kisa-film-izle/page/"			    to "Kısa Metrajlı",
        "${mainUrl}/category/komedi-filmleri-izle/page/"		to "Komedi",
        "${mainUrl}/category/korku-filmleri-izle/page/"		    to "Korku",
        "${mainUrl}/category/macera-filmleri-izle/page/"		to "Macera",
        "${mainUrl}/category/muzik-filmleri-izle/page/"		    to "Müzik",
        "${mainUrl}/category/polisiye-filmleri-izle/page/"	    to "Polisiye",
        "${mainUrl}/category/politik-filmleri-izle/page/"	    to "Politik",
        "${mainUrl}/category/romantik-filmleri-izle/page/"	    to "Romantik",
        "${mainUrl}/category/savas-filmleri-izle/page/"		    to "Savaş",
        "${mainUrl}/category/spor-filmleri-izle/page/"		    to "Spor",
        "${mainUrl}/category/suc-filmleri-izle/page/"		    to "Suç",
        "${mainUrl}/category/tarih-filmleri-izle/page/"		    to "Tarih",
        "${mainUrl}/category/yerli-filmleri-izle/page/"		    to "Yerli"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("div.movie-box").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.name a")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("div.name a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("div.img img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}?s=${query}").document

        return document.select("div.movie-box").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("div.film h1")?.text()?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("[property='og:image']")?.attr("content"))
        val description     = document.selectFirst("div.description")?.text()?.trim()
        val tags            = document.select("ul.post-categories a").map { it.text() }
        val rating          = document.selectFirst("div.imdb-count")?.text()?.trim()?.split(" ")?.first()?.toRatingInt()
        val year            = Regex("""(\d+)""").find(document.selectFirst("li.release")?.text()?.trim() ?: "")?.groupValues?.get(1)?.toIntOrNull()
        val duration        = Regex("""(\d+)""").find(document.selectFirst("li.time")?.text()?.trim() ?: "")?.groupValues?.get(1)?.toIntOrNull()
        val recommendations = document.select("div.movie-box").mapNotNull { it.toSearchResult() }
        val actors          = document.select("[href*='oyuncular']").map {
            Actor(it.text())
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.rating          = rating
            this.duration        = duration
            this.recommendations = recommendations
            addActors(actors)
        }
    }

    private fun getIframe(source_code: String): String {
        val atob_key = Regex("""PHA\+[0-9a-zA-Z+\/=]*""").find(source_code)?.groupValues?.get(1) ?: return ""

        return Jsoup.parse(String(Base64.decode(atob_key, Base64.DEFAULT))).selectFirst("iframe")?.attr("src") ?: ""
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("KLT", "data » ${data}")
        val document = app.get(data).document

        if (document.selectFirst("div#action-parts a") != null) {
            document.select("div#action-parts div.part").forEach {
                var sub_iframe:String
                val sub_url = fixUrlNull(it.parent()?.attr("href"))

                if (sub_url == null) {
                    sub_iframe  = fixUrlNull(getIframe(document.html())) ?: return@forEach
                } else {
                    val sub_doc = app.get(sub_url).document
                    sub_iframe  = fixUrlNull(getIframe(sub_doc.html())) ?: return@forEach
                }

                Log.d("KLT", "sub_iframe » ${sub_iframe}")
                loadExtractor(sub_iframe, "${mainUrl}/", subtitleCallback) { link ->
                    callback.invoke(
                        ExtractorLink(
                            source        = "${it.selectFirst("div.part-lang span")?.attr("title")?.trim()} - ${link.name}",
                            name          = "${it.selectFirst("div.part-lang span")?.attr("title")?.trim()} - ${link.name}",
                            url           = link.url,
                            referer       = link.referer,
                            quality       = link.quality,
                            headers       = link.headers,
                            extractorData = link.extractorData,
                            type          = link.type
                        )
                    )
                }
            }
        } else {
            val iframe = fixUrlNull(getIframe(document.html())) ?: return false
            Log.d("KLT", "iframe » ${iframe}")
            loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        }

        return true
    }
}
