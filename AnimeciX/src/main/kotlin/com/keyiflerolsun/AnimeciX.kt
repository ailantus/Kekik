// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class AnimeciX : MainAPI() {
    override var mainUrl            = "https://animecix.net"
    override var name               = "AnimeciX"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = false
    override val hasDownloadSupport = true
    override val supportedTypes     = setOf(TvType.Anime)

    override val mainPage =
        mainPageOf(
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=action&onlyStreamable=true"          to "Aksiyon",
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=sci-fi-fantasy&onlyStreamable=true"  to "Bilim Kurgu",
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=drama&onlyStreamable=true"           to "Dram",
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=mystery&onlyStreamable=true"         to "Gizem",
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=comedy&onlyStreamable=true"          to "Komedi",
            "$mainUrl/secure/titles?type=series&order=user_score:desc&genre=horror&onlyStreamable=true"          to "Korku"
        )

    data class Category(
        @JsonProperty("pagination") val pagination: Pagination,
    )

    data class Search(
        @JsonProperty("results") val results: List<AnimeSearch>,
    )

    data class Title(
        @JsonProperty("title") val title: Anime,
    )

    data class Pagination(
        @JsonProperty("current_page") val current_page: Int,
        @JsonProperty("last_page") val last_page: Int,
        @JsonProperty("per_page") val per_page: Int,
        @JsonProperty("data") val data: List<AnimeSearch>,
        @JsonProperty("total") val total: Int,
    )

    data class AnimeSearch(
        @JsonProperty("id") val id: Int,
        @JsonProperty("title_type") val title_type: String,
        @JsonProperty("name") val title: String,
        @JsonProperty("poster") val poster: String,
    )

    data class Anime(
        @JsonProperty("id") val id: Int,
        @JsonProperty("title_type") val title_type: String,
        @JsonProperty("name") val title: String,
        @JsonProperty("poster") val poster: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("year") val year: Int?,
        @JsonProperty("mal_vote_average") val rating: String?,
        @JsonProperty("genres") val tags: List<Genre>,
        @JsonProperty("trailer") val trailer: String?,
        @JsonProperty("credits") val actors: List<Credit>,
        @JsonProperty("season_count") val season_count: Int,
        @JsonProperty("videos") val videos: List<Video>
    )

    data class Genre(
        @JsonProperty("display_name") val name: String,
    )

    data class Credit(
        @JsonProperty("name") val name: String,
        @JsonProperty("poster") val poster: String?,
    )

    data class Video(
        @JsonProperty("episode_num") val episode_num: Int?,
        @JsonProperty("season_num") val season_num: Int?,
        @JsonProperty("url") val url: String,
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(request.data + "&page=${page}&perPage=12").parsedSafe<Category>()

        val home     = response?.pagination?.data?.mapNotNull { anime ->
            newAnimeSearchResponse(
                anime.title,
                "$mainUrl/secure/titles/${anime.id}?titleId=${anime.id}",
                TvType.Anime
            ) {
                this.posterUrl = anime.poster
            }
        } ?: emptyList()

        return newHomePageResponse(request.name, home)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get("$mainUrl/secure/search/$query?limit=20").parsedSafe<Search>() ?: return emptyList()

        return response.results.mapNotNull { anime ->
            newAnimeSearchResponse(
                anime.title,
                "$mainUrl/secure/titles/${anime.id}?titleId=${anime.id}",
                TvType.Anime
            ) {
                this.posterUrl = anime.poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val response = app.get(url).parsedSafe<Title>() ?: return null

        val episodes = mutableListOf<Episode>()

        if (response.title.title_type == "anime") {
            for (sezon in 1..response.title.season_count) {
                val sezon_response = app.get("${url}&seasonNumber=${sezon}").parsedSafe<Title>() ?: return null
                for (video in sezon_response.title.videos) {
                    episodes.add(Episode(
                        data    = video.url,
                        name    = "${video.season_num}. Sezon ${video.episode_num}. Bölüm",
                        season  = video.season_num,
                        episode = video.episode_num
                    ))
                }
            }
        } else {
            if (response.title.videos.isNotEmpty() == true) {
                episodes.add(Episode(
                    data    = response.title.videos.first().url,
                    name    = "Filmi İzle",
                    season  = 1,
                    episode = 1
                ))
            }
        }


        return newTvSeriesLoadResponse(
            response.title.title,
            "$mainUrl/secure/titles/${response.title.id}?titleId=${response.title.id}",
            TvType.Anime,
            episodes
        ) {
            this.posterUrl = response.title.poster
            this.year      = response.title.year
            this.plot      = response.title.description
            this.tags      = response.title.tags.filterNotNull().map { it.name }
            this.rating    = response.title.rating.toRatingInt()
            addActors(response.title.actors.filterNotNull().map { Actor(it.name, it.poster) })
            addTrailer(response.title.trailer)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            if (data.contains("tau-video.xyz")) {
                val key = data.split("/").last()
                val api = app.get("https://tau-video.xyz/api/video/${key}").parsedSafe<TauVideo>() ?: return false

                for (video in api.urls) {
                    callback.invoke(
                        ExtractorLink(
                            source  = "${this.name} - ${video.label}",
                            name    = this.name,
                            url     = video.url,
                            referer = "$mainUrl/",
                            quality = Qualities.Unknown.value,
                            isM3u8  = m3u_link.contains(".m3u8")
                        )
                    )
                }

                return true
            }

            return false
    }

    data class TauVideo(
        @JsonProperty("urls") val urls: List<TauVideoUrl>
    )

    data class TauVideoUrl(
        @JsonProperty("url") val url: String,
        @JsonProperty("label") val label: String,
    )

}