package el.arn.checkers.activity_widgets.main_activity.board.parts

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.android.activity
import el.arn.checkers.helpers.android.isAlive
import el.arn.checkers.helpers.points.TileCoordinates
import el.arn.checkers.managers.themed_resources.ChangesStyleByTheme
import el.arn.checkers.managers.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.checkers.managers.themed_resources.ThemedResources


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
        image.alpha = appRoot.resources.getFloat(alphaRes)
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
                States.Inactive -> setTileImage(themedResource.getResource(), R.dimen.tileAlpha_Default)
                States.Highlighted -> setTileImage(R.color.tileHighlight_available, R.dimen.tileAlpha_Highlighted)
                States.Selected, States.SelectedAndCanPassTurn ->  setTileImage(R.color.tileHighlight_selected, R.dimen.tileAlpha_Highlighted)
                States.CanLand ->  setTileImage(R.color.tileHighlight_canLand, R.dimen.tileAlpha_Highlighted)
                States.CanCapture ->  setTileImage(R.color.tileHighlight_canCapture, R.dimen.tileAlpha_Highlighted)
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