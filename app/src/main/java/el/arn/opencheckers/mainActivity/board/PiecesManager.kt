package el.arn.opencheckers.mainActivity.board

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.widget.FrameLayout
import el.arn.opencheckers.checkers_game.game_core.structs.Point
import el.arn.opencheckers.delegationMangement.DelegatesManager
import el.arn.opencheckers.delegationMangement.HoldsDelegates
import el.arn.opencheckers.helpers.PointInPx
import el.arn.opencheckers.helpers.minus
import el.arn.opencheckers.kol_minei.AnimatorListener
import el.arn.opencheckers.kol_minei.locationInWindow
import kotlin.math.abs

typealias PieceWithBoardCoordinates = el.arn.opencheckers.checkers_game.game_core.structs.Tile

class PiecesManager(
    private val layout: FrameLayout,
    private val boardSize: Int,
    private val layoutLengthInPx: Int,
    private val tileLengthInPx: Int,
    private val tilesLocationInWindow: PointArray<PointInPx>, //TODo in window?
    private val delegationMgr: DelegatesManager<Delegate> = DelegatesManager()
) : HoldsDelegates<PiecesManager.Delegate> by delegationMgr {

    companion object {
        const val ANIMATION_MIN_DURATION = 300L
        const val ANIMATION_MAX_DURATION = 750L
    }

    fun putNewPieces(boardPoint: Set<PieceWithBoardCoordinates>) /** removes old pieces if any **/ {
        layout.removeAllViews()

        for (pieceData in boardPoint) {
            val piece = createPiece(pieceData.x,pieceData.y, pieceData.piece)
            addPiece(piece)
        }
        delegationMgr.notifyAll { it.piecesWereLoaded() }
    }

    fun movePieceWithAnimation(from: Point, to: Point, captures: Point? = null, pieceChangedDuringMove: GamePiece? = null) {
        val piece = pieces[from]
        val destination = pieces[to]
        val capturedPiece = if (captures != null) pieces[captures] else null
        if (piece == null || destination != null || (capturedPiece == null) == (captures == null) ) { throw InternalError("move piece error") }

        val distanceBetweenTiles = abs(from.x - to.x)
        val distanceInPx = layout.locationInWindow - tilesLocationInWindow[to.x, to.y]
        val animationDuration =  ANIMATION_MIN_DURATION + (ANIMATION_MAX_DURATION - ANIMATION_MIN_DURATION) / (boardSize - 1 / distanceBetweenTiles)

        //animate piece
        piece.layout.animate()
            .translationX(distanceInPx.x.toFloat())
            .translationY(distanceInPx.y.toFloat())
            .setDuration(animationDuration)
            .setListener(object : AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    pieceChangedDuringMove?.let {
                        removePiece(piece)
                        val newPiece = createPiece(from.x, from.y, it)
                        addPiece(newPiece)
                    }
                    delegationMgr.notifyAll { it.pieceWasMoved(from, to, captures) }
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

    private val pieces = PointArray<Piece?>(boardSize) {null}

    private fun createPiece(boardX: Int, boardY: Int, type: GamePiece): Piece {
        return Piece(
            layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            tileLengthInPx, boardX, boardY, type
        )
    }

    private fun addPiece(piece: Piece) {
        if (pieces[piece.boardX, piece.boardY] != null) throw InternalError()
        layout.addView(piece.layout)
        pieces[piece.boardX, piece.boardY] = piece
    }

    private fun removePiece(piece: Piece) {
        val point = pieces.indexOf(piece) ?: throw InternalError()
        layout.removeView(piece.layout)
        pieces[point] = null
    }

    interface Delegate {
        fun pieceWasMoved(from: Point, to: Point, captured: Point?) {}
        fun piecesWereLoaded() {}
    }

    init {
        layout.removeAllViews()
    }

}