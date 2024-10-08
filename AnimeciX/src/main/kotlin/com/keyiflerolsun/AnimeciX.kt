 package com.lagradost

 import com.fasterxml.jackson.core.type.TypeReference
 import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
 import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
 import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
 import com.lagradost.cloudstream3.R
 import com.lagradost.cloudstream3.app
 import com.lagradost.cloudstream3.syncproviders.AccountManager
 import com.lagradost.cloudstream3.syncproviders.AuthAPI
 import com.lagradost.cloudstream3.syncproviders.InAppAuthAPI
 import com.lagradost.cloudstream3.syncproviders.InAppAuthAPIManager
 import com.lagradost.utils.FStreamUtils.listOfProviders
 import com.lagradost.utils.FStreamUtils.writeToKey

 class FStreamApi(index: Int) : InAppAuthAPIManager(index) {
     override val name = "FStream"
     override val idPrefix = "fstream"
     override val icon = R.drawable.ic_baseline_extension_24
     override val requiresUsername = true
     override val requiresPassword = true
     override val requiresServer = false
     override val createAccountUrl = "https://alldebrid.com/register/"

     companion object {
         const val ALLDEBRID_USER_KEY: String = "alldebrid_user"
         const val USED_PROVIDERS_V3: String = "used_fstream_providers_v3"
         const val FSTREAM_VERSION: String = "fstream_version"
         const val PROVIDER_URL_TEMP_USER_KEY: String = "provider_url_user"
         const val currentFstreamVersion: Int = 15
     }

     override fun getLatestLoginData(): InAppAuthAPI.LoginData? {
         return getKey(accountId, ALLDEBRID_USER_KEY)
     }

     override fun loginInfo(): AuthAPI.LoginInfo? {
         val data = getLatestLoginData() ?: return null
         return AuthAPI.LoginInfo(name = data.username ?: data.server, accountIndex = accountIndex)
     }

     override suspend fun login(data: InAppAuthAPI.LoginData): Boolean {
         if (data.username.isNullOrBlank() || data.password.isNullOrBlank()) return false // we require a server
         try {
             val isValid = app.get("http://api.alldebrid.com/v4/user?agent=${data.username}&apikey=${data.password}").text.contains("\"status\": \"success\",")
             if(!isValid) return false
         } catch (e: Exception) {
             return false
         }

         switchToNewAccount()
         setKey(accountId, ALLDEBRID_USER_KEY, data)
         registerAccount()
         initialize()
         AccountManager.inAppAuths

         return true
     }

     override fun logOut() {
         removeAccountKeys()
         //initializeData()
     }

     fun jsonToMap(json: String): MutableMap<String, Pair<Int, String?>> {
         val objectMapper = jacksonObjectMapper()
         return objectMapper.readValue(json, object : TypeReference<MutableMap<String, Pair<Int, String?>>>() {})
     }


     fun parseKey(key: String): MutableMap<String, Pair<Int, String?>>? {
         val dataFromKey: String = getKey(key) ?: return null
         return jsonToMap(dataFromKey)
     }
     private fun initializeUsedProviders() {
         val defaultData: MutableMap<String, Pair<Int, String?>> = listOfProviders
         val storedData: MutableMap<String, Pair<Int, String?>> = parseKey(USED_PROVIDERS_V3) ?: defaultData
         //println(storedData)
         listOfProviders = storedData.takeIf { storedData.keys == defaultData.keys && storedData.values.first().first is Int} ?: defaultData
     }

     /*private fun initializeData() {
         val data = getLatestLoginData() ?: run {
             FStreamProvider.blackInkApiAppName = null
             FStreamProvider.blackInkApiKey = null
             return
         }
         FStreamProvider.blackInkApiAppName = data.username
         FStreamProvider.blackInkApiKey = data.password
     }*/

     override suspend fun initialize() {
         //RepoLinkGenerator.cache.clear()
         val version: Int? = getKey(FSTREAM_VERSION)
         if (version == currentFstreamVersion) {
             initializeUsedProviders() // only use the stored data if its the current version
         } else {
             writeToKey(USED_PROVIDERS_V3, listOfProviders)
             setKey(FSTREAM_VERSION, currentFstreamVersion) // keep up to date the key
             // dont initialize the providers (we dont want new urls from the dev to be overwritten)
         }
         //initializeData()
     }
 }
