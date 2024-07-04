// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class TurboImgz : ExtractorApi() {
    override val name            = "TurboImgz"
    override val mainUrl         = "https://turbo.imgz.me"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref   = referer ?: ""
        val video_req = app.get(url.substringAfter("||"), referer=ext_ref).text

        val video_link = Regex("""file: "(.*)",""").find(video_req)?.groupValues?.get(1) ?: throw ErrorLoadingException("File not found")
        Log.d("Kekik_${this.name}", "video_link » ${video_link}")

        callback.invoke(
            ExtractorLink(
                source  = "${this.name} - " + url.substringBefore("||").uppercase(),
                name    = "${this.name} - " + url.substringBefore("||").uppercase(),
                url     = video_link,
                referer = ext_ref,
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )
    }
}