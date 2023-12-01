// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class CizgiMax : MainAPI() {
    override var mainUrl              = "https://cizgimax.online"
    override var name                 = "CizgiMax"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Cartoon)

    override val mainPage = mainPageOf(
        "${mainUrl}/category/genel/aile/page/"        to "Aile",
        "${mainUrl}/category/genel/aksyion/page/"     to "Aksyion",
        "${mainUrl}/category/genel/bilim-kurgu/page/" to "Bilim Kurgu",
        "${mainUrl}/category/genel/fantastik/page/"   to "Fantastik",
        "${mainUrl}/category/genel/komedi/page/"      to "Komedi",
        "${mainUrl}/category/genel/korku/page/"       to "Korku",
        "${mainUrl}/category/genel/macera/page/"      to "Macera",
        "${mainUrl}/category/genel/tarih/page/"       to "Tarih"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}/?sort=views").document
        val home     = document.select("div.movie-preview-content").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span.movie-title")?.text()?.substringBefore(" Türkçe İzle") ?: return null
        val href      = fixUrlNull(this.selectFirst("span.movie-title a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("div.movie-poster img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.Cartoon) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/?s=${query}").document

        return document.select("div.movie-preview-content").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.title h1")?.text()?.substringBefore(" Türkçe İzle") ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.poster img")?.attr("src")) ?: return null
        val description = document.selectFirst("div.excerpt")?.text()?.trim()
        val tags        = document.select("div.categories a").mapNotNull { it?.text()?.trim() }
        val rating      = document.selectFirst("span.imdb-rating")?.text()?.substringBefore("IMDB Puanı")?.trim()?.toRatingInt()


        val first_ep_name  = document.selectFirst("div.active div.part-name")?.text()?.trim() ?: "Filmi İzle"
        val first_episode  = Episode(
            data    = url,
            name    = first_ep_name,
            season  = Regex("""S(\d+) BÖLÜM""").find(first_ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1,
            episode = Regex("""BÖLÜM (\d+)""").find(first_ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        )
        val other_episodes = document.select("a.post-page-numbers").mapNotNull {
            val ep_name    = it.selectFirst("div.part-name")?.text()?.trim() ?: return@mapNotNull null
            val ep_href    = fixUrlNull(it.attr("href")) ?: return@mapNotNull null
            val ep_episode = Regex("""BÖLÜM (\d+)""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull()
            val ep_season  = Regex("""S(\d+) BÖLÜM""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            Episode(
                data    = ep_href,
                name    = ep_name,
                season  = ep_season,
                episode = ep_episode
            )
        }
        val episodes       = mutableListOf(first_episode)
        episodes.addAll(other_episodes)


        return newTvSeriesLoadResponse(title, url, TvType.Cartoon, episodes) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("CZGM", "data » ${data}")
        val document = app.get(data).document
        val iframe   = document.selectFirst("div.video-content iframe")?.attr("src") ?: return false
        Log.d("CZGM", "iframe » ${iframe}")

        loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)

        return true
    }
}
