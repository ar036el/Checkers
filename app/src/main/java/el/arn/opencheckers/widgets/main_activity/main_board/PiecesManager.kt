package el.arn.opencheckers.widgets.main_activity.main_board

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import el.arn.opencheckers.game.game_core.game_core.structs.Point
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.complementaries.TwoDimenPointsArray
import el.arn.opencheckers.complementaries.game.minus
import el.arn.opencheckers.complementaries.android.AnimatorListener
import el.arn.opencheckers.complementaries.android.locationInWindow
import el.arn.opencheckers.widgets.main_activity.main_board.parts.GamePiece
import el.arn.opencheckers.widgets.main_activity.main_board.parts.Piece
import kotlin.math.abs

typealias PieceWithBoardCoordinates = el.arn.opencheckers.game.game_core.game_core.structs.Tile

interface PiecesManager: HoldsListeners<PiecesManager.Listener> {

    fun loadPieces(boardPoint: Set<PieceWithBoardCoordinates>) /** removes old pieces if any **/
    fun movePieceWithAnimation(from: Point, to: Point, captures: Point?, pieceChangedDuringMove: GamePiece?)
    fun cancelAnimationIfRunning()


    interface Listener {
        fun moveAnimationFinished(from: Point, to: Point, captured: Point?) {}
        fun moveAnimationCanceled() {}
        fun piecesLoaded() {}
    }
}

class PiecesManager_impl(
    private val layout: FrameLayout,
    private val boardSize: Int,
    private val tileLengthInPx: Float,
    private val tilesManager: TilesManager,
    private val delegationMgr: ListenersManager<PiecesManager.Listener> = ListenersManager()
) : PiecesManager, HoldsListeners<PiecesManager.Listener> by delegationMgr {

    companion object {
        const val ANIMATION_MIN_DURATION = 300L
        const val ANIMATION_MAX_DURATION = 750L
    }

    private var animation: ViewPropertyAnimator? = null


    override fun loadPieces(boardPoint: Set<PieceWithBoardCoordinates>) /** removes old pieces if any **/ { //todo need to to it optimized- if pieces are there, don't remove it just change it if needed
        layout.removeAllViews()

        for (pieceData in boardPoint) {
            val piece = createPiece(pieceData.x,pieceData.y, pieceData.piece)
            addPieceToLayout(piece)
        }
        delegationMgr.notifyAll { it.piecesLoaded() }
    }

    override fun cancelAnimationIfRunning() {
        animation?.cancel()
    }

    override fun movePieceWithAnimation(from: Point, to: Point, captures: Point?, pieceChangedDuringMove: GamePiece?) {
        val piece = pieces[from]
        val destination = pieces[to]
        val capturedPiece = if (captures != null) pieces[captures] else null
        if (piece == null || destination != null || (capturedPiece == null) == (captures == null) ) { throw InternalError("move piece error") }

        val distanceBetweenTiles = abs(from.x - to.x)
        val distanceInPx = layout.locationInWindow - tilesManager.tilesStoredInLayout[to.x, to.y].layout.locationInWindow
        val animationDuration =  ANIMATION_MIN_DURATION + (ANIMATION_MAX_DURATION - ANIMATION_MIN_DURATION) / (boardSize - 1 / distanceBetweenTiles)

        //animate piece
        animation = piece.layout.animate()
            .translationX(distanceInPx.x.toFloat())
            .translationY(distanceInPx.y.toFloat())
            .setDuration(animationDuration)
            .setListener(object :
                AnimatorListener {
                override fun onAnimationCancel(animation: Animator?) {
                    animation?.removeAllListeners()
                }
                override fun onAnimationEnd(animation: Animator?) {

                    if (pieceChangedDuringMove != null) {
                        removePiece(piece)
                        val newPiece = createPiece(from.x, from.y, pieceChangedDuringMove)
                        addPieceToLayout(newPiece)
                    } else {
                        piece.boardX = to.x
                        piece.boardY = to.y
                    }
                    delegationMgr.notifyAll { it.moveAnimationFinished(from, to, captures) }
                }
            })

        //remove captured piece when capturing piece is on top
        capturedPiece?.let {
            val delayDuration = (
                    animationDuration *
                    ((animationDuration - abs(captures!!.x - to.x)).toFloat() / distanceBetweenTiles)
                    ).toLong()

            Handler().postDelayed({
                capturedPiece.layout.elevation -= 1
                removePiece(capturedPiece)
                //todo explosion here?
            }, delayDuration)
        }

    }

    private val pieces =
        TwoDimenPointsArray<Piece?>(boardSize) { null }

    private fun createPiece(boardX: Int, boardY: Int, type: GamePiece): Piece {
        return Piece(
            layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            tileLengthInPx.toInt(), boardX, boardY, type
        )
    }

    private fun addPieceToLayout(piece: Piece) {
        if (pieces[piece.boardX, piece.boardY] != null) throw InternalError()
        val tileLocationInWindow = tilesManager.tilesStoredInLayout[piece.boardX, piece.boardY].layout
        piece.layout.x = tileLocationInWindow.x
        piece.layout.y = tileLocationInWindow.y
        layout.addView(piece.layout)
        pieces[piece.boardX, piece.boardY] = piece
    }

    private fun removePiece(piece: Piece) {
        val point = pieces.indexOf(piece) ?: throw InternalError()
        layout.removeView(piece.layout)
        pieces[point] = null
    }

    init {
        layout.removeAllViews()
    }

}