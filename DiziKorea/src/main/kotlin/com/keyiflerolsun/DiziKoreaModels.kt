// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class KoreaSearch(
    @JsonProperty("theme") val theme: String
)


data class VideoSeyred(
    @JsonProperty("image")   val image: String?              = null,
    @JsonProperty("title")   val title: String?              = null,
    @JsonProperty("sources") val sources: List<SeyredSource> = emptyList(),
    @JsonProperty("tracks")  val tracks: List<SeyredTrack>   = emptyList()
)

data class SeyredSource(
    @JsonProperty("file") val file: String,
)

data class SeyredTrack(
    @JsonProperty("file")  val file: String,
    @JsonProperty("kind")  val kind: String,
    @JsonProperty("label") val label: String? = null,
)