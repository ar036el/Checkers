package el.arn.checkers.managers.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.checkers.appRoot
import el.arn.checkers.game.game_core.checkers_game.configurations.GameLogicConfig
import el.arn.checkers.managers.purchase_manager.PurchasesManager

class SettingsPreferencesManagerAsBridge(gamePreferencesManager: GamePreferencesManager, purchasesManager: PurchasesManager) : PreferencesManager(sharedPreferences) {
    //main use of this class is to be a bridge between the settings activity and the main game preferences

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
    val soundEffectsTheme = createIntPref("soundEffectsTheme", gamePreferencesManager.soundEffectsTheme.possibleValues, gamePreferencesManager.soundEffectsTheme.value) //todo try to generift it all. like this. to not be duplicated like this, and for sure: the pref keys must be a res. make a resToString(R...), resToInt(R...)....

    val isCustomSettingsEnabled = createBooleanPref("isCustomSettingsEnabled", (purchasesManager.purchasedPremiumVersion))
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
        }) //todo all these except custom shit needs generification via a function
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
        soundEffectsTheme.addListener( object : Pref.Listener<Int> {
            override fun prefHasChanged(pref: Pref<Int>, value: Int) {
                gamePreferencesManager.soundEffectsTheme.value = value
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