package com.coxju

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class xHamsterProvider: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(xHamster())
    }
}