// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class SystemResponse(
    @JsonProperty("hls") val hls: String,
    @JsonProperty("videoImage") val videoImage: String? = null,
    @JsonProperty("videoSource") val videoSource: String,
    @JsonProperty("securedLink") val securedLink: String
)

data class PeaceResponse(
    @JsonProperty("videoImage") val videoImage: String,
    @JsonProperty("videoSources") val videoSources: List<VideoSource>,
    @JsonProperty("sIndex") val sIndex: String,
    @JsonProperty("sourceList") val sourceList: Map<String, String>
)

data class VideoSource(
    @JsonProperty("file") val file: String,
    @JsonProperty("label") val label: String,
    @JsonProperty("type") val type: String
)

data class Track(
    @JsonProperty("file") val file: String?,
    @JsonProperty("label") val label: String?,
    @JsonProperty("kind") val kind: String?,
    @JsonProperty("language") val language: String?,
    @JsonProperty("default") val default: String?
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

data class Teve2ApiResponse(
    @JsonProperty("media") val media: Teve2Media
)

data class Teve2Media(
    @JsonProperty("link") val link: Teve2Link
)

data class Teve2Link(
    @JsonProperty("securePath") val securePath: String
)