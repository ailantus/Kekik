// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import android.util.Base64
import org.jsoup.nodes.Element
import org.jsoup.nodes.Document
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities


class FullHDFilmizlesene : MainAPI() {
    override var mainUrl            = "https://www.fullhdfilmizlesene.pw"
    override var name               = "FullHDFilmizlesene"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.Movie)

    override val mainPage           =
        mainPageOf(
            "$mainUrl/en-cok-izlenen-filmler-izle-hd/"            to "En Çok izlenen Filmler",
            "$mainUrl/filmizle/imdb-puani-yuksek-filmler-izle-1/" to "IMDB Puanı Yüksek Filmler",
            "$mainUrl/filmizle/aile-filmleri-izle-2/"             to "Aile Filmleri",
            "$mainUrl/filmizle/aksiyon-filmler-izle-1/"           to "Aksiyon Filmleri",
            "$mainUrl/filmizle/animasyon-filmleri-izle-4/"        to "Animasyon Filmleri",
            "$mainUrl/filmizle/belgesel-filmleri-izle-2/"         to "Belgeseller",
            "$mainUrl/filmizle/bilim-kurgu-filmleri-izle-1/"      to "Bilim Kurgu Filmleri",
            "$mainUrl/filmizle/bluray-filmler-izle-1/"            to "Blu Ray Filmler",
            "$mainUrl/filmizle/cizgi-filmler-izle-1/"             to "Çizgi Filmler",
            "$mainUrl/filmizle/dram-filmleri-izle/"               to "Dram Filmleri",
            "$mainUrl/filmizle/fantastik-filmleri-izle-2/"        to "Fantastik Filmler",
            "$mainUrl/filmizle/gerilim-filmleri-izle-3/"          to "Gerilim Filmleri",
            "$mainUrl/filmizle/gizem-filmleri-izle/"              to "Gizem Filmleri",
            "$mainUrl/filmizle/hint-filmler-fh-hd-izle/"          to "Hint Filmleri",
            "$mainUrl/filmizle/komedi-filmleri-izle-2/"           to "Komedi Filmleri",
            "$mainUrl/filmizle/korku-filmleri-izle-2/"            to "Korku Filmleri",
            "$mainUrl/filmizle/macera-filmleri-izle-1/"           to "Macera Filmleri",
            "$mainUrl/filmizle/muzikal-filmleri-izle/"            to "Müzikal Filmler",
            "$mainUrl/filmizle/polisiye-filmleri-izle-1/"         to "Polisiye Filmleri",
            "$mainUrl/filmizle/psikolojik-filmleri-izle/"         to "Psikolojik Filmler",
            "$mainUrl/filmizle/romantik-filmler-izle-1/"          to "Romantik Filmler",
            "$mainUrl/filmizle/savas-filmleri-izle-2/"            to "Savaş Filmleri",
            "$mainUrl/filmizle/suc-filmleri-izle-3/"              to "Suç Filmleri",
            "$mainUrl/filmizle/tarih-filmleri-izle/"              to "Tarih Filmleri",
            "$mainUrl/filmizle/western-filmleri-izle/"            to "Western Filmler",
            "$mainUrl/filmizle/yerli-filmler-izle-3/"             to "Yerli Filmler",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("li.film").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("span.film-title")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/arama/$query").document

        return document.select("li.film").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("div[class=izle-titles]")?.text()?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("div img")?.attr("data-src"))
        val year            = document.selectFirst("div.dd a.category")?.text()?.split(" ")?.get(0)?.trim()?.toIntOrNull()
        val description     = document.selectFirst("div.ozet-ic > p")?.text()?.trim()
        val tags            = document.select("a[rel='category tag']").map { it.text() }
        val rating          = document.selectFirst("div.puanx-puan")?.text()?.split(" ")?.last()?.split(".")?.first()?.toIntOrNull()
        val duration        = document.selectFirst("span.sure")?.text()?.split(" ")?.get(0)?.trim()?.toIntOrNull()
        val recommendations = document.selectXpath("//div[span[text()='Benzer Filmler']]/following-sibling::section/ul/li").mapNotNull {
            val recName      = it.selectFirst("span.film-title")?.text() ?: return@mapNotNull null
            val recHref      = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val recPosterUrl = fixUrlNull(it.selectFirst("img")?.attr("data-src"))
            newMovieSearchResponse(recName, recHref, TvType.Movie) {
                this.posterUrl = recPosterUrl
            }
        }

        val actors = document.select("div.film-info ul li:nth-child(2) a > span").map {
            Actor(it.text())
        }

        val trailer = Regex("""embedUrl\": \"(.*)\"""").find(document.html())?.groups?.get(1)?.value

        Log.d("FHD", "_rating » $rating")

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.rating          = rating
            this.duration        = duration
            this.recommendations = recommendations
            addActors(actors)
            addTrailer(trailer)
        }
    }

    private fun atob(s: String): String {
        return String(Base64.decode(s, Base64.DEFAULT))
    }

    private fun rtt(s: String): String {
        fun rot13Char(c: Char): Char {
            return when (c) {
                in 'a'..'z' -> ((c - 'a' + 13) % 26 + 'a'.code).toChar()
                in 'A'..'Z' -> ((c - 'A' + 13) % 26 + 'A'.code).toChar()
                else -> c
            }
        }

        return s.map { rot13Char(it) }.joinToString("")
    }

    private fun scxDecode(scx: MutableMap<String, MutableMap<String, Any>>): Map<String, Any> {
        for ((key, item) in scx) {
            item["tt"] = atob(item["tt"] as String)
            val sx = item["sx"] as MutableMap<String, Any>
            sx["t"]?.let { tList ->
                sx["t"] = (tList as List<String>).map { atob(rtt(it)) }
            }
            sx["p"]?.let { pList ->
                sx["p"] = (pList as List<String>).map { atob(rtt(it)) }
            }
            item["sx"] = sx
            scx[key] = item
        }
        return scx
    }

    private fun getRapidLink(document: Document): String? {
        val script_element = document.select("script").firstOrNull { it.data().isNotEmpty() }
        val script_content = script_element?.data()?.trim() ?: return null
    
        val scx_data = Regex("scx = (.*?);").find(script_content)?.groups?.get(1)?.value ?: return null

        val objectMapper = jacksonObjectMapper()
        val scx_map: MutableMap<String, MutableMap<String, Any>> = objectMapper.readValue(scx_data)
        val scx_decode   = scxDecode(scx_map)
    
        val atom_map = scx_decode["atom"] as? Map<String, Any> ?: return null
        val sx_map   = atom_map["sx"] as? Map<String, Any> ?: return null
        val t_list   = sx_map["t"] as? List<String> ?: return null
        if (t_list.isEmpty()) return null
        Log.d("FHD", "t_list » $t_list")
    
        return t_list[0]
    }

    private fun rapidToM3u8(rapid: String): String? {
        val extracted_value = Regex("""file": "(.*)",""").find(rapid)?.groups?.get(1)?.value ?: return null

        val bytes   = extracted_value.split("\\x").filter { it.isNotEmpty() }.map { it.toInt(16).toByte() }.toByteArray()
        val decoded = String(bytes, Charsets.UTF_8)
        Log.d("FHD", "decoded » $decoded")

        return decoded
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("FHD", "data » $data")
            val document = app.get(data).document
            val rapidvid = getRapidLink(document) ?: return false

            val rapid    = app.get(rapidvid).text
            val m3u_link = rapidToM3u8(rapid) ?: return false

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
