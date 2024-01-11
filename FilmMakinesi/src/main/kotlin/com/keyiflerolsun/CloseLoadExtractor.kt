// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class CloseLoad : ExtractorApi() {
    override val name            = "CloseLoad"
    override val mainUrl         = "https://closeload.filmmakinesi.film"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref  = referer ?: ""
        Log.d("Kekik_${this.name}", "url » ${url}")

        val i_source = app.get(url, referer=ext_ref).text
        val m3u_link = Regex("""contentUrl\": \"([^\"]+)""").find(i_source)?.groupValues?.get(1)
        if (m3u_link != null) {
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
}