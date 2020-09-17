/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.appRoot
import el.arn.ultimatecheckers.game.game_core.checkers_game.configurations.GameLogicConfig
import el.arn.ultimatecheckers.helpers.android.stringFromRes
import el.arn.ultimatecheckers.managers.purchase_manager.PurchasesManager

class SettingsPreferencesManagerAsBridge(
    private val gamePrefsMgr: GamePreferencesManager,
    purchasesManager: PurchasesManager
) : PreferencesManagerImpl(sharedPreferences) {
    //main use of this class is to be a bridge between the settings activity and the main game preferences

    companion object {
        private val sharedPreferences: SharedPreferences
            get() = appRoot.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    val kingBehaviour = createStringPref(gamePrefsMgr.kingBehaviour.key, gamePrefsMgr.kingBehaviour.possibleValues!!.map { it.id }, gamePrefsMgr.kingBehaviour.value.id)
    val canPawnCaptureBackwards = createStringPref(gamePrefsMgr.canPawnCaptureBackwards.key, gamePrefsMgr.canPawnCaptureBackwards.possibleValues!!.map { it.id }, gamePrefsMgr.canPawnCaptureBackwards.value.id)
    val isCapturingMandatory = createBooleanPref(gamePrefsMgr.isCapturingMandatory.key, gamePrefsMgr.isCapturingMandatory.value)
///------
    val boardSizeRegular = createStringPref(stringFromRes(R.string.internal_prefKey_settings_boardSizeRegular), gamePrefsMgr.boardSize.possibleValues!!.map { it.toString() }, gamePrefsMgr.boardSize.value.toString())
    val boardTheme = createIntPref(gamePrefsMgr.boardTheme.key, gamePrefsMgr.boardTheme.possibleValues!!, gamePrefsMgr.boardTheme.value)
    val playersTheme = createIntPref(gamePrefsMgr.playersTheme.key, gamePrefsMgr.playersTheme.possibleValues!!, gamePrefsMgr.playersTheme.value)
    val soundEffectsTheme = createIntPref(gamePrefsMgr.soundEffectsTheme.key, gamePrefsMgr.soundEffectsTheme.possibleValues!!, gamePrefsMgr.soundEffectsTheme.value)
///------
    val isCustomSettingsEnabled = createBooleanPref(stringFromRes(R.string.internal_prefKey_settings_isCustomSettingsEnabled), (purchasesManager.purchasedPremiumVersion))
    val boardSizeCustom = createStringPref(stringFromRes(R.string.internal_prefKey_settings_boardSizeCustom), gamePrefsMgr.boardSize.possibleValues!!.map { it.toString() }, gamePrefsMgr.boardSize.value.toString())
    val startingRows = createStringPref(gamePrefsMgr.startingRows.key, gamePrefsMgr.startingRows.possibleValues!!.map { it.toString() }, gamePrefsMgr.startingRows.value.toString())


    init {
        attachAllPrefsToOriginalGamePrefs(gamePrefsMgr)
    }

    private fun attachAllPrefsToOriginalGamePrefs(gamePreferencesManager: GamePreferencesManager) {

        kingBehaviour.addListener( object : Preference.Listener<String> {
            override fun prefHasChanged(preference: Preference<String>, value: String) {
                gamePreferencesManager.kingBehaviour.value = GameLogicConfig.KingBehaviourOptions.values().first { it.id == value}
            }
        })
        canPawnCaptureBackwards.addListener( object : Preference.Listener<String> {
            override fun prefHasChanged(preference: Preference<String>, value: String) {
                gamePreferencesManager.canPawnCaptureBackwards.value = GameLogicConfig.CanPawnCaptureBackwardsOptions.values().first { it.id == value}
            }
        })
        isCapturingMandatory.addListener( object : Preference.Listener<Boolean> {
            override fun prefHasChanged(preference: Preference<Boolean>, value: Boolean) {
                gamePreferencesManager.isCapturingMandatory.value = value
            }
        })

        attachToOriginalPrefByPrefListener_stringToInt(boardSizeRegular, gamePreferencesManager.boardSize)
        attachToOriginalPrefByPrefListener_intToInt(playersTheme, gamePreferencesManager.playersTheme)
        attachToOriginalPrefByPrefListener_intToInt(boardTheme, gamePreferencesManager.boardTheme)
        attachToOriginalPrefByPrefListener_intToInt(soundEffectsTheme, gamePreferencesManager.soundEffectsTheme)

        isCustomSettingsEnabled.addListener( object : Preference.Listener<Boolean> {
            override fun prefHasChanged(preference: Preference<Boolean>, isEnabled: Boolean) {
                updateGamePrefsByIsCustomSettingEnabled(isEnabled)
            }
        })

        attachToOriginalPrefByPrefListener_stringToInt(boardSizeCustom, gamePreferencesManager.boardSize)
        attachToOriginalPrefByPrefListener_stringToInt(startingRows, gamePreferencesManager.startingRows)
    }

    private fun updateGamePrefsByIsCustomSettingEnabled(isCustomSettingEnabled: Boolean) {
        if (isCustomSettingEnabled) {
            gamePrefsMgr.boardSize.value = boardSizeCustom.value.toInt()
            gamePrefsMgr.startingRows.value = startingRows.value.toInt()
        } else {
            gamePrefsMgr.boardSize.value = boardSizeRegular.value.toInt()
        }
        gamePrefsMgr.areCustomStartingRowsEnabled.value = isCustomSettingEnabled
    }

    private fun attachToOriginalPrefByPrefListener_stringToInt(settingsPreference: Preference<String>, gamePreference: Preference<Int>) {
        settingsPreference.addListener( object : Preference.Listener<String> {
            override fun prefHasChanged(preference: Preference<String>, value: String) {
                gamePreference.value = value.toInt()
            }
        })
    }
    private fun attachToOriginalPrefByPrefListener_intToInt(settingsPreference: Preference<Int>, gamePreference: Preference<Int>) {
        settingsPreference.addListener( object : Preference.Listener<Int> {
            override fun prefHasChanged(preference: Preference<Int>, value: Int) {
                gamePreference.value = value
            }
        })
    }

}