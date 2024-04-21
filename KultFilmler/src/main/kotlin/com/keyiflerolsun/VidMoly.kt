// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.annotation.JsonProperty

open class VidMoly : ExtractorApi() {
    override val name            = "VidMoly"
    override val mainUrl         = "https://vidmoly.to"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref  = referer ?: ""
        val i_source = app.get(url, referer=ext_ref).document

        var m3u_link = i_source.select("body > script").map { it.data() }.first { it.contains("sources") }.substringAfter("sources: [{file:\"").substringBefore("\"}],")

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