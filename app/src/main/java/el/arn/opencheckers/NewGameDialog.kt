package el.arn.opencheckers

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import el.arn.opencheckers.checkers_game.game_core.structs.Player

class NewGameDialog(
    private val activity: Activity,
    applyWhenConfirmed: (startingPlayer: Player, difficulty: Difficulty) -> Unit
) {

    private val dialogContentLayout: ConstraintLayout
    private val startingPlayerPref: ListPreferenceFromButtonsGroup<Player>
    private val gameTypePref: ListPreferenceFromButtonsGroup<GameType>


    init {
        dialogContentLayout = activity.layoutInflater.inflate(R.layout.dialog_new_game, null) as ConstraintLayout

        val builder = AlertDialog.Builder(activity)
            .setTitle(activity.resources.getString(R.string.newGameDialog_title))
            .setView(dialogContentLayout)
//            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.

        val whitePlayer = dialogContentLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_WhitePlayer)
        val blackPlayer = dialogContentLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_BlackPlayer)
        val randomPlayer = dialogContentLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_Random)

        whitePlayer.setImageResource(ResourcesByTheme.WhitePawn())
        blackPlayer.setImageResource(ResourcesByTheme.BlackPawn())
        randomPlayer.setImageResource(ResourcesByTheme.ExamplePiece())

        startingPlayerPref = initStartingPlayerPrefButtons()
        gameTypePref = initGameTypePrefButtons()


        //TODO check for all typos (found stating instead of starting

        builder.let {
            it.setPositiveButton(
                activity.resources.getString(R.string.newGameDialog_confirm))
                {    _, _ -> applyWhenConfirmed(startingPlayerPref.value, Difficulty.Hard) }

            it.setNegativeButton(activity.resources.getString(R.string.newGameDialog_cancel), null)

            it.show()
        }
    }

    private fun initStartingPlayerPrefButtons(): ListPreferenceFromButtonsGroup<Player> {
        return ListPreferenceFromButtonsGroup(
            arrayOf(
                R.id.newGameDialog_SelectPlayerButton_WhitePlayer,
                R.id.newGameDialog_SelectPlayerButton_BlackPlayer,
                R.id.newGameDialog_SelectPlayerButton_Random
            ),
            arrayOf(
                Player.White,
                Player.Black,
                getRandomPlayer()
            ),
            0,
            { it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonSelected) },
            { it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonNotSelected) },
            activity.resources.getString(R.string.pref_startingPlayer),
            dialogContentLayout
        )

    }

    private fun initGameTypePrefButtons(): ListPreferenceFromButtonsGroup<GameType> {
        val difficultySpinner = dialogContentLayout.findViewById<Spinner>(R.id.newGameDialog_Difficulty)
        return ListPreferenceFromButtonsGroup(
            arrayOf(
                R.id.newGameDialog_GameType_singlePlayer,
                R.id.newGameDialog_GameType_twoPlayers
            ),
            arrayOf(
                GameType.SinglePlayer,
                GameType.Multiplayer
            ),
            0,
            {
                it.backgroundTintList =
                    ContextCompat.getColorStateList(activity, R.color.buttonSelected)
                difficultySpinner.isEnabled = it.id == R.id.newGameDialog_GameType_singlePlayer
            },
            {
                it.backgroundTintList =
                    ContextCompat.getColorStateList(activity, R.color.buttonNotSelected)
            },
            activity.resources.getString(R.string.pref_gameType),
            dialogContentLayout
        )
    }


}

fun getRandomPlayer(): Player {
    return if (Math.random() > 0.5) Player.White else Player.Black
}


class ListPreferenceFromButtonsGroup<T>(
    private val buttonsId: Array<Int>,
    private val values: Array<T>,
    private val defaultValueIndex: Int,
    private val applyToSelectedButton: (View) -> Unit,
    private val applyToUnselectedButton: (View) -> Unit,
    private val prefKey: String,
    private val dialogLayout: View
) {

    var value: T
        private set

    private val sharedPref = dialogLayout.context.getSharedPreferences(
        dialogLayout.resources.getString(R.string.prefCategory_main), Context.MODE_PRIVATE)

    private var selected: View? = null

    init {
        value = values[getValueIndexFromPref()]
        if (buttonsId.size != values.size) { throw InternalError() }
        for (buttonId in buttonsId) {
            dialogLayout.findViewById<View>(buttonId).setOnClickListener{ select(it) }
        }
        val defaultButtonId = buttonsId[getValueIndexFromPref()]
        select(dialogLayout.findViewById(defaultButtonId))
    }

    private fun select(button: View) {
        this.selected?.let { applyToUnselectedButton(it) }
        applyToSelectedButton(button)
        this.selected = button
        val index = buttonsId.indexOf(button.id)
        value = values[index]
        saveValueIndexToPref(index)
    }

    private fun getValueIndexFromPref(): Int {
        return sharedPref.getInt(prefKey, defaultValueIndex)
    }

    private fun saveValueIndexToPref(index: Int) {
        with (sharedPref.edit()) {
            putInt(prefKey, index)
            apply()
        }
    }
}

