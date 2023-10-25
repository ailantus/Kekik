// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import okhttp3.Interceptor
import okhttp3.Response
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.ExtractorLink


class DiziBox : MainAPI() {
    override var mainUrl            = "https://www.dizibox.tv"
    override var name               = "DiziBox"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.TvSeries)
    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor by lazy { CloudflareInterceptor(cloudflareKiller) }

    override val mainPage =
        mainPageOf(
            "/?tur[0]=aile&yil&imdb&orderby=imdb"     to "Aile",
            // "/?tur[0]=aksiyon&yil&imdb&orderby=imdb"  to "Aksiyon",
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
        val url      = "$mainUrl/dizi-arsivi/page/" + page + request.data
        Log.d("DZB", "_url » $url")

        val document = app.get(
            url,
            referer     = "$mainUrl/",
            interceptor = interceptor
        ).document
        Log.d("DZB", "_document » $document")

        val home     = document.select("article.detailed-article").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("h3 a")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("h3 a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) { this.posterUrl = posterUrl }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        Log.d("DZB", "_url » $url")
        Log.d("DZB", "_document » $document")

        return null
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            Log.d("DZB", "_data » $data")

            return true
    }
}
