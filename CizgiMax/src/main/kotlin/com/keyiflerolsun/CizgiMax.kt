// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.Qualities


class CizgiMax : MainAPI() {
    override var mainUrl            = "https://cizgimax.online"
    override var name               = "CizgiMax"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = false
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    override val mainPage =
        mainPageOf(
            "$mainUrl/category/genel/aile/page/"        to "Aile",
            "$mainUrl/category/genel/aksyion/page/"     to "Aksyion",
            "$mainUrl/category/genel/bilim-kurgu/page/" to "Bilim Kurgu",
            "$mainUrl/category/genel/fantastik/page/"   to "Fantastik",
            "$mainUrl/category/genel/komedi/page/"      to "Komedi",
            "$mainUrl/category/genel/korku/page/"       to "Korku",
            "$mainUrl/category/genel/macera/page/"      to "Macera",
            "$mainUrl/category/genel/tarih/page/"       to "Tarih"
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page + "/?sort=views").document
        val home     = document.select("div.movie-preview-content").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span.movie-title")?.text()?.substringBefore(" Türkçe İzle") ?: return null
        val href      = fixUrlNull(this.selectFirst("span.movie-title a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("div.movie-poster img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document

        return document.select("div.movie-preview-content").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.title h1")?.text() ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.poster img")?.attr("src")) ?: return null
        val description = document.selectFirst("div.excerpt")?.text()?.trim()
        val tags        = document.select("div.categories a").mapNotNull { it?.text()?.trim() }
        val rating      = document.selectFirst("span.imdb-rating")?.text()?.substringBefore("IMDB Puanı")?.trim()?.toRatingInt()


        val first_episode  = Episode(
            data        = url,
            name        = "BÖLÜM 1",
            season      = 1,
            episode     = 1,
            posterUrl   = null,
            rating      = null,
            date        = null
        )
        val other_episodes = document.select("a.post-page-numbers").mapNotNull {
            val ep_name    = it.selectFirst("div.part-name")?.text()?.trim() ?: return@mapNotNull null
            val ep_href    = fixUrlNull(it.attr("href")) ?: return@mapNotNull null
            val ep_episode = Regex("""BÖLÜM (\d+)""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull()
            val ep_season  = Regex("""S(\d+) BÖLÜM""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            Episode(
                data        = ep_href,
                name        = ep_name,
                season      = ep_season,
                episode     = ep_episode,
                posterUrl   = null,
                rating      = null,
                date        = null
            )
        }
        val episodes       = mutableListOf(first_episode)
        episodes.addAll(other_episodes)


        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("CizgiMax", "data » $data")
            val document = app.get(data).document
            val iframe   = document.selectFirst("div.video-content iframe")?.attr("src") ?: return false
            Log.d("CizgiMax", "iframe » $iframe")

            var i_source: String? = null
            var m3u_link: String? = null

            if (iframe.contains("sibnet.ru")) {
                i_source      = app.get("$iframe", referer="$mainUrl/").text
                m3u_link      = Regex("""player.src\(\[\{src: \"([^\"]+)""").find(i_source)?.groupValues?.get(1)
                if (m3u_link != null) {
                    m3u_link = "https://video.sibnet.ru${m3u_link}"
                }
            }

            Log.d("CizgiMax", "m3u_link » $m3u_link")
            if (m3u_link == null) {
                Log.d("CizgiMax", "i_source » $i_source")
                return false
            }

            M3u8Helper.generateM3u8(
                source    = this.name,
                name      = this.name,
                streamUrl = m3u_link,
                referer   = iframe
            ).forEach(callback)

            // callback.invoke(
            //     ExtractorLink(
            //         source  = this.name,
            //         name    = this.name,
            //         url     = m3u_link,
            //         referer = iframe,
            //         quality = Qualities.Unknown.value,
            //         isM3u8  = m3u_link.contains(".m3u8")
            //     )
            // )

            return true
    }
}
