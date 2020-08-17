package el.arn.opencheckers.tools.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig
import el.arn.opencheckers.tools.purchase_manager.PurchasesManager
import el.arn.opencheckers.tools.purchase_manager.core.PurchaseStatus

class SettingsPreferencesManager(gamePreferencesManager: GamePreferencesManager, purchasesManager: PurchasesManager) : PreferencesManager(sharedPreferences) {
    companion object {
        private val sharedPreferences: SharedPreferences
            get() = appRoot.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    val kingBehaviour = createStringPref("kingBehaviour", GameLogicConfig.KingBehaviourOptions.values().map { it.id }, gamePreferencesManager.kingBehaviour.value.id)
    val canPawnCaptureBackwards = createStringPref("canPawnCaptureBackwards", GameLogicConfig.CanPawnCaptureBackwardsOptions.values().map { it.id }, gamePreferencesManager.canPawnCaptureBackwards.value.id)
    val isCapturingMandatory = createBooleanPref("isCapturingMandatory", gamePreferencesManager.isCapturingMandatory.value)

    val boardSizeRegular = createStringPref("boardSizeRegular", (4..24 step 2).map { it.toString() }, gamePreferencesManager.boardSize.value.toString())
    val boardTheme = createIntPref("boardTheme", 0..3, gamePreferencesManager.boardTheme.value)
    val playersTheme = createIntPref("playersTheme", 0..2, gamePreferencesManager.playersTheme.value)

    val isCustomSettingsEnabled = createBooleanPref("isCustomSettingsEnabled", (purchasesManager.purchasedFullVersion))
    val boardSizeCustom = createStringPref("boardSizeCustom", (4..24 step 2).map { it.toString() }, gamePreferencesManager.boardSize.value.toString())
    val startingRows = createStringPref("startingRows", (1..11).map { it.toString() }, gamePreferencesManager.startingRows.value.toString())

    init {
        attachAllPrefsToGamePreferencesManager(gamePreferencesManager)
    }

    private fun attachAllPrefsToGamePreferencesManager(gamePreferencesManager: GamePreferencesManager) {
        kingBehaviour.addListener( object : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                gamePreferencesManager.kingBehaviour.value = GameLogicConfig.KingBehaviourOptions.values().first { it.id == value}
            }
        })
        canPawnCaptureBackwards.addListener( object : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                gamePreferencesManager.canPawnCaptureBackwards.value = GameLogicConfig.CanPawnCaptureBackwardsOptions.values().first { it.id == value}
            }
        })
        isCapturingMandatory.addListener( object : Pref.Listener<Boolean> {
            override fun prefHasChanged(pref: Pref<Boolean>, value: Boolean) {
                gamePreferencesManager.isCapturingMandatory.value = value
            }
        })
        boardSizeRegular.addListener( object  : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                gamePreferencesManager.boardSize.value = value.toInt()
            }
        })
        boardTheme.addListener( object : Pref.Listener<Int> {
            override fun prefHasChanged(pref: Pref<Int>, value: Int) {
                gamePreferencesManager.boardTheme.value = value
            }
        })
        playersTheme.addListener( object : Pref.Listener<Int> {
            override fun prefHasChanged(pref: Pref<Int>, value: Int) {
                gamePreferencesManager.playersTheme.value = value
            }
        })
        boardSizeCustom.addListener( object : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                gamePreferencesManager.boardSize.value = value.toInt()
            }
        })
        startingRows.addListener( object : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                gamePreferencesManager.startingRows.value = value.toInt()
            }
        })

        isCustomSettingsEnabled.addListener( object : Pref.Listener<Boolean> {
            override fun prefHasChanged(pref: Pref<Boolean>, isEnabled: Boolean) {
                if (isEnabled) {
                    gamePreferencesManager.boardSize.value = boardSizeCustom.value.toInt()
                    gamePreferencesManager.startingRows.value = startingRows.value.toInt()
                } else {
                    gamePreferencesManager.boardSize.value = boardSizeRegular.value.toInt()
                }
            }

        })
    }

}