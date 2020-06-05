package el.arn.opencheckers.mainActivity.board

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import el.arn.opencheckers.R
import el.arn.opencheckers.activity
import el.arn.opencheckers.isAlive
import el.arn.opencheckers.mainActivity.StyledByTheme
import el.arn.opencheckers.mainActivity.StyledByThemeImpl
import el.arn.opencheckers.mainActivity.ThemedResources
import el.arn.opencheckers.resources

abstract class Tile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    val boardX: Int,
    val boardY: Int,
    doOnClick: (Tile) -> Unit
) : StyledByTheme {
    val layout: View = layoutInflater.inflate(R.layout.element_tile, null)

    private val image: ImageView = layout.findViewById(R.id.tile)
    protected fun setImage(@DrawableRes imageRes: Int, @DimenRes alphaRes: Int) {
        image.setImageResource(imageRes)
        image.alpha = resources.getFloat(alphaRes)
    }

    init {
        layout.layoutParams = FrameLayout.LayoutParams(lengthInPx, lengthInPx)
        layout.setOnClickListener { doOnClick(this) }
    }
}

class PlayableTile(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    boardX: Int,
    boardY: Int,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, boardX, boardY, doOnClick),
    StyledByTheme by StyledByThemeImpl(
        ThemedResources.Drawables.playableTile
    ) {
    enum class States {
        Inactive,
        CanJumpFrom,
        Selected,
        SelectedAndCanPassTurn,
        CanLand,
        CanCapture
    }

    var state = States.Inactive
        set(value) {
            when (value) {
                States.Inactive -> setImage(themedResource.getResource(), R.dimen.tileAlpha_Default)
                States.CanJumpFrom -> setImage(R.color.tileHighlight_available, R.dimen.tileAlpha_Highlighted)
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
    boardX: Int,
    boardY: Int,
    doOnClick: (Tile) -> Unit
) : Tile(layoutInflater, lengthInPx, boardX, boardY, doOnClick),
    StyledByTheme by StyledByThemeImpl(
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