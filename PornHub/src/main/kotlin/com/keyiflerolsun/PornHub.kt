// ! https://github.com/Jacekun/cs3xxx-repo/blob/main/Pornhub/src/main/kotlin/com/jacekun/Pornhub.kt

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class PornHub : MainAPI() {
    override var mainUrl              = "https://www.pornhub.com"
    override var name                 = "PornHub"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "$mainUrl/video?o=mr&hd=1&page="           to "Recently Featured",
        "$mainUrl/video?o=tr&t=w&hd=1&page="       to "Top Rated",
        "$mainUrl/video?o=mv&t=w&hd=1&page="       to "Most Viewed",
        "$mainUrl/video?o=ht&t=w&hd=1&page="       to "Hottest",
        "$mainUrl/video?p=professional&hd=1&page=" to "Professional",
        "$mainUrl/video?o=lg&hd=1&page="           to "Longest",
        "$mainUrl/video?p=homemade&hd=1&page="     to "Homemade",
        "$mainUrl/video?o=cm&t=w&hd=1&page="       to "Newest",
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

        val title           = document.selectFirst("h1.title span[class='inlineFree']")?.text()?.trim() ?: return null
        val description     = title
        val poster          = fixUrlNull(document.selectFirst("div.mainPlayerDiv img")?.attr("src"))
        val tags            = document.select("div.categoriesWrapper a[data-label='Category']").map { it?.text()?.trim().toString().replace(", ","") }
        val duration        = Regex("duration' : '(.*)',").find(document.html())?.groups?.get(1)?.value?.toIntOrNull()

        val recommendations = document.selectXpath("//a[contains(@class, 'img')]").mapNotNull {
            val recName      = it?.attr("title")?.trim() ?: return@mapNotNull null
            val recHref      = fixUrlNull(it?.attr("href")) ?: return@mapNotNull null
            val recPosterUrl = fixUrlNull(it.selectFirst("img")?.attr("src"))
            newMovieSearchResponse(recName, recHref, TvType.NSFW) {
                this.posterUrl = recPosterUrl
            }
        }

        val actors          = document.select("div.pornstarsWrapper a[data-label='Pornstar']")?.mapNotNull {
            Actor(it.text().trim(), it.select("img")?.attr("src"))
        }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl       = poster
            this.plot            = description
            this.tags            = tags
            this.duration        = duration
            this.recommendations = recommendations
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
        val extracted_value = Regex("""([^\"]*master.m3u8?.[^\"]*)""").find(source)?.groups?.last()?.value ?: return false
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