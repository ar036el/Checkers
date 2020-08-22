package el.arn.checkers.managers.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.game_enums.DifficultyEnum
import el.arn.checkers.helpers.game_enums.GameTypeEnum
import el.arn.checkers.helpers.game_enums.StartingPlayerEnum
import el.arn.checkers.game.game_core.checkers_game.configurations.GameLogicConfig.*

class GamePreferencesManager : PreferencesManager(sharedPreferences) {
    companion object {
        private val sharedPreferences: SharedPreferences
            get() = appRoot.getSharedPreferences("game", Context.MODE_PRIVATE)
    }
    //TOdo put a mechanism that detects if a key was used more than once

    val boardSize = createIntPref("boardSize", 4..24 step 2, 8)
    val startingRows = createIntPref("startingRows", 1..11, 3)
    val isCapturingMandatory = createBooleanPref("isCapturingMandatory", true)
    val canPawnCaptureBackwards = createEnumPref("canPawnCaptureBackwards", CanPawnCaptureBackwardsOptions.values(), CanPawnCaptureBackwardsOptions.Never)
    val kingBehaviour = createEnumPref("kingBehaviour", KingBehaviourOptions.values(), KingBehaviourOptions.FlyingKings)

    val boardTheme = createIntPref("boardTheme", 0..3, 0)
    val playersTheme = createIntPref("playersTheme", 0..2, 0)
    val soundEffectsTheme = createIntPref("soundEffectsTheme", 0..3, 1) //todo put this in onboarding?
    val areSoundEffectsEnabled = (soundEffectsTheme.value != 0)

    val gameType = createEnumPref("gameType", GameTypeEnum.values(), GameTypeEnum.SinglePlayer)
    val userPlaysFirst = createBooleanPref("userPlaysFirst", true)
    val startingPlayer = createEnumPref("startingPlayer", StartingPlayerEnum.values(), StartingPlayerEnum.White)

    val difficulty = createEnumPref("difficulty", DifficultyEnum.values(), DifficultyEnum.Easy)

    init {
        boardSize.addListener( object : Pref.Listener<Int> {
            override fun prefHasChanged(pref: Pref<Int>, value: Int) {
                when (value) {
                    8 -> startingRows.value = 3
                    10 -> startingRows.value = 4
                    12 -> startingRows.value = 5
                }
            }

        })
    }
}