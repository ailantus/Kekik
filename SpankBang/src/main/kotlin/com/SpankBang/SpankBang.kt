// ! https://github.com/phisher98/CXXX/blob/master/spankbang/src/main/kotlin/com/Spankbang/spankbang.kt

package com.coxju

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class SpankBang : MainAPI() {
    override var mainUrl              = "https://spankbang.com"
    override var name                 = "SpankBang"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "${mainUrl}/trending_videos/"           to "Trend",
        "${mainUrl}/upcoming/"                  to "Upcoming",
        "${mainUrl}/new_videos/"                to "New",
        "${mainUrl}/most_popular/"              to "Popular",
        "${mainUrl}/j2/channel/familyxxx/"      to "Family XXX",
        "${mainUrl}/ce/channel/bratty+milf/"    to "Bratty MILF",
        "${mainUrl}/cf/channel/bratty+sis/"     to "Bratty Sis",
        "${mainUrl}/j3/channel/hot+wife+xxx/"   to "Hot Wife XXX",
        "${mainUrl}/d6/channel/my+family+pies/" to "My Family Pies",
        "${mainUrl}/6l/channel/mylf/"           to "MYLF",
        "${mainUrl}/6d/channel/family+strokes/" to "Family Strokes",
        "${mainUrl}/6c/channel/teamskeet/"      to "TeamSkeet",
        "${mainUrl}/co/channel/moms+teach+sex/" to "Moms Teach",
        "${mainUrl}/np/channel/facials4k/"      to "FACIALS4K",
        "${mainUrl}/1q/channel/daddy4k/"        to "Daddy4K",
        "${mainUrl}/4w/channel/letsdoeit/"      to "LETSDOEIT",
        "${mainUrl}/o4/channel/touch+my+wife/"  to "Touch My Wife"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}/?o=popular&p=w&d=10").document      
        val home     = document.select("div.main_results div.video-item").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = fixTitle(this.select("div.name-and-menu-wrapper a")!!.text()) ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.select("picture img").attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (say in 1..5) {
            val document = app.get("${mainUrl}/s/${query}/${say}/?o=new&d=10").document
            val results  = document.select("div.main_results div.video-item").mapNotNull { it.toSearchResult() }

            if (!searchResponse.containsAll(results)) {
                searchResponse.addAll(results)
            } else {
                break
            }

            if (results.isEmpty()) break
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("meta[property=og:title]")?.attr("content")?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("meta[property='og:image']")?.attr("content"))
        val description     = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
        val year            = Regex("""\"uploadDate\":\s*\"(\d{4})""").find(document.html())?.groupValues?.get(1)?.toIntOrNull()
        val tags            = document.select("div.searches a").map { it.text() }
        val rating          = document.selectFirst("span.rate")?.text()?.trim()?.substringBefore("%")?.toRatingInt()?.div(10)
        val duration        = document.selectFirst("meta[property=og:duration]")?.attr("content")?.toIntOrNull()?.div(60)
        val recommendations = document.select("section.user_uploads div.video-item").mapNotNull { it.toSearchResult() }
        val actors          = document.select("li.primary_actions_container").map {
            Actor(it.selectFirst("span.name")!!.text(), fixUrlNull(it.selectFirst("img")?.attr("src")))
        }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl       = poster
            this.plot            = description
            this.year            = year
            this.tags            = tags
            this.rating          = rating
            this.duration        = duration
            this.recommendations = recommendations
            addActors(actors)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("SkBg", "data » ${data}")
        val document = app.get(data).document

        document.select("div#video_container").map { res ->
            val video_url = fixUrl(res.selectFirst("video > source")?.attr("src")?.trim().toString())
            Log.d("SkBg", "video_url » ${video_url}")

            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = video_url,
                    referer = data,
                    quality = Qualities.Unknown.value,
                    type    = INFER_TYPE
                )
            )
        }

        return true
    }
}