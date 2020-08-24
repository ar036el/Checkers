package el.arn.checkers.activity_widgets.main_activity.board.parts

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.checkers.R
import el.arn.checkers.helpers.android.activity
import el.arn.checkers.helpers.android.isAlive
import el.arn.checkers.managers.themed_resources.ChangesStyleByTheme
import el.arn.checkers.managers.themed_resources.ChangesStyleByTheme_implByDelegation
import el.arn.checkers.managers.themed_resources.ThemedResources

typealias GenericPieceType = el.arn.checkers.game.game_core.checkers_game.structs.Piece


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