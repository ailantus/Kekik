// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
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
            "$mainUrl/secure/titles?genre=action&onlyStreamable=true"  to "Aksiyon",
            "$mainUrl/secure/titles?genre=mystery&onlyStreamable=true" to "Gizem",
            "$mainUrl/secure/titles?genre=drama&onlyStreamable=true"   to "Dram",
            "$mainUrl/secure/titles?genre=comedy&onlyStreamable=true"  to "Komedi",
            "$mainUrl/secure/titles?genre=horror&onlyStreamable=true"  to "Korku",
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
        @JsonProperty("name") val title: String,
        @JsonProperty("poster") val poster: String,
    )

    data class Anime(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val title: String,
        @JsonProperty("poster") val poster: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("year") val year: Int?,
        @JsonProperty("mal_vote_average") val rating: String?,
        @JsonProperty("genres") val tags: List<Genre?> = emptyList(),
        @JsonProperty("credits") val actors: List<Credit?> = emptyList(),
    )

    data class Genre(
        @JsonProperty("display_name") val name: String,
    )

    data class Credit(
        @JsonProperty("name") val name: String,
        @JsonProperty("poster") val poster: String,
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(request.data + "&page=${page}&perPage=12").parsedSafe<Category>()

        val home     = response?.pagination?.data?.mapNotNull { anime ->
            newAnimeSearchResponse(
                anime.title,
                "$mainUrl/secure/titles/${anime.id}",
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
                "$mainUrl/secure/titles/${anime.id}",
                TvType.Anime
            ) {
                this.posterUrl = anime.poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val response = app.get(url).parsedSafe<Title>() ?: return null

        val episodes = emptyList<Episode>()

        return newTvSeriesLoadResponse(
            response.title.title,
            "$mainUrl/secure/titles/${anime.id}",
            TvType.Anime,
            episodes
        ) {
            this.posterUrl = response.title.poster
            this.year      = response.title.year
            this.plot      = response.title.description
            this.tags      = response.title.tags?.filterNotNull()?.map { it.name }
            this.rating    = response.title.rating.toRatingInt()
            addActors(response.title.actors?.filterNotNull()?.map { Actor(it.name, it.poster) })
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
        ): Boolean {

            return false
    }
}