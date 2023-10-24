// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities


class Dizilla : MainAPI() {
    override var mainUrl            = "https://dizilla.club"
    override var name               = "Dizilla"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = true
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    // ! CloudFlare bypass
    override var sequentialMainPage = true
    // override var sequentialMainPageDelay       = 250L // ? 0.25 saniye
    // override var sequentialMainPageScrollDelay = 250L // ? 0.25 saniye

    override val mainPage =
        mainPageOf(
            "$mainUrl/arsiv?s=&ulke=&tur=15&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="  to "Aile",
            "$mainUrl/arsiv?s=&ulke=&tur=9&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Aksiyon",
            "$mainUrl/arsiv?s=&ulke=&tur=5&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Bilim Kurgu",
            "$mainUrl/arsiv?s=&ulke=&tur=6&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Biyografi",
            "$mainUrl/arsiv?s=&ulke=&tur=2&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Dram",
            "$mainUrl/arsiv?s=&ulke=&tur=12&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="  to "Fantastik",
            "$mainUrl/arsiv?s=&ulke=&tur=18&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="  to "Gerilim",
            "$mainUrl/arsiv?s=&ulke=&tur=4&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Komedi",
            "$mainUrl/arsiv?s=&ulke=&tur=8&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Korku",
            "$mainUrl/arsiv?s=&ulke=&tur=7&year_start=&year_end=&imdb_start=&imdb_end=&language=&orders=desc&orderby=imdb&page="   to "Romantik"
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("span.watchlistitem-").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span.text-white")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a[href*='/dizi/']")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    data class SearchResult(
        @JsonProperty("data") val data: SearchData?
    )
    
    data class SearchData(
        @JsonProperty("state")   val state: Boolean? = null,
        @JsonProperty("result")  val result: List<SearchItem>? = arrayListOf(),
        @JsonProperty("message") val message: String? = null,
        @JsonProperty("html")    val html: String? = null
    )
    
    data class SearchItem(
        @JsonProperty("used_slug")         val slug: String? = null,
        @JsonProperty("object_name")       val title: String? = null,
        @JsonProperty("object_poster_url") val poster: String? = null,
    )

    private fun SearchItem.toSearchResponse(): SearchResponse? {
        return newTvSeriesSearchResponse(
            title ?: return null,
            "$mainUrl/$slug",
            TvType.TvSeries,
        ) {
            this.posterUrl = poster
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val main_page = app.get(mainUrl).document
        val c_key     = main_page.selectFirst("input[name='cKey']")?.attr("value") ?: return emptyList()
        val c_value   = main_page.selectFirst("input[name='cValue']")?.attr("value") ?: return emptyList()

        return app.post(
            "$mainUrl/bg/searchcontent",
            data    = mapOf(
                "cKey"       to c_key,
                "cValue"     to c_value,
                "searchterm" to query
            ),
            headers = mapOf(
                "Accept"           to "application/json, text/javascript, */*; q=0.01",
                "X-Requested-With" to "XMLHttpRequest"
            ),
            referer = "$mainUrl/"
        ).parsedSafe<SearchResult>()
        ?.data?.result
        ?.mapNotNull { search_item -> search_item.toSearchResponse() }
        ?: throw ErrorLoadingException("Invalid Json response")        
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.w-full h1")?.text() ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.w-full img")?.attr("src")) ?: return null
        val year        = document.select("div.gap-3 span.text-sm")?.get(0)?.text()?.trim()?.toIntOrNull()
        val description = document.selectFirst("div.mv-det-p")?.text()?.trim()
        val tags        = document.select("[href*='dizi-turu']").map { it.text() }
        val rating      = document.selectFirst("a[href*='imdb.com'] span")?.text()?.split(".")?.first()?.trim()?.toIntOrNull()
        val duration    = Regex("(\\d+)").find(document.select("div.gap-3 span.text-sm").get(1)?.text() ?: "")?.value?.toIntOrNull()

        val actors      = document.select("[href*='oyuncu']").map {
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

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year      = year
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
            this.duration  = duration
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZL", "data » $data")
            val document   = app.get(data).document
            val raw_iframe = document.selectFirst("div#playerLsDizilla iframe")?.attr("src") ?: return false
            val iframe     = raw_iframe.substringAfter("//")

            if (iframe.startsWith("contentx.me") || iframe.startsWith("hotlinger.com")) {
                val i_source  = app.get("https://$iframe", referer="$mainUrl/").text
                val i_extract = Regex("""window\.openPlayer\('([^']+)'""").find(i_source)?.groups?.get(1)?.value ?: return false

                val vid_source  = app.get("https://contentx.me/source2.php?v=$i_extract", referer="$mainUrl/").text
                val vid_extract = Regex("""file\":\"([^\"]+)""").find(vid_source)?.groups?.get(1)?.value ?: return false
                val m3u_link    = vid_extract?.replace("\\", "") ?: return false

                callback.invoke(
                    ExtractorLink(
                        source  = this.name,
                        name    = this.name,
                        url     = m3u_link,
                        referer = "https://$iframe",
                        quality = Qualities.Unknown.value,
                        isM3u8  = true
                    )
                )

                return true
            } else {
                Log.d("DZL", "iframe » $iframe")
                return false
            }
    }
}
