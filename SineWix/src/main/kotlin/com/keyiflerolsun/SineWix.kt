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

    override val mainPage = mainPageOf(
        // "${mainUrl}/public/api/genres/series/all/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"         to "Diziler",
        // "${mainUrl}/public/api/genres/animes/all/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"         to "Animeler",
        // "${mainUrl}/public/api/genres/movies/all/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"         to "Filmler",
        "${mainUrl}/public/api/genres/movies/show/10751/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "Aile",
        "${mainUrl}/public/api/genres/movies/show/28/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Aksiyon",
        "${mainUrl}/public/api/genres/movies/show/16/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Animasyon",
        "${mainUrl}/public/api/genres/movies/show/99/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Belgesel",
        "${mainUrl}/public/api/genres/movies/show/10765/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "Bilim Kurgu & Fantazi",
        "${mainUrl}/public/api/genres/movies/show/878/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"    to "Bilim-Kurgu",
        "${mainUrl}/public/api/genres/movies/show/18/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Dram",
        "${mainUrl}/public/api/genres/movies/show/14/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Fantastik",
        "${mainUrl}/public/api/genres/movies/show/53/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Gerilim",
        "${mainUrl}/public/api/genres/movies/show/9648/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"   to "Gizem",
        "${mainUrl}/public/api/genres/movies/show/35/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Komedi",
        "${mainUrl}/public/api/genres/movies/show/27/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Korku",
        "${mainUrl}/public/api/genres/movies/show/12/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Macera",
        "${mainUrl}/public/api/genres/movies/show/10402/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "Müzik",
        "${mainUrl}/public/api/genres/movies/show/10749/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "Romantik",
        "${mainUrl}/public/api/genres/movies/show/10752/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "Savaş",
        "${mainUrl}/public/api/genres/movies/show/80/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Suç",
        "${mainUrl}/public/api/genres/movies/show/10770/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"  to "TV film",
        "${mainUrl}/public/api/genres/movies/show/36/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA"     to "Tarih",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val home = when {
            request.data.contains("/genres/movies/") -> {
                app.get("${request.data}?page=${page}").parsedSafe<GenresMovie>()!!.data.mapNotNull { item ->
                    newMovieSearchResponse(item.title, "?type=${item.type}&id=${item.id}", TvType.Movie) { this.posterUrl = item.poster_path }
                }
            }
            request.data.contains("/genres/series/") -> {
                app.get("${request.data}?page=${page}").parsedSafe<GenresSerie>()!!.data.mapNotNull { item ->
                    newTvSeriesSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.TvSeries) { this.posterUrl = item.poster_path }
                }
            }
            request.data.contains("/genres/animes/") -> {
                app.get("${request.data}?page=${page}").parsedSafe<GenresSerie>()!!.data.mapNotNull { item ->
                    newAnimeSearchResponse(item.name, "?type=${item.type}&id=${item.id}", TvType.Anime) { this.posterUrl = item.poster_path }
                }
            }
            else -> listOf<SearchResponse>()
        }

        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val request = app.get("${mainUrl}/public/api/search/${query}/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA")
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
            val request = app.get("${mainUrl}/public/api/media/detail/${itemId}/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA")
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
            val request = app.get("${mainUrl}/public/api/${itemType}s/show/${itemId}/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA")
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
            val request = app.get("${mainUrl}/public/api/media/detail/${itemId}/9iQNC5HQwPlaFuJDkhncJ5XTJ8feGXOJatAA")
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