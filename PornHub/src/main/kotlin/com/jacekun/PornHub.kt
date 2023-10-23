// ! https://github.com/Jacekun/cs3xxx-repo/blob/main/Pornhub/src/main/kotlin/com/jacekun/Pornhub.kt

package com.jacekun

import android.util.Log
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.network.WebViewResolver
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class PornHub : MainAPI() {
    override var mainUrl              = "https://www.pornhub.com"
    override var name                 = "PornHub"
    override val hasMainPage          = true
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val vpnStatus            = VPNStatus.MightBeNeeded //Cause it's a big site
    override val supportedTypes       = setOf(TvType.NSFW)
    private val globalTvType          = TvType.NSFW

    override val mainPage = mainPageOf(
        "$mainUrl/video?page="            to "Main Page",
        "$mainUrl/video?o=cm&page="       to "Newest",
        "$mainUrl/video?o=ht&page="       to "Hottest",
        "$mainUrl/video?o=mv&page="       to "Most Viewed",
        "$mainUrl/video?o=tr&page="       to "Top Rated",
        "$mainUrl/video?p=homemade&page=" to "Popular Homemade"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        try {
            val categoryData = request.data
            val categoryName = request.name
            val pagedLink = if (page > 0) categoryData + page else categoryData
            val soup = app.get(pagedLink).document
            val home = soup.select("div.sectionWrapper div.wrap").mapNotNull {
                if (it == null) { return@mapNotNull null }
                val title = it.selectFirst("span.title a")?.text() ?: ""
                val link = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
                val img = fixUrlNull(it.selectFirst("img.thumb")?.attr("src"))
                MovieSearchResponse(
                    name = title,
                    url = link,
                    apiName = this.name,
                    type = globalTvType,
                    posterUrl = img
                )
            }
            if (home.isNotEmpty()) {
                return newHomePageResponse(
                    list = HomePageList(
                        name = categoryName,
                        list = home,
                        isHorizontalImages = true
                    ),
                    hasNext = true
                )
            } else {
                throw ErrorLoadingException("No homepage data found!")
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            logError(e)
        }
        throw ErrorLoadingException()
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/video/search?search=${query}"
        val document = app.get(url).document
        return document.select("div.sectionWrapper div.wrap").mapNotNull {
            if (it == null) { return@mapNotNull null }
            val title = it.selectFirst("span.title a")?.text() ?: return@mapNotNull null
            val link = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val image = fixUrlNull(it.selectFirst("img.thumb")?.attr("src"))
            MovieSearchResponse(
                name = title,
                url = link,
                apiName = this.name,
                type = globalTvType,
                posterUrl = image
            )
        }.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        val soup = app.get(url).document
        val title = soup.selectFirst(".title span")?.text() ?: ""
        val poster: String? = soup.selectFirst("div.video-wrapper .mainPlayerDiv img")?.attr("src") ?:
        soup.selectFirst("head meta[property=og:image]")?.attr("content")
        val tags = soup.select("div.categoriesWrapper a")
            .map { it?.text()?.trim().toString().replace(", ","") }
        return MovieLoadResponse(
            name = title,
            url = url,
            apiName = this.name,
            type = globalTvType,
            dataUrl = url,
            posterUrl = poster,
            tags = tags,
            plot = title
        )
    }
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("PHub", "_url » $data")

        val source          = app.get(data).text
        val pattern         = """([^\"]*master.m3u8?.[^\"]*)""".toRegex()
        val match_result    = pattern.find(source)
        val extracted_value = match_result?.groups?.last()?.value?.trim()?.replace("\\", "") ?: return false
        Log.d("PHub", "_extracted_value » $extracted_value")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = extracted_value,
                referer = "$mainUrl/",
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )
        return true

        // app.get(
        //     url = data,
        //     interceptor = WebViewResolver(
        //         Regex("(master\\.m3u8\\?.*)")
        //     )
        // ).let { response ->
        //     M3u8Helper().m3u8Generation(
        //         M3u8Helper.M3u8Stream(
        //             response.url,
        //             headers = response.headers.toMap()
        //         ), true
        //     ).apmap { stream ->
        //         callback(
        //             ExtractorLink(
        //                 source = name,
        //                 name = "${this.name} m3u8",
        //                 url = stream.streamUrl,
        //                 referer = mainUrl,
        //                 quality = getQualityFromName(stream.quality?.toString()),
        //                 isM3u8 = true
        //             )
        //         )
        //     }
        // }
        // return true
    }

    private fun fetchImgUrl(imgsrc: Element?): String? {
        return try { imgsrc?.attr("data-src")
            ?: imgsrc?.attr("data-mediabook")
            ?: imgsrc?.attr("alt")
            ?: imgsrc?.attr("data-mediumthumb")
            ?: imgsrc?.attr("data-thumb_url")
            ?: imgsrc?.attr("src")
        } catch (e:Exception) { null }
    }
}