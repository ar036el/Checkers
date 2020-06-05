package el.arn.opencheckers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig
import kotlin.math.roundToInt


@ColorInt
fun adjustColorAlpha(@ColorInt color: Int, factor: Float): Int {
    val alpha = (Color.alpha(color) * factor).roundToInt()
    val red: Int = Color.red(color)
    val green: Int = Color.green(color)
    val blue: Int = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}



@DrawableRes
fun getTileColorResDefaultState(x: Int, y: Int): Int {
    val isDarkTile = (x % 2 == y % 2)
    return if (isDarkTile)
        ResourcesByTheme.PlayableTile()
    else
        ResourcesByTheme.NotPlayableTile()
}




enum class ResourcesByTheme(
    @StringRes private val prefKey: Int,
    @IntegerRes private val prefDefaultValue: Int,
    @DrawableRes private vararg val res: Int) {

    PlayableTile(
        R.string.pref_boardTheme,
        R.integer.pref_boardTheme_defaultValue,
        R.color.tile_playable_1,
        R.color.tile_playable_2,
        R.color.tile_playable_3,
        R.color.tile_playable_4
    ),

    NotPlayableTile(
        R.string.pref_boardTheme,
        R.integer.pref_boardTheme_defaultValue,
        R.color.tile_notPlayable_1,
        R.color.tile_notPlayable_2,
        R.color.tile_notPlayable_3,
        R.color.tile_notPlayable_4
    ),

    WhitePawn(
        R.string.pref_playerTheme,
        R.integer.pref_playerTheme_defaultValue,
        R.drawable.piece_white_pawn_1,
        R.drawable.piece_white_pawn_2,
        R.drawable.piece_white_pawn_3
    ),

    WhiteKing(
        R.string.pref_playerTheme,
        R.integer.pref_playerTheme_defaultValue,
        R.drawable.piece_white_king_1,
        R.drawable.piece_white_king_2,
        R.drawable.piece_white_king_3
    ),

    BlackPawn(
        R.string.pref_playerTheme,
        R.integer.pref_playerTheme_defaultValue,
        R.drawable.piece_black_pawn_1,
        R.drawable.piece_black_pawn_2,
        R.drawable.piece_black_pawn_3
    ),

    BlackKing(
        R.string.pref_playerTheme,
        R.integer.pref_playerTheme_defaultValue,
        R.drawable.piece_black_king_1,
        R.drawable.piece_black_king_2,
        R.drawable.piece_black_king_3
    ),

    ExamplePiece(
    R.string.pref_playerTheme,
    R.integer.pref_playerTheme_defaultValue,
    R.drawable.piece_both_players_1,
    R.drawable.piece_both_players_2,
    R.drawable.piece_both_players_3
    );


    private fun restoreToDefaultValue() {
        val defaultValue = resources.getInteger(prefDefaultValue)
        with (sharedPrefs.edit()) {
            putInt(resources.getString(prefKey), defaultValue)
            commit()
        }
    }

    @DrawableRes
    operator fun invoke(): Int {
        val themeIndex = sharedPrefs.getInt(
            resources.getString(prefKey),
            resources.getInteger(prefDefaultValue))
        if (themeIndex < 0 || themeIndex > res.lastIndex) {
            restoreToDefaultValue()
            return res[invoke()]
        }
        return res[themeIndex]
    }

}


fun getBooleanFromSettings(@StringRes prefKey: Int, @BoolRes defaultValue: Int): Boolean {
    return sharedPrefs.getBoolean(
        resources.getString(prefKey),
        resources.getBoolean(defaultValue))
}

fun getIntFromSettings(@StringRes prefKey: Int, @IntegerRes defaultValue: Int): Int {
    return sharedPrefs.getInt(
        resources.getString(prefKey),
        resources.getInteger(defaultValue))
}

fun getStringFromSettings(@StringRes prefKey: Int, @StringRes defaultValue: Int): String {
    return sharedPrefs.getString(
        resources.getString(prefKey),
        resources.getString(defaultValue))!!
}


fun isCustomSettingsEnabled(): Boolean {
    return getBooleanFromSettings(
        R.string.pref_customSettingsEnabled,
        R.bool.pref_customSettingsEnabled_defaultValue)
}

//TODO get all the static strings into consts?

object getValueFromSettings {

    fun boardSize(): Int {
        return if (isCustomSettingsEnabled()) {
            getStringFromSettings(
                R.string.pref_customBoardSize,
                R.string.pref_customBoardSize_defaultValue
            ).toInt()
        } else {
            getStringFromSettings(
                R.string.pref_boardSize,
                R.string.pref_boardSize_defaultValue
            ).toInt()
        }
    }

    fun startingRows(): Int {
        return if (isCustomSettingsEnabled()) {
            getStringFromSettings(
                R.string.pref_customStartingRows,
                R.string.pref_customStartingRows_defaultValue
            ).toInt()
        } else {
            resources.getInteger(R.integer.starting_rows_defaultValue)
        }
    }

    fun isCapturingMandatory(): Boolean {
        return getBooleanFromSettings(
                R.string.pref_isCapturingMandatory,
                R.bool.pref_isCapturingMandatory_defaultValue
            )
    }

    fun kingBehaviour(): GameLogicConfig.KingBehaviourOptions {
        val prefValue = getStringFromSettings(
            R.string.pref_kingBehaviour,
            R.string.pref_kingBehaviour_defaultValue)
        val prefValues = resources.getStringArray(R.array.pref_kingBehaviour_entryValues)
        return when (prefValue) {
            prefValues[0] -> GameLogicConfig.KingBehaviourOptions.FlyingKings
            prefValues[1] -> GameLogicConfig.KingBehaviourOptions.LandsRightAfterCapture
            prefValues[2] -> GameLogicConfig.KingBehaviourOptions.NoFlyingKings
            else -> throw NotImplementedError()
        }
    }

    fun canPawnCaptureBackwards(): GameLogicConfig.CanPawnCaptureBackwardsOptions {
        val prefValue = getStringFromSettings(
            R.string.pref_canManCaptureBackwards,
            R.string.pref_canManCaptureBackwards_defaultValue)
        val prefValues = resources.getStringArray(R.array.pref_canManCaptureBackwards_entryValues)
        return when (prefValue) {
            prefValues[0] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.Always
            prefValues[1] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.OnlyWhenMultiCapture
            prefValues[2] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.Never
            else -> throw NotImplementedError()
        }
    }

}

class GatedFunction(startingOpen: Boolean = false, private val func: () -> Unit) {
    private var open = startingOpen

    fun giveOneAccess() {
        open = true
    }

    fun invokeIfOpen(): Boolean {
        if (open) {
            open = false
            func.invoke()
            return true
        } else {
            return false
        }
    }

}

val View.activity: Activity?
    get() {
    var context: Context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

val Activity?.isAlive: Boolean
    get() = (this?.isDestroyed == false)