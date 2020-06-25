package el.arn.opencheckers.widgets.main_activity.main_board.parts

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.android.activity
import el.arn.opencheckers.complementaries.android.isAlive
import el.arn.opencheckers.tools.themed_resources.ChangesStyleByTheme
import el.arn.opencheckers.tools.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.opencheckers.tools.themed_resources.ThemedResources

typealias GamePiece = el.arn.opencheckers.game.game_core.game_core.structs.Piece


class Piece(
    layoutInflater: LayoutInflater,
    lengthInPx: Int,
    var boardX: Int,
    var boardY: Int,
    val type: GamePiece
) : ChangesStyleByTheme by ChangesStyleByTheme_implByDelegation(
    when (type) {
        GamePiece.WhitePawn -> ThemedResources.Drawables.whitePawn
        GamePiece.WhiteKing -> ThemedResources.Drawables.whiteKing
        GamePiece.BlackPawn -> ThemedResources.Drawables.blackPawn
        GamePiece.BlackKing -> ThemedResources.Drawables.blackKing
    }
) {
    enum class States {
        Default, CanPassTurn
    }

    var state: States =
        States.Default
        set(value) {
            when (value) {
                States.Default -> image.setImageResource(R.drawable.crap_pass_turn_highlight) //TODO don't forget to replace all the "craps"
                States.CanPassTurn -> image.setImageResource(android.R.color.transparent)
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