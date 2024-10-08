 package com.lagradost

 import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
 import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
 import com.lagradost.cloudstream3.R
 import com.lagradost.cloudstream3.app
 import com.lagradost.cloudstream3.syncproviders.AccountManager
 import com.lagradost.cloudstream3.syncproviders.AuthAPI
 import com.lagradost.cloudstream3.syncproviders.InAppAuthAPI
 import com.lagradost.cloudstream3.syncproviders.InAppAuthAPIManager

 class FStreamProviderUrlsApi(index: Int) : InAppAuthAPIManager(index) {
     override val name = "Provider Url"
     override val idPrefix = "fstreamProviderUrl"
     override val icon = R.drawable.ic_baseline_extension_24
     override val requiresUsername = false
     override val requiresPassword = false
     override val requiresServer = true
     override val createAccountUrl = ""

     companion object {
         const val PROVIDER_URL_KEY: String = "provider_url_user"
     }

     override fun getLatestLoginData(): InAppAuthAPI.LoginData? {
         return getKey(accountId, PROVIDER_URL_KEY)
     }

     override fun loginInfo(): AuthAPI.LoginInfo? {
         val data = getLatestLoginData() ?: return null
         return AuthAPI.LoginInfo(name = data.username ?: data.server, accountIndex = accountIndex)
     }

     fun providerUrl(): AuthAPI.LoginInfo? {
         val data = getLatestProviderData() ?: return null
         return AuthAPI.LoginInfo(name = data.server,  accountIndex = accountIndex)
     }

     private fun getLatestProviderData(): InAppAuthAPI.LoginData? {
         return getKey(accountId, PROVIDER_URL_KEY)
     }


     override suspend fun login(data: InAppAuthAPI.LoginData): Boolean {
         if (data.server.isNullOrBlank()) return false // we require a server

         switchToNewAccount()
         setKey(accountId, PROVIDER_URL_KEY, data)
         registerAccount()
         initialize()
         AccountManager.inAppAuths
         return true
     }

     override fun logOut() {
         removeAccountKeys()
         //initializeData()
     }


     override suspend fun initialize() {
         //initializeUsedProviders()
         //initializeData()
     }
 }
