package com.lagradost

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.ui.player.RepoLinkGenerator
import com.lagradost.extractors.BlackInkExtractor
import com.lagradost.extractors.DoodsOZExtractor
import com.lagradost.extractors.DoodsProExtractor
import com.lagradost.extractors.DoodsZeroExtractor
import com.lagradost.extractors.DooodsNetExtractor
import com.lagradost.extractors.Ds2playExtractor
import com.lagradost.extractors.FembedOneExtractor
import com.lagradost.extractors.Mela

@CloudstreamPlugin
class FStreamProviderPlugin : Plugin() {
    private val fstreamApi = FStreamApi(0)
    private val fstreamProviderUrlApi = FStreamProviderUrlsApi(0)

    override fun load(context: Context) {
        // All providers should be added in this manner. Please don't edit the providers list directly.
        fstreamApi.init()
        registerMainAPI(FStreamProvider())
        registerExtractorAPI(DoodsProExtractor())
        registerExtractorAPI(DoodsZeroExtractor())
        registerExtractorAPI(DoodsOZExtractor())
        registerExtractorAPI(DooodsNetExtractor())
        //registerExtractorAPI(Fastream())
        registerExtractorAPI(Ds2playExtractor())
        registerExtractorAPI(Mela())
        registerExtractorAPI(BlackInkExtractor())
        registerExtractorAPI(FembedOneExtractor())
        ioSafe {
            fstreamApi.initialize()
        }
    }

    init {
        //RepoLinkGenerator.cache.clear() // this is for debug to clear caches =================
        this.openSettings = {
            val activity = it as? AppCompatActivity
            if (activity != null) {
                val frag = FStreamSettingsFragment(this, fstreamApi, fstreamProviderUrlApi)
                frag.show(activity.supportFragmentManager, fstreamApi.name)
            }
        }
    }
}
