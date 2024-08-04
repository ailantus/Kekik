// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class SineWix : MainAPI() {
    override var mainUrl              = "https://ydfvfdizipanel.ru"
    override var name                 = "SineWix"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)

    // override var sequentialMainPage            = true
    // override var sequentialMainPageDelay       = 250L
    // override var sequentialMainPageScrollDelay = 250L

    private fun getRandomUserAgent(): String {
        val androidVersions = listOf("12", "13", "14")
        val modelPrefixes   = listOf("SM-S", "SM-G", "SM-A", "MI", "POCO", "M", "V", "H")
        val modelSuffixes   = listOf("F", "U", "W", "K", "T")
        val brands          = listOf("Samsung", "Xiaomi", "Huawei", "OnePlus", "Oppo", "Vivo", "Google")
        val deviceNames     = mapOf(
            "Samsung" to listOf("s22", "s21", "a52", "a32", "note20"),
            "Xiaomi"  to listOf("mi11", "redmiNote10", "pocoX3"),
            "Huawei"  to listOf("p40", "mate40", "nova8"),
            "OnePlus" to listOf("9pro", "8t", "nord2"),
            "Oppo"    to listOf("findX3", "reno5", "a54"),
            "Vivo"    to listOf("x60", "v21", "y20"),
            "Google"  to listOf("pixel6", "pixel5", "pixel4a")
        )
        val languages       = listOf("tr")

        val randomAndroidVersion = androidVersions.random()
        val randomBrand          = brands.random()
        val randomModelNumber    = modelPrefixes.random() + (900..999).random().toString() + modelSuffixes.random()
        val randomDeviceName     = deviceNames[randomBrand]?.random() ?: "unknown"
        val randomLanguage       = languages.random()

        return "EasyPlex (Android ${randomAndroidVersion}; ${randomModelNumber}; ${randomBrand} ${randomDeviceName}; ${randomLanguage})"
    }

    val swKey     = "9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"
    val swHeaders = mapOf(
        "accept"          to "application/json",
        "packagename"     to "com.sinewix",
        "authorization"   to "Bearer EuXs1Y5oXTrDpGte3E2dNDIu82LLjaoCd6om",
        "user-agent"      to getRandomUserAgent()
    )

    override val mainPage = mainPageOf(
        "${mainUrl}/public/api/genres/movies/all/${swKey}"         to "Filmler",
        "${mainUrl}/public/api/genres/series/all/${swKey}"         to "Diziler",
        "${mainUrl}/public/api/genres/animes/all/${swKey}"         to "Animeler",
        "${mainUrl}/public/api/genres/movies/show/10751/${swKey}"  to "Aile",
        "${mainUrl}/public/api/genres/movies/show/28/${swKey}"     to "Aksiyon",
        "${mainUrl}/public/api/genres/movies/show/16/${swKey}"     to "Animasyon",
        "${mainUrl}/public/api/genres/movies/show/99/${swKey}"     to "Belgesel",
        "${mainUrl}/public/api/genres/movies/show/10765/${swKey}"  to "Bilim Kurgu & Fantazi",
        "${mainUrl}/public/api/genres/movies/show/878/${swKey}"    to "Bilim-Kurgu",
        "${mainUrl}/public/api/genres/movies/show/18/${swKey}"     to "Dram",
        "${mainUrl}/public/api/genres/movies/show/14/${swKey}"     to "Fantastik",
        "${mainUrl}/public/api/genres/movies/show/53/${swKey}"     to "Gerilim",
        "${mainUrl}/public/api/genres/movies/show/9648/${swKey}"   to "Gizem",
        "${mainUrl}/public/api/genres/movies/show/35/${swKey}"     to "Komedi",
        "${mainUrl}/public/api/genres/movies/show/27/${swKey}"     to "Korku",
        "${mainUrl}/public/api/genres/movies/show/12/${swKey}"     to "Macera",
        "${mainUrl}/public/api/genres/movies/show/10402/${swKey}"  to "Müzik",
        "${mainUrl}/public/api/genres/movies/show/10749/${swKey}"  to "Romantik",
        "${mainUrl}/public/api/genres/movies/show/10752/${swKey}"  to "Savaş",
        "${mainUrl}/public/api/genres/movies/show/80/${swKey}"     to "Suç",
        "${mainUrl}/public/api/genres/movies/show/10770/${swKey}"  to "TV film",
        "${mainUrl}/public/api/genres/movies/show/36/${swKey}"     to "Tarih",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url  = "${request.data}?page=${page}"
        val home = when {
            request.data.contains("/genres/movies/") -> {
                app.get(url, headers=swHeaders).parsedSafe<GenresMovie>()?.data?.mapNotNull { item ->
                    newMovieSearchResponse(item.title, "?type=${item.type}&id=${item.id}", TvType.Movie) { this.posterUrl = item.poster_path }
                } ?: app.get(url.replace("movies/all", "news/all"), headers=swHeaders).parsedSafe<GenresMovie>()?.data?.mapNotNull { item ->
                    newMovieSearchResponse(item.title, "?type=${item.type}&id=${item.id}", TvType.Movie) { this.posterUrl = item.poster_path }
                } ?: listOf<SearchResponse>()
            }
            request.data.contains("/genres/series/") -> {
                app.get(url, headers=swHeaders).parsedSafe<GenresSerie>()?.data?.mapNotNull { item ->
                    newTvSeriesSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.TvSeries) { this.posterUrl = item.poster_path }
                } ?: app.get(url.replace("series/all", "latestseries/all"), headers=swHeaders).parsedSafe<GenresSerie>()?.data?.mapNotNull { item ->
                    newMovieSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.Movie) { this.posterUrl = item.poster_path }
                } ?: listOf<SearchResponse>()
            }
            request.data.contains("/genres/animes/") -> {
                app.get(url, headers=swHeaders).parsedSafe<GenresSerie>()?.data?.mapNotNull { item ->
                    newAnimeSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.Anime) { this.posterUrl = item.poster_path }
                } ?: app.get(url.replace("animes/all", "latestanimes/all"), headers=swHeaders).parsedSafe<GenresSerie>()?.data?.mapNotNull { item ->
                    newMovieSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.Movie) { this.posterUrl = item.poster_path }
                } ?: listOf<SearchResponse>()
            }
            else -> listOf<SearchResponse>()
        }

        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val request = app.get("${mainUrl}/public/api/search/${query}/${swKey}", headers=swHeaders)
        val reqData = request.parsedSafe<Search>()?.search

        return reqData?.mapNotNull { item ->
            when (item.type) {
                "movie" -> newMovieSearchResponse(item.name,    "?type=${item.type}&id=${item.id}", TvType.Movie)    { this.posterUrl = item.poster_path }
                "serie" -> newTvSeriesSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.TvSeries) { this.posterUrl = item.poster_path }
                "anime" -> newAnimeSearchResponse(item.name,    "?type=${item.type}&id=${item.id}", TvType.Anime)    { this.posterUrl = item.poster_path }
                else -> null
            }
        } ?: mutableListOf<SearchResponse>()
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val itemType = url.substringAfter("?type=").substringBefore("&id=")
        val itemId   = url.substringAfter("&id=")

        if (itemType == "movie") {
            val request = app.get("${mainUrl}/public/api/media/detail/${itemId}/${swKey}", headers=swHeaders)
            val media   = request.parsedSafe<MovieDetail>() ?: return null

            val orgTitle        = media.title
            val altTitle        = media.original_name ?: ""
            val title           = if (altTitle.isNotEmpty() && orgTitle != altTitle) "${orgTitle} - ${altTitle}" else orgTitle

            val poster          = fixUrlNull(media.poster_path)
            val description     = media.overview
            val year            = media.release_date.split("-").first().toIntOrNull()
            val tags            = media?.genres?.map { it.name }
            val rating          = "${media.vote_average}".toRatingInt()
            val recommendations = media?.relateds?.mapNotNull { newMovieSearchResponse(it.title, "?type=${it.type}&id=${it.id}", TvType.Movie) { this.posterUrl = it.poster_path } }
            val actors          = media?.casterslist?.map { Actor(it.name, it?.profile_path) }

            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.rating          = rating
                this.recommendations = recommendations
                addActors(actors)
            }
        } else {
            val request = app.get("${mainUrl}/public/api/${itemType}s/show/${itemId}/${swKey}", headers=swHeaders)
            val media   = request.parsedSafe<SerieDetail>() ?: return null

            val orgTitle        = media.name
            val altTitle        = media.original_name ?: ""
            val title           = if (altTitle.isNotEmpty() && orgTitle != altTitle) "${orgTitle} - ${altTitle}" else orgTitle

            val poster          = fixUrlNull(media.poster_path)
            val description     = media.overview
            val year            = media.first_air_date.split("-").first().toIntOrNull()
            val tags            = media?.genres?.map { it.name }
            val rating          = "${media.vote_average}".toRatingInt()
            val recommendations = media?.relateds?.mapNotNull { newMovieSearchResponse(it.name, "?type=${it.type}&id=${it.id}", TvType.Movie) { this.posterUrl = it.poster_path } }
            val actors          = media?.casterslist?.map { Actor(it.name, it?.profile_path) }

            val episodeList     = mutableListOf<Episode>()

            media.seasons.forEach { season ->
                season.episodes.forEach { episode ->
                    episodeList.add(Episode(
                        data        = url + "&source=" + episode.videos.firstOrNull()?.link ?: "",
                        name        = episode.name,
                        season      = season.season_number,
                        episode     = episode.episode_number,
                        description = episode.overview,
                        posterUrl   = episode.still_path
                    ))
                }
            }

            return newTvSeriesLoadResponse(title, url, if (itemType == "serie") TvType.TvSeries else TvType.Anime, episodeList) {
                this.posterUrl       = poster
                this.plot            = description
                this.year            = year
                this.tags            = tags
                this.rating          = rating
                this.recommendations = recommendations
                addActors(actors)
            }
        }

        return null
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        if (!data.contains("&source=")) {
            val itemId  = data.substringAfter("&id=").substringBefore("&source=")
            val request = app.get("${mainUrl}/public/api/media/detail/${itemId}/${swKey}", headers=swHeaders)
            val media   = request.parsedSafe<MovieDetail>() ?: return false

            media.videos.forEach { video ->
                Log.d("SNWX", "video » ${video}")

                callback.invoke(
                    ExtractorLink(
                        source  = this.name,
                        name    = this.name,
                        url     = video.link,
                        referer = "${mainUrl}/",
                        quality = Qualities.Unknown.value,
                        type    = INFER_TYPE
                    )
                )
            }
        } else {
            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = data.substringAfter("&source="),
                    referer = "${mainUrl}/",
                    quality = Qualities.Unknown.value,
                    type    = INFER_TYPE
                )
            )
        }

        return true
    }
}