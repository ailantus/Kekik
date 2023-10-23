// ! https://github.com/Jacekun/cs3xxx-repo/blob/main/Pornhub/src/main/kotlin/com/jacekun/Pornhub.kt

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class PornHub : MainAPI() {
    override var mainUrl              = "https://www.pornhub.com"
    override var name                 = "PornHub"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = true
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "$mainUrl/video?o=mr&hd=1&page="           to "Recently Featured",
        "$mainUrl/video?o=cm&t=t&hd=1&page="       to "Newest",
        "$mainUrl/video?o=mv&t=t&hd=1&page="       to "Most Viewed",
        "$mainUrl/video?o=tr&t=t&hd=1&page="       to "Top Rated",
        "$mainUrl/video?o=ht&t=t&hd=1&page="       to "Hottest",
        "$mainUrl/video?o=lg&hd=1&page="           to "Longest",
        "$mainUrl/video?p=homemade&hd=1&page="     to "Homemade",
        "$mainUrl/video?p=professional&hd=1&page=" to "Professional"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("li.pcVideoListItem").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("a")?.attr("title") ?: return null
        val link      = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img.thumb")?.attr("src"))

        return newMovieSearchResponse(title, link, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/video/search?search=${query}").document

        return document.select("li.pcVideoListItem").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("h1.title span")?.text()?.trim() ?: return null
        val description = title
        val poster      = fixUrlNull(document.selectFirst("div.mainPlayerDiv img")?.attr("src"))
        val tags        = document.select("div.categoriesWrapper a").map { it?.text()?.trim().toString().replace(", ","") }

        val actors      = document.select("div.pornstarsWrapper a[data-label='Pornstar']")?.mapNotNull {
            Actor(it.text().trim(), it.select("img")?.attr("src"))
        }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("PHub", "url » $data")

        val source          = app.get(data).text
        val pattern         = """([^\"]*master.m3u8?.[^\"]*)""".toRegex()
        val match_result    = pattern.find(source)
        val extracted_value = match_result?.groups?.last()?.value ?: return false
        val m3u_link        = extracted_value?.replace("\\", "") ?: return false
        Log.d("PHub", "extracted_value » $extracted_value")
        Log.d("PHub", "m3u_link » $m3u_link")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = m3u_link,
                referer = "$mainUrl/",
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )

        return true
    }
}