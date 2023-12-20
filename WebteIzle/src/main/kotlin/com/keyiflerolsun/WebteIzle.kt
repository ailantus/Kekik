// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class WebteIzle : MainAPI() {
    override var mainUrl              = "https://webteizle2.com"
    override var name                 = "WebteIzle"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/film-izle/"       to "Güncel",
        "${mainUrl}/yeni-filmler/"    to "Yeni",
        "${mainUrl}/tavsiye-filmler/" to "Tavsiye",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}").document
        val home     = document.select("div.golgever").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.filmname")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.post("${mainUrl}/_ajaxweb/wrapper/filtre?a=${query}").document

        return document.select("div.golgever").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("[property='og:title']")?.attr("content")?.substringBefore(" izle") ?: return null
        val poster      = fixUrlNull(document.selectFirst("div.card img")?.attr("data-src"))
        val description = document.selectFirst("blockquote")?.text()?.trim()
        val tags        = document.selectXpath("//a[@itemgroup='genre']").map { it.text() }
        val rating      = document.selectFirst("div.detail")?.text()?.trim()?.toRatingInt()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot      = description
            this.tags      = tags
            this.rating    = rating
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("WBTI", "data » ${data}")
        val document = app.get(data).document

        val film_id  = document.selectFirst("button#wip")?.attr("data-id") ?: return false
        Log.d("WBTI", "film_id » ${film_id}")

        val dil_list = mutableListOf<String>()
        if (document.selectFirst("div.golge a[href*=dublaj]")?.attr("src") != null) {
            dil_list.add("0")
        }

        if (document.selectFirst("div.golge a[href*=altyazi]")?.attr("src") != null) {
            dil_list.add("1")
        }

        dil_list.forEach {
            val dilAd = if (it == "0") "Türkçe Dublaj" else "Altyazı"

            val player_api = app.post(
                "${mainUrl}/ajax/dataAlternatif3.asp",
                headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
                data    = mapOf(
                    "filmid" to film_id,
                    "dil"    to it,
                    "s"      to "",
                    "b"      to "",
                    "bot"    to "0"
                )
            ).text
            val player_data = AppUtils.tryParseJson<DataAlternatif>(player_api) ?: return@forEach

            for (this_embed in player_data.data) { 
                val embed_api = app.post(
                    "${mainUrl}/ajax/dataEmbed.asp",
                    headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
                    data    = mapOf("id" to this_embed.id.toString())
                ).document

                var iframe = fixUrlNull(embed_api.selectFirst("iframe")?.attr("src"))

                if (iframe == null) {
                    val scriptSource = embed_api.selectFirst("script")?.data() ?: ""
                    val matchResult  = Regex("""(vidmoly|okru|filemoon)\('(.*)','""").find(scriptSource)

                    if (matchResult == null) {
                        Log.d("WBTI", "scriptSource » $scriptSource")
                    } else {
                        val platform = matchResult.groupValues[1]
                        val vidId    = matchResult.groupValues[2]

                        iframe       = when(platform) {
                            "vidmoly"  -> "https://vidmoly.to/embed-${vidId}.html"
                            "okru"     -> "https://odnoklassniki.ru/videoembed/${vidId}"
                            "filemoon" -> "https://filemoon.sx/e/${vidId}"
                            else       -> null
                        }
                    }
                } else if (iframe.contains(mainUrl)) {
                    Log.d("WBTI", "iframe » ${iframe}")
                    val i_source = app.get(iframe, referer=data).text

                    val encoded  = Regex("""file\": \"([^\"]+)""").find(i_source)?.groupValues?.get(1)
                    val bytes    = encoded.split("\\x").filter { it.isNotEmpty() }.map { it.toInt(16).toByte() }.toByteArray()
                    val m3u_link = String(bytes, Charsets.UTF_8)
                    Log.d("WBTI", "m3u_link » ${m3u_link}")

                    val track_str = Regex("""tracks = \[([^\]]+)""").find(i_source)?.groupValues?.get(1)
                    if (track_str != null) {
                        val tracks:List<Track> = jacksonObjectMapper().readValue("[${track_str}]")

                        for (track in tracks) {
                            if (track.file == null || track.label == null) continue
                            if (track.label.contains("Forced")) continue

                            subtitleCallback.invoke(
                                SubtitleFile(
                                    lang = track.label.replace("\\u0131", "ı").replace("\\u0130", "İ").replace("\\u00fc", "ü").replace("\\u00e7", "ç"),
                                    url  = fixUrl(track.file).replace("\\", "")
                                )
                            )
                        }
                    }

                    callback.invoke(
                        ExtractorLink(
                            source  = "${this.name} - ${dilAd}",
                            name    = "${this.name} - ${dilAd}",
                            url     = m3u_link ?: continue,
                            referer = "${mainUrl}/",
                            quality = Qualities.Unknown.value,
                            isM3u8  = true
                        )
                    )

                    continue
                }

                if (iframe != null) {
                    Log.d("WBTI", "iframe » ${iframe}")
                    loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
                }
            }
        }


        return true
    }
}
