// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

open class SetPlay : ExtractorApi() {
    override val name            = "SetPlay"
    override val mainUrl         = "https://setplay.site"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref   = referer ?: ""
        val partKey   = url.substringAfter("?partKey=")?.substringAfter("turkce")?.uppercase()
        val url       = url.substringBefore("?partKey=")
        val i_source  = app.post(url.replace("embed?i=", "embed/get?i="), referer=url).text

        val links = Regex("""Links\":\[\"([^\"\]]+)""").find(i_source)?.groupValues?.get(1) ?: throw ErrorLoadingException("Links not found")
        if (!links.startsWith("/")) {
            throw ErrorLoadingException("Links not valid")
        }

        val m3u_link = "${mainUrl}${links}"
        Log.d("Kekik_${this.name}", "m3u_link » ${m3u_link}")

        callback.invoke(
            ExtractorLink(
                source  = this.name,
                name    = if (partKey != "") "${this.name} - ${partKey}" else this.name,
                url     = m3u_link,
                referer = url,
                quality = Qualities.Unknown.value,
                isM3u8  = true
            )
        )
    }
}