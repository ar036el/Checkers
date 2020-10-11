/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.activityWidgets.mainActivity.board.parts

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.helpers.android.activity
import el.arn.ultimatecheckers.helpers.android.isAlive
import el.arn.ultimatecheckers.managers.themed_resources.ChangesStyleByTheme
import el.arn.ultimatecheckers.managers.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.ultimatecheckers.managers.themed_resources.ThemedResources
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Piece

typealias GenericPieceType = Piece


class Piece(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    var boardX: Int,
    var boardY: Int,
    val type: GenericPieceType
) : ChangesStyleByTheme by ChangesStyleByTheme_implByDelegation(
    when (type) {
        GenericPieceType.WhitePawn -> ThemedResources.Drawables.whitePawn
        GenericPieceType.WhiteKing -> ThemedResources.Drawables.whiteKing
        GenericPieceType.BlackPawn -> ThemedResources.Drawables.blackPawn
        GenericPieceType.BlackKing -> ThemedResources.Drawables.blackKing
    }
) {
    enum class States {
        Default, CanPassTurn
    }

    var state: States =
        States.Default
        set(value) {
            when (value) {
                States.Default -> image.setImageResource(android.R.color.transparent)
                States.CanPassTurn -> image.setImageResource(R.drawable.pass_turn_highlight)
            }
            field = value
        }

    val layout: View = layoutInflater.inflate(R.layout.element_piece, null)

    private val image: ImageView = layout.findViewById(R.id.pieceImage)

    private fun setImage(@DrawableRes imageRes: Int) {
        image.setBackgroundResource(imageRes)
    }

    init {
        layout.layoutParams = FrameLayout.LayoutParams(lengthInPx, lengthInPx)
        setImage(themedResource.getResource())
        state =
            States.Default
        enableAutoRefresh(
            { setImage(it.getResource()) },
            { !layout.activity.isAlive }
        )
    }


}