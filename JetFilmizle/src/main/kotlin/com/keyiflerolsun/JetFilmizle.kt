// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class JetFilmizle : MainAPI() {
    override var mainUrl            = "https://jetfilmizle.cx"
    override var name               = "JetFilmizle"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = true
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.Movie)

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    override val mainPage = mainPageOf(
        "${mainUrl}/page/" to "Son Filmler"
    )

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())
            if (doc.select("title").text() == "Just a moment...") {
                return cloudflareKiller.intercept(chain)
            }
            return response
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}${page}", interceptor=interceptor).document
        val home     = document.select("article.movie").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h4 a")?.text()?.substringBefore(" izle") ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.post(
            "${mainUrl}/filmara.php",
            referer     = "${mainUrl}/",
            data        = mapOf("s" to query),
            interceptor = interceptor
        ).document

        return document.select("article.movie").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        Log.d("JTF", "url » ${url}")
        val document = app.get(url, interceptor=interceptor).document

        val title           = document.selectFirst("section#film-100 div.movie-exp-title")?.text()?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("section#film-100 img")?.attr("src"))
        val year            = Regex("""\b(\d{4})\b""").find(document.selectXpath("//div[@class='yap']/strong[contains(text(), 'Vizyon')]").text())?.groupValues?.get(1)?.toIntOrNull()
        val description     = document.selectFirst("section#film-100 p.aciklama")?.text()?.trim()
        val tags            = document.select("section#film-100 div.catss a").map { it.text() }
        val rating          = document.selectFirst("section#film-100 div.imdb_puan span")?.text()?.split(" ")?.last()?.toRatingInt()
        val actors          = document.select("section#film-100 div.oyuncu").map {
            Actor(it.selectFirst("div.name")!!.text(), it.selectFirst("div.img img")!!.attr("src"))
        }

        val recommendations = document.selectXpath("div#benzers article").mapNotNull {
            val recName      = it.selectFirst("h2 a")?.text() ?: return@mapNotNull null
            val recHref      = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
            val recPosterUrl = fixUrlNull(it.selectFirst("img")?.attr("src"))
            newMovieSearchResponse(recName, recHref, TvType.Movie) {
                this.posterUrl = recPosterUrl
            }
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.rating          = rating
            this.recommendations = recommendations
            addActors(actors)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("JTF", "data » ${data}")
        val document = app.get(data, interceptor=interceptor).document

        document.select("div.film_part a").forEach {
            val source = it.selectFirst("span")?.text()?.trim() ?: return@forEach
            val movDoc = app.get(it.attr("href"), interceptor=interceptor).document
            var iframe = movDoc.selectFirst("div#movie iframe")?.attr("src") ?: return@forEach

            if (iframe.startsWith("//")) iframe = "https:$iframe"
            Log.d("JTF", "iframe » ${iframe}")

            loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        }

        return true
    }
}
