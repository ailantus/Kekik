// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import com.lagradost.cloudstream3.*
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
            "$mainUrl/secure/titles?genre=action&onlyStreamable=true" to "Aksiyon",
            "$mainUrl/secure/titles?genre=comedy&onlyStreamable=true" to "Komedi",
            "$mainUrl/secure/titles?genre=drama&onlyStreamable=true"  to "Dram",
            "$mainUrl/secure/titles?genre=harem&onlyStreamable=true"  to "Harem"
        )

    data class Category(
        @JsonProperty("pagination") val pagination: Pagination,
    )

    data class Search(
        @JsonProperty("results") val results: List<Anime>,
    )

    data class Pagination(
        @JsonProperty("current_page") val current_page: Int,
        @JsonProperty("last_page") val last_page: Int,
        @JsonProperty("per_page") val per_page: Int,
        @JsonProperty("data") val data: List<Anime>,
        @JsonProperty("total") val total: Int,
    )

    data class Anime(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val title: String,
        @JsonProperty("poster") val poster: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("year") val year: Int?,
        @JsonProperty("mal_vote_average") val rating: Float?,
        @JsonProperty("genres") val tags: List<Genre>,
    )

    data class Genre(
        @JsonProperty("display_name") val name: String,
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get(request.data + "&page=${page}&perPage=10").parsedSafe<Category>()

        val home     = response?.pagination?.data?.mapNotNull { anime ->
            newTvSeriesSearchResponse(
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
            newTvSeriesSearchResponse(
                anime.title,
                "$mainUrl/secure/titles/${anime.id}",
                TvType.Anime
            ) {
                this.posterUrl = anime.poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        return null
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