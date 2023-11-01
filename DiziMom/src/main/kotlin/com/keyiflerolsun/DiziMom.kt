// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.Qualities

class DiziMom : MainAPI() {
    override var mainUrl            = "https://www.dizimom.pro"
    override var name               = "DiziMom"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = false
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)

    override val mainPage =
        mainPageOf(
            "$mainUrl/turkce-dublaj-diziler/page/"      to "Dublajlı Diziler",
            "$mainUrl/netflix-dizileri-izle/page/"      to "Netflix Dizileri",
            "$mainUrl/yabanci-dizi-izle/page/"          to "Yabancı Diziler",
            "$mainUrl/yerli-dizi-izle/page/"            to "Yerli Diziler",
            "$mainUrl/kore-dizileri-izle/page/"         to "Kore Dizileri",
            "$mainUrl/full-hd-hint-dizileri-izle/page/" to "Hint Dizileri",
            "$mainUrl/tv-programlari-izle/page/"        to "TV Programları",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}/").document
        val home     = document.select("div.single-item").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.categorytitle a")?.text()?.substringBefore(" izle") ?: return null
        val href      = fixUrlNull(this.selectFirst("div.categorytitle a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("div.cat-img img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document

        return document.select("div.single-item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("div.title h1")?.text()?.substringBefore(" izle") ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.category_image img")?.attr("src")) ?: return null
        val year        = document.selectXpath("//div[span[contains(text(), 'Yapım Yılı')]]").text().substringAfter("Yapım Yılı : ").trim().toIntOrNull()
        val description = document.selectFirst("div.category_desc")?.text()?.trim()
        val tags        = document.select("div.genres a").mapNotNull { it?.text()?.trim() }
        val rating      = document.selectXpath("//div[span[contains(text(), 'IMDB')]]").text().substringAfter("IMDB : ").trim().toRatingInt()
        val actors      = document.selectXpath("//div[span[contains(text(), 'Oyuncular')]]").text().substringAfter("Oyuncular : ").split(", ").map {
            Actor(it.trim())
        }

        val episodes    = document.select("div.bolumust").mapNotNull {
            val ep_name    = it.selectFirst("div.baslik")?.text()?.trim() ?: return@mapNotNull null
            val ep_href    = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val ep_episode = Regex("""(\d+)\.Bölüm""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull()
            val ep_season  = Regex("""(\d+)\.Sezon""").find(ep_name)?.groupValues?.get(1)?.toIntOrNull() ?: 1

            Episode(
                data    = ep_href,
                name    = ep_name,
                season  = ep_season,
                episode = ep_episode
            )
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year      = year
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZM", "data » $data")
            val document = app.get(
                data,
                headers = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36",
                    "Cookie"     to "wordpress_logged_in_94427965a200eb7dd292509ed2c7c018=keyiflerolsun|1699737674|EvfGx8bnw88aTkvvmRNqWQUuIYsUzLOIlWa4nwixKFn|87a00e3a0d391fa1074e004d37a54a66e7bfc85bdc0191a8eec3cb9df741db8c"
                )
            ).document
            val iframe   = document.selectFirst("div#vast iframe")?.attr("src") ?: return false
            Log.d("DZM", "iframe » $iframe")


            var i_source: String? = null
            var m3u_link: String? = null

            if (iframe.contains("hdmomplayer")) {
                i_source      = app.get("$iframe", referer="$mainUrl/").text
                m3u_link      = Regex("""file:\"([^\"]+)""").find(i_source)?.groupValues?.get(1)

                val track_str = Regex("""tracks:\[([^\]]+)""").find(i_source)?.groupValues?.get(1)
                if (track_str != null) {
                    val tracks:List<Track> = jacksonObjectMapper().readValue("[$track_str]")

                    Log.d("DZM", "tracks » $tracks")
                    for (track in tracks) {
                        if (track.file == null || track.label == null) continue
                        if (track.label.contains("Forced")) continue

                        Log.d("DZM", "${track.label} » ${track.file}")
                        subtitleCallback.invoke(
                            SubtitleFile(
                                lang = track.label,
                                url  = fixUrl("https://hdmomplayer.com" + track.file)
                            )
                        )
                    }
                }
            }

            if (iframe.contains("hdplayersystem")) {
                val vid_id = if (iframe.contains("video/")) {
                    iframe.substringAfter("video/")
                } else {
                    iframe.substringAfter("?data=")
                }

                val post_url = "https://hdplayersystem.live/player/index.php?data=${vid_id}&do=getVideo"
                Log.d("DZM", "post_url » $post_url")

                val response = app.post(
                    post_url,
                    data = mapOf(
                        "hash" to vid_id,
                        "r"    to "$mainUrl/"
                    ),
                    referer = "$mainUrl/",
                    headers = mapOf(
                        "Content-Type"     to "application/x-www-form-urlencoded; charset=UTF-8",
                        "X-Requested-With" to "XMLHttpRequest"
                    )
                )
                val video_response = response.parsedSafe<SystemResponse>() ?: return false
                m3u_link           = video_response.securedLink
            }

            if (iframe.contains("peacemakerst") || iframe.contains("hdstreamable")) {
                val post_url = "${iframe}?do=getVideo"
                Log.d("DZM", "post_url » $post_url")

                val response = app.post(
                    post_url,
                    data = mapOf(
                        "hash" to iframe.substringAfter("video/"),
                        "r"    to "$mainUrl/",
                        "s"    to ""
                    ),
                    referer = "$mainUrl/",
                    headers = mapOf(
                        "Content-Type"     to "application/x-www-form-urlencoded; charset=UTF-8",
                        "X-Requested-With" to "XMLHttpRequest"
                    )
                )
                val video_response = response.parsedSafe<PeaceResponse>() ?: return false
                val video_sources  = video_response.videoSources
                if (video_sources.isNotEmpty()) {
                    m3u_link = video_sources.last().file
                }
            }

            if (iframe.contains("videoseyred.in")) {
                val video_id = iframe.substringAfter("embed/").substringBefore("?")
                val response_raw = app.get("https://videoseyred.in/playlist/${video_id}.json")
                val response_list:List<VideoSeyred> = jacksonObjectMapper().readValue(response_raw.text)
                Log.d("DZM", "response_list » $response_list")
                val response = response_list[0]
                Log.d("DZM", "response » $response")

                for (track in response.tracks) {
                    if (track.label != null && track.kind == "captions") {
                        subtitleCallback.invoke(
                            SubtitleFile(
                                lang = track.label,
                                url  = fixUrl(track.file)
                            )
                        )
                    }
                }

                for (source in response.sources) {
                    callback.invoke(
                        ExtractorLink(
                            source  = this.name,
                            name    = this.name,
                            url     = source.file,
                            referer = "https://videoseyred.in/",
                            quality = Qualities.Unknown.value,
                            isM3u8  = source.file.contains(".m3u8")
                        )
                    )
                }

                return true
            }

            Log.d("DZM", "m3u_link » $m3u_link")
            if (m3u_link == null) {
                Log.d("DZM", "i_source » $i_source")
                return false
            }

            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = m3u_link,
                    referer = iframe,
                    quality = Qualities.Unknown.value,
                    isM3u8  = m3u_link.contains(".m3u8") || m3u_link.contains(".txt")
                )
            )

            return true
    }
}
