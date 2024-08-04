version = 9

cloudstream {
    authors     = listOf("keyiflerolsun")
    language    = "tr"
    description = "Yabancı dizi izle, anime izle, en popüler yabancı dizileri ve animeleri ücretsiz olarak diziwatch.net'te izleyin."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=diziwatch.net&sz=%size%"
}