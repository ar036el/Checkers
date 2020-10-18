/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import el.arn.opencheckers.R
import el.arn.opencheckers.activityWidgets.settingsActivity.BoardThemeSelectorPreference
import el.arn.opencheckers.activityWidgets.settingsActivity.ImageSelectorPreference
import el.arn.opencheckers.activityWidgets.settingsActivity.PlayerThemeSelectorPreference
import el.arn.opencheckers.activityWidgets.settingsActivity.SoundEffectsThemeSelectorPreference
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.gameCore.game_core.checkers_game.configurations.BoardConfig
import el.arn.opencheckers.gameCore.game_core.checkers_game.implementations.BoardConfigImpl
import el.arn.opencheckers.helpers.android.stringFromRes

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsFragment =
            SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, settingsFragment)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }



    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var regularBoardSizePref: ListPreference
        private lateinit var customBoardSizePref: ListPreference
        private lateinit var startingRowsPref: ListPreference
        private lateinit var enableCustomSettingsPref: SwitchPreferenceCompat
        private lateinit var boardThemePref: BoardThemeSelectorPreference
        private lateinit var playersThemePref: PlayerThemeSelectorPreference
        private lateinit var soundEffectsThemePref: SoundEffectsThemeSelectorPreference
        private lateinit var purchasePremiumVersionButton: Preference
        private lateinit var purchaseNoAdsButton: Preference

        private var arePremiumFeaturedLocked = true //lateinit


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "settings"
            setPreferencesFromResource(R.xml.settings, rootKey)

            arePremiumFeaturedLocked = !appRoot.purchasesManager.purchasedPremiumVersion

            findPreferences()
            initCustomSettingsPreferences()
            initImageSelectorPreferences()
            initPurchasePremiumPreferences()
        }

        private fun findPreferences() {
            regularBoardSizePref = findPreference("boardSizeRegular")!!
            customBoardSizePref = findPreference("boardSizeCustom")!!
            startingRowsPref = findPreference("startingRows")!!
            enableCustomSettingsPref = findPreference("isCustomSettingsEnabled")!!
            boardThemePref = findPreference("boardTheme")!!
            playersThemePref = findPreference("playersTheme")!!
            soundEffectsThemePref = findPreference("soundEffectsTheme")!!
            purchasePremiumVersionButton = findPreference("purchasePremiumVersionButtonOnly")!!
            purchaseNoAdsButton = findPreference("purchaseNoAdsButtonOnly")!!
        }

        private fun initCustomSettingsPreferences() {
            val isCustomSettingsEnabled =  if (arePremiumFeaturedLocked) false else preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)
            enableOrDisableCustomSettingsPref(isCustomSettingsEnabled)

            enableCustomSettingsPref.setOnPreferenceChangeListener {
                _, isChecked ->
                val isChecked = isChecked as Boolean
                if (isChecked && arePremiumFeaturedLocked) {
                    Handler().postDelayed({
                        appRoot.toastManager.showShort(stringFromRes(R.string.settingsActivity_toastMessage_premiumFeature))
                        enableOrDisableCustomSettingsPref(false)
                    },500)
                }
                enableOrDisableCustomSettingsPref(isChecked)
                true
            }

            regularBoardSizePref.setOnPreferenceChangeListener { _, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}
            customBoardSizePref.setOnPreferenceChangeListener {_, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}

            updateStartingRowsPrefEntries(enableCustomSettingsPref.isEnabled)
        }

        private fun initPurchasePremiumPreferences() {
            purchasePremiumVersionButton.setOnPreferenceClickListener { openActivityBuyPremiumActivity(); true }
            purchaseNoAdsButton.setOnPreferenceClickListener { openActivityBuyPremiumActivity(); true }
        }

        private fun openActivityBuyPremiumActivity() {
            val buyPremiumActivity = Intent(activity, BuyPremiumActivity::class.java)
            startActivity(buyPremiumActivity)
        }

        private fun enableOrDisableCustomSettingsPref(isEnabled: Boolean) {
            enableCustomSettingsPref.isChecked = isEnabled
            startingRowsPref.isVisible = isEnabled
            customBoardSizePref.isVisible = isEnabled
            regularBoardSizePref.isEnabled = !isEnabled
            startingRowsPref.isEnabled = !arePremiumFeaturedLocked
            customBoardSizePref.isEnabled = !arePremiumFeaturedLocked

            updateStartingRowsPrefEntries(isEnabled)
        }

        private fun initImageSelectorPreferences() {
            fun initPref(pref: ImageSelectorPreference) {
                pref.isLockEnabled = arePremiumFeaturedLocked
                pref.addListener(imageSelectorPreferenceListener)
            }
            initPref(boardThemePref)
            initPref(playersThemePref)
            initPref(soundEffectsThemePref)
        }

        private val imageSelectorPreferenceListener = object : ImageSelectorPreference.Listener {
            override fun imageWasChanged(imageSelectorPreference: ImageSelectorPreference, currentImageIndex: Int) {
                if (imageSelectorPreference.isCurrentImageLocked) {
                    appRoot.toastManager.showShort(stringFromRes(R.string.settingsActivity_toastMessage_premiumTheme))
                }
            }
        }

        private fun updateStartingRowsPrefEntries(isCustomSettingsEnabled: Boolean) {
            updateStartingRowsPrefEntries(
                preferenceManager.sharedPreferences.getString(
                    if (isCustomSettingsEnabled) {
                        customBoardSizePref.key
                    } else {
                        regularBoardSizePref.key
                    }, "8")!!.toInt()
            )
        }

        private fun updateStartingRowsPrefEntries(boardSize: Int) {
            val isCustomSettingsEnabled =  preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)

            val minStartingRows = BoardConfig.minStartingRowsForEachPlayer
            val maxStartingRows = BoardConfigImpl()
                .maxStartingRowsForEachPlayer(boardSize)
            val entries = Array(maxStartingRows - minStartingRows + 1) { (it +  minStartingRows).toString() }
            startingRowsPref.entries = entries
            startingRowsPref.entryValues = entries

            if (startingRowsPref.value?.toInt() !in minStartingRows..maxStartingRows) {
                startingRowsPref.value =
                    if (isCustomSettingsEnabled && boardSize == 4) {
                        "1"
                    } else if (isCustomSettingsEnabled && boardSize == 6) {
                        "2"
                    } else {
                        "3"
                    }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}