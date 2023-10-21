package com.jacekun

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class XVideosPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(XVideos())
    }
}