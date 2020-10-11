/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.dialogs

import android.app.Activity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.arealapps.ultimatecheckers.R
import com.arealapps.ultimatecheckers.appRoot
import com.arealapps.ultimatecheckers.helpers.game_enums.DifficultyEnum
import com.arealapps.ultimatecheckers.helpers.game_enums.GameTypeEnum
import com.arealapps.ultimatecheckers.helpers.game_enums.StartingPlayerEnum
import com.arealapps.ultimatecheckers.helpers.android.SingleSelectionButtonGroup
import com.arealapps.ultimatecheckers.managers.themed_resources.ThemedResources

class NewGameDialog(
    private val activity: Activity,
    showBonusVirtualGame: Boolean,
    private val applyWhenConfirmed: (startingPlayer: StartingPlayerEnum, gameType: GameTypeEnum, difficulty: DifficultyEnum, userPlaysFirst: Boolean) -> Unit
) : Dialog {

    override val isShowing: Boolean
        get() = dialog.isShowing
    override fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private var dialog: android.app.Dialog

    private val dialogLayout: LinearLayout =
        activity.layoutInflater.inflate(R.layout.dialog_new_game, null) as LinearLayout
    private val startingPlayerButtonGroup: SingleSelectionButtonGroup<StartingPlayerEnum, View>
    private val gameTypeButtonGroup: SingleSelectionButtonGroup<GameTypeEnum, View>

    private var userPlaysFirstCheckbox: CheckBox
    private var difficultySpinner: Spinner


    init {

        val builder = AlertDialog.Builder(activity)
            .setTitle(activity.resources.getString(R.string.mainActivity_newGameDialog_title))
            .setView(dialogLayout)
//            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.

        difficultySpinner = dialogLayout.findViewById(R.id.newGameDialog_Difficulty)
        userPlaysFirstCheckbox = dialogLayout.findViewById(R.id.newGameDialog_userPlaysFirst_checkbox)

        initPiecesButtonsImageResources()
        initBonusVirtualGameButtonAndSelectionPref(showBonusVirtualGame)
        initUserPlaysFirstCheckbox()
        initDifficultySpinnerSelectedItem()

        startingPlayerButtonGroup = initStartingPlayerPrefButtons()
        gameTypeButtonGroup = initGameTypePrefButtons()


        //TODO check for all typos (found stating instead of starting

        builder.let {
            it.setPositiveButton(
                activity.resources.getString(R.string.mainActivity_newGameDialog_confirm)
            ) { _, _ -> pressedOk() }

            it.setNegativeButton(activity.resources.getString(R.string.general_dialog_cancel), null)

            dialog = it.show()
        }
    }
    private fun initPiecesButtonsImageResources() {
        val whitePlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_WhitePlayer)
        val blackPlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_BlackPlayer)
        val randomPlayer = dialogLayout.findViewById<ImageButton>(R.id.newGameDialog_SelectPlayerButton_Random)
        whitePlayer.setImageResource(ThemedResources.Drawables.whitePawn.getResource())
        blackPlayer.setImageResource(ThemedResources.Drawables.blackPawn.getResource())
        randomPlayer.setImageResource(ThemedResources.Drawables.mixedPawn.getResource())
    }

    private fun initBonusVirtualGameButtonAndSelectionPref(showBonusVirtualGame: Boolean) {
        if (!showBonusVirtualGame) {
            dialogLayout.findViewById<Button>(R.id.newGameDialog_GameType_virtualGame).visibility = View.GONE
            if (appRoot.gamePreferencesManager.gameType.value == GameTypeEnum.VirtualGame) {
                appRoot.gamePreferencesManager.gameType.restoreToDefault()
            }
        }
    }

    private fun initUserPlaysFirstCheckbox() {
        userPlaysFirstCheckbox = dialogLayout.findViewById<CheckBox>(R.id.newGameDialog_userPlaysFirst_checkbox)
        userPlaysFirstCheckbox.isChecked = appRoot.gamePreferencesManager.userPlaysFirst.value
        userPlaysFirstCheckbox.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            appRoot.gamePreferencesManager.userPlaysFirst.value = isChecked
        }
    }

    private fun pressedOk() {
        val startingPlayer = appRoot.gamePreferencesManager.startingPlayer.value
        val gameType = appRoot.gamePreferencesManager.gameType.value
        val difficulty = appRoot.gamePreferencesManager.difficulty.value
        val userPlaysFirst = appRoot.gamePreferencesManager.userPlaysFirst.value

        applyWhenConfirmed(startingPlayer, gameType, difficulty, userPlaysFirst)
    }

    private fun initStartingPlayerPrefButtons(): SingleSelectionButtonGroup<StartingPlayerEnum, View> {
        return SingleSelectionButtonGroup(
            appRoot.gamePreferencesManager.startingPlayer,
            setOf(
                dialogLayout.findViewById<View>(R.id.newGameDialog_SelectPlayerButton_WhitePlayer) to StartingPlayerEnum.White,
                dialogLayout.findViewById<View>(R.id.newGameDialog_SelectPlayerButton_BlackPlayer) to StartingPlayerEnum.Black,
                dialogLayout.findViewById<View>(R.id.newGameDialog_SelectPlayerButton_Random) to StartingPlayerEnum.Random
            ),
            {
                it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonSelected)
            },
            {
                it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonNotSelected)
            }
        )

    }

    private fun initDifficultySpinnerSelectedItem() {
        //the position of the items are always [0]easy [1]medium [2]hard
        difficultySpinner.setSelection(
            when (appRoot.gamePreferencesManager.difficulty.value) {
                DifficultyEnum.Easy -> 0
                DifficultyEnum.Medium -> 1
                DifficultyEnum.Hard -> 2
            }
        )

        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appRoot.gamePreferencesManager.difficulty.value =
                    when (position) {
                        0 -> DifficultyEnum.Easy
                        1 -> DifficultyEnum.Medium
                        2 -> DifficultyEnum.Hard
                        else -> error("index mismatch")
                    }
            }
        }
    }

    private fun initGameTypePrefButtons(): SingleSelectionButtonGroup<GameTypeEnum, View> {
        return SingleSelectionButtonGroup(
            appRoot.gamePreferencesManager.gameType,
            setOf(
                dialogLayout.findViewById<View>(R.id.newGameDialog_GameType_singlePlayer) to GameTypeEnum.SinglePlayer,
                dialogLayout.findViewById<View>(R.id.newGameDialog_GameType_twoPlayers) to GameTypeEnum.Multiplayer,
                dialogLayout.findViewById<View>(R.id.newGameDialog_GameType_virtualGame) to GameTypeEnum.VirtualGame
            ),
            {
                it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonSelected)
                difficultySpinner.isEnabled = (it.id == R.id.newGameDialog_GameType_singlePlayer || it.id == R.id.newGameDialog_GameType_virtualGame)
                userPlaysFirstCheckbox.isEnabled = (it.id == R.id.newGameDialog_GameType_singlePlayer)

            },
            {
                it.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.buttonNotSelected)
            })
    }


}


