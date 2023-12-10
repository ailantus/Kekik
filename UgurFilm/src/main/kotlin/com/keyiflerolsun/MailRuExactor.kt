// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.annotation.JsonProperty

open class MailRu : ExtractorApi() {
    override val name            = "MailRu"
    override val mainUrl         = "https://my.mail.ru"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref = referer ?: ""
        Log.d("Kekik_${this.name}", "url » ${url}")

        app.get(url)

        val vid_id     = url.substringAfter("video/embed/").trim()
        val video_req  = app.get("${mainUrl}/+/video/meta/${vid_id}", referer=url).text
        val video_data = AppUtils.tryParseJson<MailRuData>(video_req) ?: throw ErrorLoadingException("Video not found")
        Log.d("Kekik_${this.name}", "video_data » ${video_data}")

        for (video in video_data.videos) {
            Log.d("Kekik_${this.name}", "video » ${video}")

            val video_url = if (video.url.startsWith("//")) "https:${video.url}" else video.url
            Log.d("Kekik_${this.name}", "video_url » ${video_url}")

            callback.invoke(
                ExtractorLink(
                    source  = "${this.name} - ${video.key}",
                    name    = "${this.name} - ${video.key}",
                    url     = video_url,
                    referer = url,
                    quality = Qualities.Unknown.value,
                    isM3u8  = video_url.contains(".m3u8")
                )
            )
        }
    }

    data class MailRuData(
        @JsonProperty("provider") val provider: String,
        @JsonProperty("videos")   val videos: List<MailRuVideoData>
    )

    data class MailRuVideoData(
        @JsonProperty("url") val url: String,
        @JsonProperty("key") val key: String
    )
}
