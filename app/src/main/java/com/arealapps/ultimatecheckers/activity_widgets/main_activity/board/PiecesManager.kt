/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.activity_widgets.main_activity.board

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import com.arealapps.timecalc.helpers.android.AnimatorListener
import com.arealapps.ultimatecheckers.helpers.listeners_engine.ListenersManager
import com.arealapps.ultimatecheckers.helpers.listeners_engine.HoldsListeners
import com.arealapps.ultimatecheckers.helpers.TwoDimenPointsArray
import com.arealapps.ultimatecheckers.helpers.points.PixelCoordinate
import com.arealapps.ultimatecheckers.helpers.points.TileCoordinates
import com.arealapps.ultimatecheckers.activity_widgets.main_activity.board.parts.GenericPieceType
import com.arealapps.ultimatecheckers.activity_widgets.main_activity.board.parts.Piece
import kotlin.math.abs

typealias PieceWithBoardCoordinates = com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile

interface PiecesManager: HoldsListeners<PiecesManager.Listener> {

    fun loadPieces(boardPieces: Set<PieceWithBoardCoordinates>, tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate>, tileLengthInPx: Float, tilesInARow: Int) /** removes old pieces if any **/
    fun movePieceWithAnimation(from: TileCoordinates, to: TileCoordinates, captures: TileCoordinates?, pieceChangedDuringMove: GenericPieceType?)
    fun cancelAnimationIfRunning()


    interface Listener {
        fun animationHasStarted() {}
        fun animationHasFinished() {}
        fun pieceWasEatenDuringAnimation() {}
        fun piecesWereLoaded() {}
    }
}

class PiecesManagerImpl(
    private val layout: FrameLayout,
    private var boardSize: Int,
    private var tileLengthInPx: Float,
    private var tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate>,
    private val listenersMgr: ListenersManager<PiecesManager.Listener> = ListenersManager()
) : PiecesManager, HoldsListeners<PiecesManager.Listener> by listenersMgr {

    companion object {
        const val ANIMATION_MIN_DURATION = 300L
        const val ANIMATION_MAX_DURATION = 550L
    }

    private var animation: ViewPropertyAnimator? = null


    override fun loadPieces(boardPieces: Set<PieceWithBoardCoordinates>, tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate>, tileLengthInPx: Float, tilesInARow: Int) /** removes old pieces if any **/ { //todo need to do it optimized- if pieces are there, but no input , don't remove it just change it if needed
        if (tilesLocationInWindow != null) {
            this.tilesLocationInWindow = tilesLocationInWindow
        }
        if (tileLengthInPx != null) {
            this.tileLengthInPx = tileLengthInPx
        }
        if (tilesInARow != null) {
            this.boardSize = tilesInARow
        }

        removeAllPiecesAndCreateNewArray()
        for (pieceData in boardPieces) {
            createPiece(pieceData.x,pieceData.y, pieceData.piece)
        }
        listenersMgr.notifyAll { it.piecesWereLoaded() }
    }

    private fun removeAllPiecesAndCreateNewArray() {
        layout.removeAllViews()
        pieces = TwoDimenPointsArray(boardSize) { null }
    }

    override fun cancelAnimationIfRunning() {
        animation?.cancel()
        removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt_Handler?.removeCallbacksAndMessages(null)
    }

    override fun movePieceWithAnimation(from: TileCoordinates, to: TileCoordinates, captures: TileCoordinates?, pieceChangedDuringMove: GenericPieceType?) {
        val piece = pieces[from]
        val destination = pieces[to]
        val capturedPiece = if (captures != null) pieces[captures] else null
        if (piece == null || destination != null || (capturedPiece == null) != (captures == null) ) { throw InternalError("move piece error: piece[$piece], destination[$destination], capturedPiece[$capturedPiece], captured[$captures]") }

        val distanceBetweenTiles = abs(to.x - from.x)
        val animationDuration =  ANIMATION_MIN_DURATION + (ANIMATION_MAX_DURATION - ANIMATION_MIN_DURATION) / ((boardSize - 1) / distanceBetweenTiles)

        animatePieceForMakingAMove(piece, from, to, captures, pieceChangedDuringMove, animationDuration)
        removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt(capturedPiece, to, animationDuration, distanceBetweenTiles)

    }

    private var removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt_Handler: Handler? = null
    private fun removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt(capturedPiece: Piece?, to: TileCoordinates, animationDuration: Long, distanceBetweenTiles: Int) {
        if (capturedPiece != null) {
            val delayDurationWhenCapturingPieceDirectlyAboveThePiece = (
                    animationDuration - animationDuration *
                            (abs(capturedPiece.boardX - to.x).toFloat() / distanceBetweenTiles)
                    ).toLong()
            capturedPiece.layout.elevation -= 1

            val handler = Handler()
            removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt_Handler = handler
            handler.postDelayed({
                removePiece(capturedPiece)
                listenersMgr.notifyAll { it.pieceWasEatenDuringAnimation() }
                removeCapturedPieceWhenCapturingPieceIsDirectlyAboveIt_Handler = null
            }, delayDurationWhenCapturingPieceDirectlyAboveThePiece)
        }
    }

    private fun animatePieceForMakingAMove(piece: Piece, from: TileCoordinates, to: TileCoordinates, captures: TileCoordinates?, pieceWasChangedDuringMoveProbablyToKing: GenericPieceType?, animationDuration: Long) {
        animation = piece.layout.animate()
            .x(tilesLocationInWindow[to.x, to.y].x.toFloat())
            .y(tilesLocationInWindow[to.x, to.y].y.toFloat())
            .setDuration(animationDuration)
            .setListener(object :
                AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    listenersMgr.notifyAll { it.animationHasStarted() }
                }
                override fun onAnimationCancel(animation: Animator?) {
                    animation?.removeAllListeners()
                }
                override fun onAnimationEnd(animation: Animator?) {

                    pieces[piece.boardX, piece.boardY] = null
                    pieces[to.x, to.y] = piece

                    if (pieceWasChangedDuringMoveProbablyToKing != null) {
                        removePiece(piece)
                        createPiece(to.x, to.y, pieceWasChangedDuringMoveProbablyToKing)
                    } else {
                        piece.boardX = to.x
                        piece.boardY = to.y
                    }

                    listenersMgr.notifyAll { it.animationHasFinished() }
                }
            })
    }

    private var pieces =
        TwoDimenPointsArray<Piece?>(boardSize) { null }

    private fun createPiece(boardX: Int, boardY: Int, type: GenericPieceType) {
        val newPiece = Piece(
            layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            tileLengthInPx.toInt(), boardX, boardY, type
        )
        attachNewPieceToMatchingTileAndAddToLayout(newPiece)
    }

    private fun attachNewPieceToMatchingTileAndAddToLayout(piece: Piece) {
        if (pieces[piece.boardX, piece.boardY] != null) throw InternalError()
        val tileLocationInWindow = tilesLocationInWindow[piece.boardX, piece.boardY]
        piece.layout.x = tileLocationInWindow.x.toFloat()
        piece.layout.y = tileLocationInWindow.y.toFloat()
        layout.addView(piece.layout)
        pieces[piece.boardX, piece.boardY] = piece
    }

    private fun removePiece(piece: Piece) {
        val point = pieces.indexOf(piece) ?: throw InternalError()
        layout.removeView(piece.layout)
        pieces[point] = null
    }

    init {
        if (tilesLocationInWindow.size != boardSize) { throw InternalError() }

        layout.removeAllViews()
    }

}