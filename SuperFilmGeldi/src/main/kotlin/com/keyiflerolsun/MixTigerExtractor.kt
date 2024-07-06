// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.annotation.JsonProperty

open class MixTiger : ExtractorApi() {
    override val name            = "MixTiger"
    override val mainUrl         = "https://www.mixtiger.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val m3u_link:String?
        val ext_ref  = referer ?: ""
        val post_url = "${url}?do=getVideo"
        Log.d("Kekik_${this.name}", "post_url » ${post_url}")

        val response = app.post(
            post_url,
            data = mapOf(
                "hash" to url.substringAfter("video/"),
                "r"    to ext_ref,
                "s"    to ""
            ),
            referer = ext_ref,
            headers = mapOf(
                "Content-Type"     to "application/x-www-form-urlencoded; charset=UTF-8",
                "X-Requested-With" to "XMLHttpRequest"
            )
        )

        val video_response = response.parsedSafe<FirePlayer>() ?: throw ErrorLoadingException("peace response is null")
        Log.d("Kekik_${this.name}", "video_response » ${video_response}")

        if (video_response?.videoSrc != null) {
            m3u_link = video_response.videoSrc
            Log.d("Kekik_${this.name}", "m3u_link » ${m3u_link}")

            loadExtractor(m3u_link, ext_ref, subtitleCallback, callback)
        } else {
            val video_sources  = video_response.videoSources
            if (video_sources.isNotEmpty()) {
                m3u_link = video_sources.lastOrNull()?.file
            } else {
                m3u_link = null
            }

            Log.d("Kekik_${this.name}", "m3u_link » ${m3u_link}")

            callback.invoke(
                ExtractorLink(
                    source  = this.name,
                    name    = this.name,
                    url     = m3u_link ?: throw ErrorLoadingException("m3u link not found"),
                    referer = if (m3u_link.contains("disk.yandex")) "" else ext_ref,
                    quality = Qualities.Unknown.value,
                    type    = INFER_TYPE
                )
            )
        }
    }

    data class FirePlayer(
        @JsonProperty("videoSrc")     val videoSrc: String?               = null,
        @JsonProperty("videoSources") val videoSources: List<VideoSource> = emptyList(),
    )

    data class VideoSource(
        @JsonProperty("file")  val file: String,
        @JsonProperty("label") val label: String,
        @JsonProperty("type")  val type: String
    )
}