package el.arn.opencheckers.widgets.main_activity.main_board.parts

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import el.arn.opencheckers.R
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.complementaries.android.activity
import el.arn.opencheckers.complementaries.android.isAlive
import el.arn.opencheckers.complementaries.game.TileCoordinate
import el.arn.opencheckers.tools.themed_resources.ChangesStyleByTheme
import el.arn.opencheckers.tools.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.opencheckers.tools.themed_resources.ThemedResources


abstract class Tile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    val tileCoordinate: TileCoordinate,
    doOnClick: (Tile) -> Unit
) : ChangesStyleByTheme {
    val layout: View = layoutInflater.inflate(R.layout.element_tile, null)


    private val image: ImageView = layout.findViewById(R.id.tile)
    protected fun setImage(@DrawableRes imageRes: Int, @DimenRes alphaRes: Int) {
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
    tileCoordinate: TileCoordinate,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, tileCoordinate, doOnClick),
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
                States.Inactive -> setImage(themedResource.getResource(), R.dimen.tileAlpha_Default)
                States.Highlighted -> setImage(R.color.tileHighlight_available, R.dimen.tileAlpha_Highlighted)
                States.Selected, States.SelectedAndCanPassTurn ->  setImage(R.color.tileHighlight_selected, R.dimen.tileAlpha_Highlighted)
                States.CanLand ->  setImage(R.color.tileHighlight_canLand, R.dimen.tileAlpha_Highlighted)
                States.CanCapture ->  setImage(R.color.tileHighlight_canCapture, R.dimen.tileAlpha_Highlighted)
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
    tileCoordinate: TileCoordinate,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, tileCoordinate, doOnClick),
    ChangesStyleByTheme by ChangesStyleByTheme_implByDelegation(
        ThemedResources.Drawables.unplayableTile
    ) {

    init {
        setImage(themedResource.getResource(), R.dimen.tileAlpha_Default)
        enableAutoRefresh(
            { setImage(it.getResource(), R.dimen.tileAlpha_Default) },
            { !layout.activity.isAlive }
        )
    }

}