package el.arn.opencheckers.tools.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.complementaries.game.GameTypes
import el.arn.opencheckers.complementaries.game.StartingPlayerEnum
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig.*

class GamePreferencesManager : PrefsManager(sharedPreferences) {
    companion object {
        private val sharedPreferences: SharedPreferences
            get() = appRoot.getSharedPreferences("main", Context.MODE_PRIVATE)
    }
    //TOdo put a mechanism that detects if a key was used more than once

    val boardSize = createIntPref("boardSize", 4..24 step 2, 8)
    val startingRows = createIntPref("boardSize", 1..11, 3)
    val isCapturingMandatory = createBooleanPref("isCapturingMandatory", true)
    val canPawnCaptureBackwards = createEnumPref("canPawnCaptureBackwards", CanPawnCaptureBackwardsOptions.values(), CanPawnCaptureBackwardsOptions.Never)
    val kingBehaviour = createEnumPref("kingBehaviour", KingBehaviourOptions.values(), KingBehaviourOptions.FlyingKings)

    val boardTheme = createIntPref("boardTheme", 1..4, 1)
    val piecesTheme = createIntPref("piecesTheme", 1..3, 1)

    val gameType = createEnumPref("gameType", GameTypes.values(), GameTypes.SinglePlayer)
    val startingPlayer = createEnumPref("startingPlayer", StartingPlayerEnum.values(), StartingPlayerEnum.White)


}