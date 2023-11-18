// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class KoreaSearch(
    @JsonProperty("theme") val theme: String
)

data class VideoSeyred(
    @JsonProperty("image") val image: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("sources") val sources: List<VSSource>,
    @JsonProperty("tracks") val tracks: List<VSTrack>
)

data class VSSource(
    @JsonProperty("file") val file: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("default") val default: String
)

data class VSTrack(
    @JsonProperty("file") val file: String,
    @JsonProperty("kind") val kind: String,
    @JsonProperty("language") val language: String? = null,
    @JsonProperty("label") val label: String? = null,
    @JsonProperty("default") val default: String? = null
)