// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.
// ! https://github.com/Amiqo09/Diziyou-Cloudstream

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class RecTV : MainAPI() {
    override var mainUrl              = "https://rectv.cloudflareaccess.com/cdn-cgi/access/login/m.rectv1244.xyz?kid=36db0254eed7e991a894d45bde609de1419f68759c7ea12c3c73647af4d59d53&redirect_url=%2Fadmin%2F&meta=eyJraWQiOiI4N2E0NTdlMDNkMmU0MGE2M2EwMjZiYTdjYWU1ZTViMGFlM2Q4YzUzY2I4OTIyZDRkZDAwNTdhYWNkMDNiM2U5IiwiYWxnIjoiUlMyNTYiLCJ0eXAiOiJKV1QifQ.eyJzZXJ2aWNlX3Rva2VuX3N0YXR1cyI6ZmFsc2UsImlhdCI6MTcyMjcxODkwNywic2VydmljZV90b2tlbl9pZCI6IiIsImF1ZCI6IjM2ZGIwMjU0ZWVkN2U5OTFhODk0ZDQ1YmRlNjA5ZGUxNDE5ZjY4NzU5YzdlYTEyYzNjNzM2NDdhZjRkNTlkNTMiLCJob3N0bmFtZSI6Im0ucmVjdHYxMjQ0Lnh5eiIsImFwcF9zZXNzaW9uX2hhc2giOiI4M2QyMjhhZTZjZjVjMDMyYmQyMDI2NTFmMzc1NzU2MTdiMmIyODljNTc3MjM1MDIyMGUzMTFiZjRiMmM3ODlhIiwibmJmIjoxNzIyNzE4OTA3LCJpc193YXJwIjpmYWxzZSwiaXNfZ2F0ZXdheSI6ZmFsc2UsInR5cGUiOiJtZXRhIiwicmVkaXJlY3RfdXJsIjoiXC9hZG1pblwvIiwibXRsc19hdXRoIjp7ImNlcnRfaXNzdWVyX3NraSI6IiIsImNlcnRfcHJlc2VudGVkIjpmYWxzZSwiY2VydF9zZXJpYWwiOiIiLCJjZXJ0X2lzc3Vlcl9kbiI6IiIsImF1dGhfc3RhdHVzIjoiTk9ORSJ9LCJhdXRoX3N0YXR1cyI6Ik5PTkUifQ.kZS6r0NGjS-2H-ydJIfHFAVzRjJ8Nu9ljbCEi5cSZc-mVBkyzO5KKKgJF-lMHwYz3NH_1CgPkbFiqaFjfFqvvG3e__rJlOxHmCqbfkVxv6gdKSg96-624rTs8BsW4qPzAOyC9GpX0Is86lc10bG4Af7FOzDLiio5xIXA-y5gkyZIcKapGDSgWPktTwpcbnbrZHxQR1B8dNwiJMN30yA48IIsf8E2tBBxyitVaxqo2eaIpnaH9AiU_vQ9SLzjXhagPcT17SBmagweiF--0FCXLipl-CnzeB3kZccsXKUBxOVNQyZ9FLL9yWqo0GJV8wvGNcRzfsKKZj4LZV6uHabhQg"
    override var name                 = "RecTV"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie, TvType.Live)

    override val mainPage = mainPageOf(
        "${mainUrl}/api/channel/by/filtres/0/0/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"      to "Canlı",
        "${mainUrl}/api/movie/by/filtres/0/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Son Yüklenen",
        "${mainUrl}/api/movie/by/filtres/14/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/" to "Aile",
        "${mainUrl}/api/movie/by/filtres/1/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Aksiyon",
        "${mainUrl}/api/movie/by/filtres/13/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/" to "Animasyon",
        "${mainUrl}/api/movie/by/filtres/19/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/" to "Belgesel",
        "${mainUrl}/api/movie/by/filtres/4/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Bilim Kurgu",
        "${mainUrl}/api/movie/by/filtres/2/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Dram",
        "${mainUrl}/api/movie/by/filtres/10/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/" to "Fantastik",
        "${mainUrl}/api/movie/by/filtres/3/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Komedi",
        "${mainUrl}/api/movie/by/filtres/8/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Korku",
        "${mainUrl}/api/movie/by/filtres/17/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/" to "Macera",
        "${mainUrl}/api/movie/by/filtres/5/created/SAYFA/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/"  to "Romantik"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val page = page - 1

        val url  = request.data.replace("SAYFA", "${page}")
        val home = app.get(url)

        val movies = AppUtils.tryParseJson<List<RecItem>>(home.text)!!.mapNotNull { item ->
            val toDict = jacksonObjectMapper().writeValueAsString(item)

            if (item.label != "CANLI" && item.label != "Canlı") {
                newMovieSearchResponse(item.title, "${toDict}", TvType.Movie) { this.posterUrl = item.image }
            } else {
                LiveSearchResponse(
                    name      = item.title,
                    url       = "${toDict}",
                    apiName   = this@RecTV.name,
                    type      = TvType.Live,
                    posterUrl = item.image
                )
            }
        }

        return newHomePageResponse(request.name, movies)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val home    = app.get("${mainUrl}/api/search/${query}/4F5A9C3D9A86FA54EACEDDD635185/c3c5bd17-e37b-4b94-a944-8a3688a30452/")
        val veriler = AppUtils.tryParseJson<RecSearch>(home.text)

        val sonuclar = mutableListOf<SearchResponse>()

        veriler?.channels?.forEach { item ->
            val toDict = jacksonObjectMapper().writeValueAsString(item)

            sonuclar.add(newMovieSearchResponse(item.title, "${toDict}", TvType.Movie) { this.posterUrl = item.image })
        }

        veriler?.posters?.forEach { item ->
            val toDict = jacksonObjectMapper().writeValueAsString(item)

            sonuclar.add(newMovieSearchResponse(item.title, "${toDict}", TvType.Movie) { this.posterUrl = item.image })
        }

        return sonuclar
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val veri = AppUtils.tryParseJson<RecItem>(url) ?: return null

        if (veri.label != "CANLI" && veri.label != "Canlı") {
            return newMovieLoadResponse(veri.title, url, TvType.Movie, url) {
                this.posterUrl = veri.image
                this.plot      = veri.description
                this.year      = veri.year
                this.tags      = veri.genres?.map { it.title }
                this.rating    = "${veri.rating}".toRatingInt()
            }
        } else {
            return LiveStreamLoadResponse(
                name      = veri.title,
                url       = url,
                apiName   = this.name,
                dataUrl   = url,
                posterUrl = veri.image,
                plot      = veri.description,
                tags      = veri.genres?.map { it.title },
            )
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val veri = AppUtils.tryParseJson<RecItem>(data) ?: return false

        veri.sources.forEach { source ->
            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = source.url,
                    referer = "${mainUrl}/",
                    quality = Qualities.Unknown.value,
                    isM3u8  = true
                )
            )
        }

        return true
    }
}