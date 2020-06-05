package el.arn.opencheckers.prefs_managers

import android.content.Context
import android.content.SharedPreferences
import el.arn.opencheckers.App
import el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig.*

class MainPrefsManager : PrefsManager(sharedPreferences) {
    companion object {
        private val sharedPreferences: SharedPreferences
            get() = App.instance.getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    val boardSize = createIntPref("boardSize", 4..24 step 2, 8)
    val startingRows = createIntPref("boardSize", 1..11, 3)
    val isCapturingMandatory = createBooleanPref("isCapturingMandatory", true)
    val canPawnCaptureBackwards = createEnumPref("canPawnCaptureBackwards", CanPawnCaptureBackwardsOptions.values(), CanPawnCaptureBackwardsOptions.Never)
    val kingBehaviour = createStringPref("kingBehaviour", KingBehaviourOptions.values().map { it.id }, KingBehaviourOptions.FlyingKings.toString())

    val boardTheme = createIntPref("boardTheme", 1..3, 1)
    val piecesTheme = createIntPref("piecesTheme", 1..3, 1)


}