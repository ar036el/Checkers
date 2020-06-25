package el.arn.opencheckers.dialogs

import android.app.Activity
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import el.arn.opencheckers.R
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.complementaries.game.Difficulties
import el.arn.opencheckers.complementaries.game.GameTypes
import el.arn.opencheckers.complementaries.game.StartingPlayerEnum
import el.arn.opencheckers.dialogs.parts.SingleSelectionButtonGroup
import el.arn.opencheckers.tools.themed_resources.ThemedResources

class NewGameDialog(
    private val activity: Activity,
    applyWhenConfirmed: (startingPlayer: StartingPlayerEnum, gameType: GameTypes, difficulty: Difficulties) -> Unit
) {

    private val dialogLayout: ConstraintLayout =
        activity.layoutInflater.inflate(R.layout.dialog_new_game, null) as ConstraintLayout
    private val startingPlayerButtonGroup: SingleSelectionButtonGroup<StartingPlayerEnum>
    private val gameTypeButtonGroup: SingleSelectionButtonGroup<GameTypes>


    init {

        val builder = AlertDialog.Builder(activity)
            .setTitle(activity.resources.getString(R.string.newGameDialog_title))
            .setView(dialogLayout)
//            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.

        val whitePlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_WhitePlayer)
        val blackPlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_BlackPlayer)
        val randomPlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_Random)

        whitePlayer.setImageResource(ThemedResources.Drawables.whitePawn.getResource())
        blackPlayer.setImageResource(ThemedResources.Drawables.whitePawn.getResource())
        randomPlayer.setImageResource(ThemedResources.Drawables.mixedPawn.getResource())

        startingPlayerButtonGroup = initStartingPlayerPrefButtons()
        gameTypeButtonGroup = initGameTypePrefButtons()


        //TODO check for all typos (found stating instead of starting

        builder.let {
            it.setPositiveButton(
                activity.resources.getString(R.string.newGameDialog_confirm)
            ) { _, _ ->
                applyWhenConfirmed(
                    startingPlayerButtonGroup.value,
                    gameTypeButtonGroup.value,
                    Difficulties.Hard
                )
            }

            it.setNegativeButton(activity.resources.getString(R.string.dialog_cancel), null)

            it.show()
        }
    }

    private fun initStartingPlayerPrefButtons(): SingleSelectionButtonGroup<StartingPlayerEnum> {
        return SingleSelectionButtonGroup(
            arrayOf(
                R.id.newGameDialog_SelectPlayerButton_WhitePlayer,
                R.id.newGameDialog_SelectPlayerButton_BlackPlayer,
                R.id.newGameDialog_SelectPlayerButton_Random
            ),
            {
                it.backgroundTintList = ContextCompat.getColorStateList(
                    activity,
                    R.color.buttonSelected
                )
            },
            {
                it.backgroundTintList = ContextCompat.getColorStateList(
                    activity,
                    R.color.buttonNotSelected
                )
            },
            appRoot.gamePreferencesManager.startingPlayer,
            dialogLayout
        )

    }

    private fun initGameTypePrefButtons(): SingleSelectionButtonGroup<GameTypes> {
        val difficultySpinner = dialogLayout.findViewById<Spinner>(R.id.newGameDialog_Difficulty)
        return SingleSelectionButtonGroup(
            arrayOf(
                R.id.newGameDialog_GameType_singlePlayer,
                R.id.newGameDialog_GameType_twoPlayers,
                R.id.newGameDialog_GameType_virtualGame
            ),
            {
                it.backgroundTintList =
                    ContextCompat.getColorStateList(
                        activity,
                        R.color.buttonSelected
                    )
                difficultySpinner.isEnabled =
                    it.id == R.id.newGameDialog_GameType_singlePlayer
            },
            {
                it.backgroundTintList =
                    ContextCompat.getColorStateList(
                        activity,
                        R.color.buttonNotSelected
                    )
            },
            appRoot.gamePreferencesManager.gameType,
            dialogLayout
        )
    }


}


