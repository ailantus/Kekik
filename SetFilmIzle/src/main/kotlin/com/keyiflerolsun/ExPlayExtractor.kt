// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class ExPlay : ExtractorApi() {
    override val name            = "ExPlay"
    override val mainUrl         = "https://explay.store"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref   = referer ?: ""
        val i_source  = app.get(url, referer=ext_ref).text

        val videoUrl    = Regex("""videoUrl\":\"([^\",\"]+)""").find(i_source)?.groupValues?.get(1) ?: throw ErrorLoadingException("videoUrl not found")
        val videoServer = Regex("""videoServer\":\"([^\",\"]+)""").find(i_source)?.groupValues?.get(1) ?: throw ErrorLoadingException("videoServer not found")
        val title       = Regex("""title\":\"([^\",\"]+)""").find(i_source)?.groupValues?.get(1) ?: ""
        val m3u_link    = "${mainUrl}${videoUrl.replace("\\", "")}?s=${videoServer}"
        Log.d("Kekik_${this.name}", "m3u_link » ${m3u_link}")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = "${this.name} - ${title.split('.').last()}",
                url     = m3u_link,
                referer = url,
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )
    }
}