package com.lagradost

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lagradost.cloudstream3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.FStreamApi.Companion.USED_PROVIDERS_V3

import com.lagradost.cloudstream3.AcraApplication.Companion.openBrowser
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.SearchQuality
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.syncproviders.AuthAPI
import com.lagradost.cloudstream3.syncproviders.InAppAuthAPI
import com.lagradost.cloudstream3.ui.settings.SettingsAccount.Companion.showLoginInfo
import com.lagradost.cloudstream3.ui.settings.SettingsAccount.Companion.addAccount
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showMultiDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showNginxTextInputDialog
import com.lagradost.cloudstream3.utils.UIHelper.colorFromAttribute
import com.lagradost.utils.FStreamUtils.getSourceMainUrl
import com.lagradost.utils.FStreamUtils.isSourceCensored
import com.lagradost.utils.FStreamUtils.listOfProviders
import com.lagradost.utils.FStreamUtils.writeToKey


class FStreamSettingsFragment(private val plugin: Plugin, val fstreamApi: FStreamApi, val providerUrlApi: FStreamProviderUrlsApi) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val id = plugin.resources!!.getIdentifier("fstream_settings", "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
        val layout = plugin.resources!!.getLayout(id)
        return inflater.inflate(layout, container, false)
    }

    private fun <T : View> View.findView(name: String): T {
        val id = plugin.resources!!.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        return this.findViewById(id)
    }

    private fun getDrawable(name: String): Drawable? {
        val id = plugin.resources!!.getIdentifier(name, "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        return ResourcesCompat.getDrawable(plugin.resources!!, id, null)
    }

    private fun getString(name: String): String? {
        val id = plugin.resources!!.getIdentifier(name, "string", BuildConfig.LIBRARY_PACKAGE_NAME)
        return plugin.resources!!.getString(id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val infoView = view.findView<LinearLayout>("fstream_info")
        val infoTextView = view.findView<TextView>("info_main_text")
        val infoSubTextView = view.findView<TextView>("info_sub_text")
        val infoImageView = view.findView<ImageView>("fstream_info_imageview")
        val loginView = view.findView<LinearLayout>("fstream_login")
        val loginTextView = view.findView<TextView>("main_text")
        val loginImageView = view.findView<ImageView>("fstream_login_imageview")

        val settingsView =  view.findView<TextView>("main_settings_text")
        val providerSettingsTitle =  view.findView<TextView>("provider_settings_title")
        val providerSettingsView =  view.findView<LinearLayout>("fstream_provider_settings")


        val providerUrlTiltle =  view.findView<TextView>("provider_url_title")
        val providerUrlsView =  view.findView<LinearLayout>("fstream_url_settings")


        settingsView.text = getString("main_settings_title") ?: "Ayarlar"

        providerSettingsTitle.text = getString("provider_settings_title") ?: "Toggle sources"
        providerUrlTiltle.text = getString("provider_url_settings_title") ?: "Edit source"

        infoTextView.text = getString("alldebrid_info_title") ?: "Alldebrid"
        infoSubTextView.text = getString("alldebrid_info_summary") ?: ""
        infoImageView.setImageDrawable(getDrawable("fstream_logo"))
        infoImageView.imageTintList =
            ColorStateList.valueOf(view.context.colorFromAttribute(R.attr.white))
        loginImageView.setImageDrawable(getDrawable("fstream_logo"))
        loginImageView.imageTintList =
            ColorStateList.valueOf(view.context.colorFromAttribute(R.attr.white))




        // object : View.OnClickListener is required to make it compile because otherwise it used invoke-customs
        infoView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openBrowser(fstreamApi.createAccountUrl)
            }
        })

        providerSettingsView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                    val listOfNames = listOfProviders.keys.toList()
                    val listOfPairs = listOfProviders.values.toList()
                    val indexList = listOfPairs.mapIndexedNotNull { index, pair -> if (pair.first == 1) index else null }
                    activity?.showMultiDialog(
                        listOfNames,
                        indexList,
                        getString("provider_settings_title") ?: "Fournisseurs",
                        {

                        },
                        {
                            listOfProviders.toList().forEachIndexed { index, entry ->
                                val enabled = if (index in it) 1 else 0
                                listOfProviders[entry.first] = Pair(enabled, entry.second.second)
                            }
                            writeToKey(USED_PROVIDERS_V3, listOfProviders)
                        }
                    )
            }
        })



        providerUrlsView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val listOfNames = listOfProviders.keys.toList()
                activity?.showDialog(
                    listOfNames,
                    0,
                    getString("provider_to_select_title") ?: "Source à modifier", // getString(R.)
                    true,
                    {})
                { selection ->
                    val name = listOfNames[selection]
                    val defaultUrl = getSourceMainUrl(name)
                    val storedUrl = listOfProviders[listOfNames[selection]]?.second.toString()
                    val displayedUrl = if (isSourceCensored(name) == true && storedUrl == defaultUrl) { // censored and unchanged url:
                        "[censored]" // dont display the url if censored
                    } else {
                        listOfProviders[listOfNames[selection]]?.second.toString()
                    }
                    activity?.showNginxTextInputDialog(name, displayedUrl, textInputType = null, dismissCallback = {})
                    { newUrl ->
                        if(newUrl.contains("://")) {
                            listOfProviders[name] = Pair(listOfProviders[name]?.first ?: 1, newUrl)
                        } else { // reset the url to default
                            listOfProviders[name] = Pair(listOfProviders[name]?.first ?: 1, defaultUrl)
                        }
                        writeToKey(USED_PROVIDERS_V3, listOfProviders)
                    }
                }
            }
        })

        loginTextView.text = getString("alldebrid_account_title")


        loginView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val info = fstreamApi.loginInfo()
                if (info != null) {
                    showLoginInfo(activity, fstreamApi, info)
                } else {
                    addAccount(activity, fstreamApi)
                }
            }
        })
    }
}
