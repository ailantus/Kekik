import org.jetbrains.kotlin.fir.scopes.impl.overrides
import org.jetbrains.kotlin.konan.properties.Properties

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
}

// use an integer for version numbers
version = 15


android {
    defaultConfig {
        val properties = Properties()
        properties.load(project.rootProject.file("credentials_DO_NOT_SHARE.properties").inputStream())

        buildConfigField("String", "TMDB_API", properties.getProperty("TMDB_API"))
    }
}


cloudstream {
        //overrideUrlPrefix("https://git.disroot.org/ayza/FStream/src/branch/main/")
    buildBranch="main"
    setRepo("ayza", "FStream", "https://raw.githubusercontent.com/ailantus/FStream")
    language = "fr"
    // All of these properties are optional, you can safely remove them

    description = "Simplification de l'ajout des sources, contributions bienvenues !"
    authors = listOf("disroot.org/ayza/FStream")
    iconUrl = "https://raw.githubusercontent.com/ailantus/FStreammain/fstream.png"
    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "TvSeries",
        "Movie",
    )
    requiresResources = true
}
