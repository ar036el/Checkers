package el.arn.opencheckers.game

import el.arn.opencheckers.complementaries.game.TileCoordinate
import el.arn.opencheckers.game.game_core.game_core.structs.*
import el.arn.opencheckers.game.parts.Undoable
import el.arn.opencheckers.widgets.main_activity.main_board.PiecesManager
import el.arn.opencheckers.widgets.main_activity.main_board.PossibleMovesForTurnBuilder
import el.arn.opencheckers.widgets.main_activity.main_board.TilesManager
import java.lang.IllegalStateException

//todo needs more synchronization work..
class GameAsyncCoordinator(
    private val gameCore: GameCore,
    private var CheckersPiecesOnUi: PiecesManager, //todo weird name, too big. maybe piecesAnimationHandler
    private var tilesOnUi: TilesManager,
    private val undoRedoDataBridgeGateB: UndoRedoDataBridgeGateB,
    private val virtualFirstPlayer: VirtualPlayer?,
    private val virtualSecondPlayer: VirtualPlayer?,
    private val firstPlayer: Player
) {

    enum class State { ReadyForUserInput, VirtualPlayerIsCalculating, MoveAnimationIsInProgress}
    private var state: State = State.ReadyForUserInput

    fun replaceComponents(piecesManager: PiecesManager, tilesManager: TilesManager) {
        synchronized(this) {
            piecesManager.loadPieces()
            tilesManager.loadPossibleMoves()
            this.CheckersPiecesOnUi = piecesManager
            this.tilesOnUi = tilesManager
        }
    }

    private val virtualPlayerListener = object : VirtualPlayer.Listener {
        override fun finishedCalculatingFoundAMove(from: Point, to: Point) {
            synchronized(this) {
                if (state != State.VirtualPlayerIsCalculating) { throw InternalError() }
                movePiece(from, to)
            }
        }
    }

    private val tilesManagerListener = object : TilesManager.Listener {
        override fun moveWasSelected(from: TileCoordinate, to: TileCoordinate) {
            synchronized(this) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                movePiece(from, to)
            }
        }
        override fun moveWasSelectedPassedTurn() {
            synchronized(this) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                gameCore.passTurn()
                determineNextPlayer()
            }
        }
    }

    private val piecesManagerListener = object : PiecesManager.Listener {
        override fun moveAnimationFinished(from: Point, to: Point, captured: Point?) {
            synchronized(this) {
                if (state != State.MoveAnimationIsInProgress) { throw InternalError() }
                determineNextPlayer()
            }
        }
    }

    private val gameCoreListener = object : Undoable.Listener {
        override fun undoWasMade() {
            synchronized(this) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                CheckersPiecesOnUi.loadPieces()
                tilesOnUi.loadPossibleMoves()
            }
        }
        override fun redoWasMade() {
            synchronized(this) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                CheckersPiecesOnUi.loadPieces()
                tilesOnUi.loadPossibleMoves()
            }
        }
    }

    private val undoRedoMediatorListener = object : UndoRedoDataBridge.Listener {
        override fun undoWasInvoked() {
            synchronized(this) {
                if (state != State.ReadyForUserInput || !undoRedoDataBridgeGateB.isEnabled) { throw IllegalStateException() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                val successful = gameCore.undo()
                if (!successful) { throw InternalError() }
                CheckersPiecesOnUi.loadPieces()
                tilesOnUi.loadPossibleMoves()
            }
        }
        override fun redoWasInvoked() {
            synchronized(this) {
                if (state != State.ReadyForUserInput || !undoRedoDataBridgeGateB.isEnabled) { throw IllegalStateException() }
                tilesOnUi.disableSelectionAndRemoveHighlight()
                val successful = gameCore.redo()
                if (!successful) { throw InternalError() }
                CheckersPiecesOnUi.loadPieces()
                tilesOnUi.loadPossibleMoves()
            }
        }
    }


    init {

        gameCore.config.addListener {
            synchronized(this) {
                when (state) {
                    State.ReadyForUserInput -> {
                        tilesOnUi.removeListener(tilesManagerListener)
                        tilesOnUi.disableSelectionAndRemoveHighlight()
                        gameCore.refreshGame()
                        tilesOnUi.addListener(tilesManagerListener)
                        determineNextPlayer()
                    }
                    State.VirtualPlayerIsCalculating -> {
                        virtualFirstPlayer?.removeListener(virtualPlayerListener)
                        virtualSecondPlayer?.removeListener(virtualPlayerListener)
                        virtualFirstPlayer?.cancelCalculationIfRunning()
                        virtualSecondPlayer?.cancelCalculationIfRunning()
                        gameCore.refreshGame()
                        virtualFirstPlayer?.addListener(virtualPlayerListener)
                        virtualSecondPlayer?.addListener(virtualPlayerListener)
                        determineNextPlayer()

                    }
                    State.MoveAnimationIsInProgress -> {
                        CheckersPiecesOnUi.removeListener(piecesManagerListener)
                        CheckersPiecesOnUi.cancelAnimationIfRunning()
                        gameCore.refreshGame()
                        CheckersPiecesOnUi.addListener(piecesManagerListener)
                        CheckersPiecesOnUi.loadPieces()
                        determineNextPlayer()
                    }
                }
            }
        }

        gameCore.addListener(gameCoreListener)
        CheckersPiecesOnUi.addListener(piecesManagerListener)
        tilesOnUi.addListener(tilesManagerListener)
        virtualFirstPlayer?.addListener(virtualPlayerListener)
        virtualSecondPlayer?.addListener(virtualPlayerListener)

        undoRedoDataBridgeGateB.addListener(undoRedoMediatorListener)

        CheckersPiecesOnUi.loadPieces()
        determineNextPlayer()
    }


    private fun determineNextPlayer() {
        synchronized(this) {
            if (isFirstPlayerTurn() && virtualFirstPlayer != null) {
                state = State.VirtualPlayerIsCalculating
                tilesOnUi.disableSelectionAndRemoveHighlight()
                virtualFirstPlayer.startCalculation()
            } else if (isSecondPlayerTurn() && virtualSecondPlayer != null) {
                state = State.VirtualPlayerIsCalculating
                tilesOnUi.disableSelectionAndRemoveHighlight()
                virtualSecondPlayer.startCalculation()
            } else {
                state = State.ReadyForUserInput
                tilesOnUi.loadPossibleMoves()
            }
        }
    }
    private fun isFirstPlayerTurn() = (firstPlayer == gameCore.currentPlayer)
    private fun isSecondPlayerTurn() = (firstPlayer != gameCore.currentPlayer)


    private fun PiecesManager.loadPieces() = this.loadPieces(gameCore.getAllPiecesForPlayer(gameCore.currentPlayer))
    private fun TilesManager.loadPossibleMoves() = this.enableTileSelection(PossibleMovesForTurnBuilder.build(gameCore))
    private fun VirtualPlayer.startCalculation() = this.calculateNextMoveInBackground(gameCore.clone())

    private fun movePiece(from: Point, to: Point) {
        state = State.MoveAnimationIsInProgress

        val pieceBefore = gameCore.getPiece(from.x, from.y)!!
        val captures = gameCore.makeAMove(from.x, from.y, to.x, to.y)

        val pieceAfter = gameCore.getPiece(to.x, to.y)!!
        val pieceChangedDuringMove = if (pieceBefore != pieceAfter) pieceAfter else null
        CheckersPiecesOnUi.movePieceWithAnimation(from, to, Point(captures.x, captures.y), pieceChangedDuringMove)
    }

}