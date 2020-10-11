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
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.appRoot
import el.arn.ultimatecheckers.helpers.android.activity
import el.arn.ultimatecheckers.helpers.android.getFloatCompat
import el.arn.ultimatecheckers.helpers.android.isAlive
import el.arn.ultimatecheckers.helpers.points.TileCoordinates
import el.arn.ultimatecheckers.managers.themed_resources.ChangesStyleByTheme
import el.arn.ultimatecheckers.managers.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.ultimatecheckers.managers.themed_resources.ThemedResources


abstract class Tile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    val tileCoordinates: TileCoordinates,
    doOnClick: (Tile) -> Unit
) : ChangesStyleByTheme {
    val layout: View = layoutInflater.inflate(R.layout.element_tile, null)


    private val image: ImageView = layout.findViewById(R.id.tile)
    protected fun setTileImage(@DrawableRes imageRes: Int, @DimenRes alphaRes: Int) {
        image.setImageResource(imageRes)
        image.alpha = appRoot.resources.getFloatCompat(alphaRes)
    }

    init {
        layout.layoutParams = FrameLayout.LayoutParams(lengthInPx, lengthInPx)
        layout.setOnClickListener { doOnClick(this) }
    }
}

class PlayableTile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    tileCoordinates: TileCoordinates,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, tileCoordinates, doOnClick),
    ChangesStyleByTheme by ChangesStyleByTheme_implByDelegation(
        ThemedResources.Drawables.playableTile
    ) {
    enum class States {
        Inactive,
        Highlighted,
        Selected,
        SelectedAndCanPassTurn,
        CanLand,
        CanCapture
    }

    var state =
        States.Inactive
        set(value) {
            when (value) {
                States.Inactive -> setTileImage(
                    themedResource.getResource(),
                    R.dimen.tileAlpha_Default
                )
                States.Highlighted -> setTileImage(
                    R.color.tileHighlight_available,
                    R.dimen.tileAlpha_Highlighted
                )
                States.Selected, States.SelectedAndCanPassTurn -> setTileImage(
                    R.color.tileHighlight_selected,
                    R.dimen.tileAlpha_Highlighted
                )
                States.CanLand -> setTileImage(
                    R.color.tileHighlight_canLand,
                    R.dimen.tileAlpha_Highlighted
                )
                States.CanCapture -> setTileImage(
                    R.color.tileHighlight_canCapture,
                    R.dimen.tileAlpha_Highlighted
                )
            }
            field = value
        }

    init {
        state = States.Inactive
        enableAutoRefresh(
            { state = state },
            { !layout.activity.isAlive }
        )
    }

}

class UnplayableTile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    tileCoordinates: TileCoordinates,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, tileCoordinates, doOnClick),
    ChangesStyleByTheme by ChangesStyleByTheme_implByDelegation(
        ThemedResources.Drawables.unplayableTile
    ) {

    init {
        setTileImage(themedResource.getResource(), R.dimen.tileAlpha_Default)
        enableAutoRefresh(
            { setTileImage(it.getResource(), R.dimen.tileAlpha_Default) },
            { !layout.activity.isAlive }
        )
    }

}