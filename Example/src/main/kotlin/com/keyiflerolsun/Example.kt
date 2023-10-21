// ! https://gitdab.com/recloudstream/plugin-template/src/branch/master/ExampleProvider/src/main/kotlin/com/example/ExamplePlugin.kt

package com.keyiflerolsun

import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse

class Example : MainAPI() {
    override var mainUrl        = "https://example.com/" 
    override var name           = "Example"
    override val hasMainPage    = true
    override var lang           = "tr"
    override val supportedTypes = setOf(TvType.Movie)

    // this function gets called when you search for something
    override suspend fun search(query: String): List<SearchResponse> {
        return listOf<SearchResponse>()
    }
}