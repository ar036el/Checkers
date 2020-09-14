package el.arn.checkers.managers.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.game_enums.DifficultyEnum
import el.arn.checkers.helpers.game_enums.GameTypeEnum
import el.arn.checkers.helpers.game_enums.StartingPlayerEnum
import el.arn.checkers.game.game_core.checkers_game.configurations.GameLogicConfig.*
import el.arn.checkers.helpers.android.stringFromRes

class GamePreferencesManager : PreferencesManagerImpl(sharedPreferences) {
    companion object {
        private val sharedPreferences: SharedPreferences
            get() = appRoot.getSharedPreferences(stringFromRes(R.string.internal_prefFileKey_game), Context.MODE_PRIVATE)
    }
    //TOdo put a mechanism that detects if a key was used more than once

    val boardSize = createIntPref(stringFromRes(R.string.internal_prefKey_boardSize), 4..24 step 2, 8)
    val startingRows = createIntPref(stringFromRes(R.string.internal_prefKey_startingRows), 1..11, 3)
    val areCustomStartingRowsEnabled = createBooleanPref(stringFromRes(R.string.internal_prefKey_areCustomStartingRowsEnabled), false)

    val isCapturingMandatory = createBooleanPref(stringFromRes(R.string.internal_prefKey_isCapturingMandatory), true)
    val canPawnCaptureBackwards = createEnumPref(stringFromRes(R.string.internal_prefKey_canPawnCaptureBackwards), CanPawnCaptureBackwardsOptions.values(), CanPawnCaptureBackwardsOptions.Never)
    val kingBehaviour = createEnumPref(stringFromRes(R.string.internal_prefKey_kingBehaviour), KingBehaviourOptions.values(), KingBehaviourOptions.FlyingKings)

    val boardTheme = createIntPref(stringFromRes(R.string.internal_prefKey_boardTheme), 0..4, 0)
    val playersTheme = createIntPref(stringFromRes(R.string.internal_prefKey_playersTheme), 0..9, 0)
    val soundEffectsTheme = createIntPref(stringFromRes(R.string.internal_prefKey_soundEffectsTheme), 0..3, 1) //todo put this in onboarding?
    val areSoundEffectsEnabled = (soundEffectsTheme.value != 0)

    val gameType = createEnumPref(stringFromRes(R.string.internal_prefKey_gameType), GameTypeEnum.values(), GameTypeEnum.SinglePlayer)
    val userPlaysFirst = createBooleanPref(stringFromRes(R.string.internal_prefKey_userPlaysFirst), true)
    val startingPlayer = createEnumPref(stringFromRes(R.string.internal_prefKey_startingPlayer), StartingPlayerEnum.values(), StartingPlayerEnum.White)
    val difficulty = createEnumPref(stringFromRes(R.string.internal_prefKey_difficulty), DifficultyEnum.values(), DifficultyEnum.Easy)



    init {
        boardSize.addListener( object : Preference.Listener<Int> {
            override fun prefHasChanged(preference: Preference<Int>, value: Int) {
                if (!areCustomStartingRowsEnabled.value) {
                    adjustStartingRowsByBoardSizeAsRegularCheckersRules(value)
                }
            }
        })
        areCustomStartingRowsEnabled.addListener( object : Preference.Listener<Boolean> {
            override fun prefHasChanged(preference: Preference<Boolean>, value: Boolean) {
                if (!areCustomStartingRowsEnabled.value) {
                    adjustStartingRowsByBoardSizeAsRegularCheckersRules(boardSize.value)
                }
            }
        })
    }

    private fun adjustStartingRowsByBoardSizeAsRegularCheckersRules(boardSize: Int) {
        when (boardSize) {
            8 -> startingRows.value = 3
            10 -> startingRows.value = 4
            12 -> startingRows.value = 5
        }
    }
}