// ! https://github.com/Jacekun/cs3xxx-repo/blob/main/Pornhub/src/main/kotlin/com/jacekun/Pornhub.kt

package com.jacekun

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
        "$mainUrl/video?page="                       to "Main Page",
        "$mainUrl/video?c=105&page="                 to "60FPS",
        "$mainUrl/video?c=3&page="                   to "Amateur",
        "$mainUrl/video?c=35&page="                  to "Anal",
        "$mainUrl/video?c=98&page="                  to "Arab",
        "$mainUrl/video?c=1&page="                   to "Asian",
        "$mainUrl/categories/babe&page="             to "Babe",
        "$mainUrl/video?c=89&page="                  to "Babysitter (18+)",
        "$mainUrl/video?c=6&page="                   to "BBW",
        "$mainUrl/video?c=141&page="                 to "Behind The Scenes",
        "$mainUrl/video?c=4&page="                   to "Big Ass",
        "$mainUrl/video?c=7&page="                   to "Big Dick",
        "$mainUrl/video?c=8&page="                   to "Big Tits",
        "$mainUrl/video?c=76&page="                  to "Bisexual Male",
        "$mainUrl/video?c=9&page="                   to "Blonde",
        "$mainUrl/video?c=13&page="                  to "Blowjob",
        "$mainUrl/video?c=10&page="                  to "Bondage",
        "$mainUrl/video?c=102&page="                 to "Brazilian",
        "$mainUrl/video?c=96&page="                  to "British",
        "$mainUrl/video?c=11&page="                  to "Brunette",
        "$mainUrl/video?c=14&page="                  to "Bukkake",
        "$mainUrl/video?c=86&page="                  to "Cartoon",
        "$mainUrl/video?c=90&page="                  to "Casting",
        "$mainUrl/video?c=12&page="                  to "Celebrity",
        "$mainUrl/video?c=732&page="                 to "Closed Captions",
        "$mainUrl/categories/college&page="          to "College (18+)",
        "$mainUrl/video?c=57&page="                  to "Compilation",
        "$mainUrl/video?c=241&page="                 to "Cosplay",
        "$mainUrl/video?c=15&page="                  to "Creampie",
        "$mainUrl/video?c=242&page="                 to "Cuckold",
        "$mainUrl/video?c=16&page="                  to "Cumshot",
        "$mainUrl/video?c=100&page="                 to "Czech",
        "$mainUrl/described-video&page="             to "Described Video",
        "$mainUrl/video?c=72&page="                  to "Double Penetration",
        "$mainUrl/video?c=17&page="                  to "Ebony",
        "$mainUrl/video?c=55&page="                  to "Euro",
        "$mainUrl/video?c=115&page="                 to "Exclusive",
        "$mainUrl/video?c=93&page="                  to "Feet",
        "$mainUrl/video?c=502&page="                 to "Female Orgasm",
        "$mainUrl/video?c=18&page="                  to "Fetish",
        "$mainUrl/video?c=592&page="                 to "Fingering",
        "$mainUrl/video?c=19&page="                  to "Fisting",
        "$mainUrl/video?c=94&page="                  to "French",
        "$mainUrl/video?c=32&page="                  to "Funny",
        "$mainUrl/video?c=80&page="                  to "Gangbang",
        "$mainUrl/video?c=95&page="                  to "German",
        "$mainUrl/video?c=20&page="                  to "Handjob",
        "$mainUrl/video?c=21&page="                  to "Hardcore",
        "$mainUrl/hd&page="                     	 to "HD Porn",
        "$mainUrl/categories/hentai&page="           to "Hentai",
        "$mainUrl/video?c=101&page="                 to "Indian",
        "$mainUrl/interactive&page="                 to "Interactive",
        "$mainUrl/video?c=25&page="                  to "Interracial",
        "$mainUrl/video?c=97&page="                  to "Italian",
        "$mainUrl/video?c=111&page="                 to "Japanese",
        "$mainUrl/video?c=103&page="                 to "Korean",
        "$mainUrl/video?c=26&page="                  to "Latina",
        "$mainUrl/video?c=27&page="                  to "Lesbian",
        "$mainUrl/video?c=78&page="                  to "Massage",
        "$mainUrl/video?c=22&page="                  to "Masturbation",
        "$mainUrl/video?c=28&page="                  to "Mature",
        "$mainUrl/video?c=29&page="                  to "MILF",
        "$mainUrl/video?c=512&page="                 to "Muscular Men",
        "$mainUrl/video?c=121&page="                 to "Music",
        "$mainUrl/video?c=181&page="                 to "Old/Young (18+)",
        "$mainUrl/video?c=2&page="                   to "Orgy",
        "$mainUrl/video?c=201&page="                 to "Parody",
        "$mainUrl/video?c=53&page="                  to "Party",
        "$mainUrl/video?c=211&page="                 to "Pissing",
        "$mainUrl/popularwithwomen&page="            to "Popular With Women",
        "$mainUrl/categories/pornstar&page="         to "Pornstar",
        "$mainUrl/video?c=41&page="                  to "POV",
        "$mainUrl/video?c=24&page="                  to "Public",
        "$mainUrl/video?c=131&page="                 to "Pussy Licking",
        "$mainUrl/video?c=31&page="                  to "Reality",
        "$mainUrl/video?c=42&page="                  to "Red Head",
        "$mainUrl/video?c=81&page="                  to "Role Play",
        "$mainUrl/video?c=522&page="                 to "Romantic",
        "$mainUrl/video?c=67&page="                  to "Rough Sex",
        "$mainUrl/video?c=99&page="                  to "Russian",
        "$mainUrl/video?c=88&page="                  to "School (18+)",
        "$mainUrl/sfw&page="                         to "SFW",
        "$mainUrl/video?c=59&page="                  to "Small Tits",
        "$mainUrl/video?c=91&page="                  to "Smoking",
        "$mainUrl/video?c=492&page="                 to "Solo Female",
        "$mainUrl/video?c=92&page="                  to "Solo Male",
        "$mainUrl/video?c=69&page="                  to "Squirt",
        "$mainUrl/video?c=444&page="                 to "Step Fantasy",
        "$mainUrl/video?c=542&page="                 to "Strap On",
        "$mainUrl/video?c=33&page="                  to "Striptease",
        "$mainUrl/video?c=562&page="                 to "Tattooed Women",
        "$mainUrl/categories/teen&page="             to "Teen (18+)",
        "$mainUrl/video?c=65&page="                  to "Threesome",
        "$mainUrl/video?c=23&page="	                 to "Toys",
        "$mainUrl/transgender&page="	             to "Transgender",
        "$mainUrl/video?c=138&page="	             to "Verified Amateurs",
        "$mainUrl/video?c=482&page="	             to "Verified Couples",
        "$mainUrl/video?c=139&page="	             to "Verified Models",
        "$mainUrl/video?c=43&page="	                 to "Vintage",
        "$mainUrl/vr&page="	                         to "Virtual Reality",
        "$mainUrl/video?c=61&page="	                 to "Webcam",
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

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("a")?.attr("title") ?: return null
        val _href     = this.selectFirst("a")?.attr("href") ?: return null
        val link      = fixUrlNull("$mainUrl/$_href") ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img.thumb")?.attr("src"))

        return newMovieSearchResponse(title, link, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/video/search?search=${query}"
        val document = app.get(url).document
        return document.select("div.sectionWrapper div.wrap").mapNotNull {
            if (it == null) { return@mapNotNull null }
            val title = it.selectFirst("span.title a")?.text() ?: return@mapNotNull null
            val link = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val image = fetchImgUrl(it.selectFirst("img"))
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
        app.get(
            url = data,
            interceptor = WebViewResolver(
                Regex("(master\\.m3u8\\?.*)")
            )
        ).let { response ->
            M3u8Helper().m3u8Generation(
                M3u8Helper.M3u8Stream(
                    response.url,
                    headers = response.headers.toMap()
                ), true
            ).apmap { stream ->
                callback(
                    ExtractorLink(
                        source = name,
                        name = "${this.name} m3u8",
                        url = stream.streamUrl,
                        referer = mainUrl,
                        quality = getQualityFromName(stream.quality?.toString()),
                        isM3u8 = true
                    )
                )
            }
        }
        return true
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