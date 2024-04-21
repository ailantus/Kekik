// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class VidMoly : ExtractorApi() {
    override val name            = "VidMoly"
    override val mainUrl         = "https://vidmoly.to"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref  = referer ?: ""
        val headers  = mapOf(
            "User-Agent"     to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36",
            "Sec-Fetch-Dest" to "iframe"
        )
        val i_source = app.get(url, headers=headers, referer=ext_ref).text
        var m3u_link = Regex("""file:\"([^\"]+)""").find(i_source)?.groupValues?.get(1) ?: throw ErrorLoadingException("m3u link not found")

        Log.d("Kekik_${this.name}", "m3u_link » ${m3u_link}")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = m3u_link,
                referer = ext_ref,
                quality = Qualities.Unknown.value,
                type    = INFER_TYPE
            )
        )
    }
}