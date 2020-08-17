package el.arn.opencheckers.game

import el.arn.opencheckers.android_widgets.main_activity.WinnerMessage
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.complementaries.game.TileCoordinates
import el.arn.opencheckers.game.game_core.game_core.structs.*
import el.arn.opencheckers.android_widgets.main_activity.board.PiecesManager
import el.arn.opencheckers.android_widgets.main_activity.board.PossibleMovesForTurnBuilder
import el.arn.opencheckers.android_widgets.main_activity.board.TilesManager
import el.arn.opencheckers.android_widgets.main_activity.toolbar.ToolbarAbstract
import el.arn.opencheckers.complementaries.game.GameTypeEnum
import el.arn.opencheckers.complementaries.game.WinningTypes
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.complementaries.listener_mechanism.LimitedListener
import el.arn.opencheckers.complementaries.listener_mechanism.LimitedListenerImpl
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.game.game_core.game_core.configurations.ConfigListener
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig
import el.arn.opencheckers.tools.preferences_managers.Pref
import java.lang.IllegalStateException

//todo needs more synchronization work..


interface GameCoordinator {
    fun replaceActivityComponents(piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage)
    fun destroyGame()
    var isPaused: Boolean
    val isGameOn:  /**[false] means game was finished**/ Boolean
    val winningTypeIfGameWasFinished: WinningTypes?
}

class GameCoordinatorImpl(
    private val gameCore: GameCore,
    private var piecesManager: PiecesManager, //todo weird name, too big. maybe piecesAnimationHandler
    private var tilesManager: TilesManager,
    private var toolbar: ToolbarAbstract,
    private var winnerMessage: WinnerMessage,
    private val undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB,
    private val virtualFirstPlayer: VirtualPlayer?,
    private val virtualSecondPlayer: VirtualPlayer?,
    private val firstPlayer: Player,
    private val whitePiecesStartOnBoardTop: Boolean
) : GameCoordinator {

    private enum class State { GameIsInitialized, ReadyForUserInput, VirtualPlayerIsCalculating, MoveAnimationIsInProgress, GameIsPaused, GameIsResumed, GameIsFinished }
    private var state: State = State.GameIsInitialized

    override fun replaceActivityComponents(piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage) {
        synchronized(this@GameCoordinatorImpl) {
            stopCurrentCycleAndRefreshGame()
            this.piecesManager.removeListener(piecesManagerListener)
            this.tilesManager.removeListener(tilesManagerListener)
            this.piecesManager = piecesManager
            this.tilesManager = tilesManager
            this.toolbar = toolbar
            this.winnerMessage = winnerMessage
            this.piecesManager.addListener(piecesManagerListener)
            this.tilesManager.addListener(tilesManagerListener)

            initActivityComponents()
            determineCurrentPlayer()
        }
    }

    override fun destroyGame() {
        synchronized(this@GameCoordinatorImpl) {
            removeAllListeners()
            killCycle()
        }
    }
    
    override var isPaused: Boolean = false
        set(value) {
            synchronized(this@GameCoordinatorImpl) {
                if (field != value) {
                    field = value
                    if (isGameOn) {
                        if (value) pauseGame() else resumeGame()
                    }
                }
            }
        }

    override val isGameOn: Boolean
        get() = state != State.GameIsFinished

    override val winningTypeIfGameWasFinished: WinningTypes?
        get () {
            if (isGameOn || gameCore.winner == null) {
                return null
            }
            return if (gameType == GameTypeEnum.SinglePlayer) {
                if ((virtualFirstPlayer == null && gameCore.winner == firstPlayer)
                    || (virtualSecondPlayer == null && gameCore.winner == firstPlayer.opponent())) {
                    WinningTypes.Win
                } else {
                    WinningTypes.Lose
                }
            } else {
                if (gameCore.winner == firstPlayer) {
                    WinningTypes.Player1Wins
                } else {
                    WinningTypes.Player2Wins
                }
            }
        }

    private val virtualPlayerListener = object : VirtualPlayer.Listener, LimitedListener by LimitedListenerImpl() {
        override fun startedCalculating() {
            synchronized(this@GameCoordinatorImpl) {
                toolbar.progressBarVisible = true
            }
        }
        override fun finishedCalculatingFoundAMove(from: TileCoordinates, to: TileCoordinates) {
            synchronized(this@GameCoordinatorImpl) {
                if (state != State.VirtualPlayerIsCalculating) { throw InternalError() }
                toolbar.progressBarVisible = false
                makeAMove(from, to)
            }
        }
        override fun calculationWasCanceled() {
            synchronized(this@GameCoordinatorImpl) {
                toolbar.progressBarVisible = false
            }
        }
    }

    private val tilesManagerListener = object : TilesManager.Listener {
        override fun moveWasSelectedByUser(from: TileCoordinates, to: TileCoordinates) {
            synchronized(this@GameCoordinatorImpl) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesManager.disableSelectionAndRemoveHighlight()
                makeAMove(from, to)
            }
        }
        override fun moveWasSelectedByUserPassedTurn() {
            synchronized(this@GameCoordinatorImpl) {
                if (state != State.ReadyForUserInput) { throw InternalError() }
                tilesManager.disableSelectionAndRemoveHighlight()
                gameCore.passTurn() //todo that's it?
                determineCurrentPlayer()
            }
        }
    }

    private val piecesManagerListener = object : PiecesManager.Listener {
        override fun piecesWereLoaded() {
            synchronized(this@GameCoordinatorImpl) {
                updateIfCanUndoOrRedo()
            }
        }
        override fun animationHasFinished() {
            synchronized(this@GameCoordinatorImpl) {
                if (!isPlayerVirtual(gameCore.currentPlayer)) {
                    gameCore.saveSnapshotAsLatest()
                }
                updateIfCanUndoOrRedo()
                determineCurrentPlayer()
            }
        }
    }

    private val preferenceChangedListenerKingBehaviour = object : Pref.Listener<GameLogicConfig.KingBehaviourOptions> {
        override fun prefHasChanged(pref: Pref<GameLogicConfig.KingBehaviourOptions>, value: GameLogicConfig.KingBehaviourOptions) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.kingBehaviour = value
            }
        }
    }
    private val preferenceChangedListenerCanPawnCaptureBackwards = object : Pref.Listener<GameLogicConfig.CanPawnCaptureBackwardsOptions> {
        override fun prefHasChanged(pref: Pref<GameLogicConfig.CanPawnCaptureBackwardsOptions>, value: GameLogicConfig.CanPawnCaptureBackwardsOptions) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.canPawnCaptureBackwards = value
            }
        }
    }
    private val preferenceChangedListenerIsCapturingMandatory = object : Pref.Listener<Boolean> {
        override fun prefHasChanged(pref: Pref<Boolean>, value: Boolean) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.isCapturingMandatory = value
            }
        }
    }

    private val undoRedoDataBridgeListener = object : UndoRedoDataBridge.Listener {
        override fun undoWasInvoked() {
            synchronized(this@GameCoordinatorImpl) {
                if (state != State.ReadyForUserInput || !undoRedoDataBridgeSideB.isEnabled) { throw IllegalStateException() }
                tilesManager.disableSelectionAndRemoveHighlight()
                val successful = gameCore.undo()
                if (!successful) { throw InternalError() }
                piecesManager.loadPieces()
                tilesManager.loadAndHighlightPossibleMoves()
            }
        }
        override fun redoWasInvoked() {
            synchronized(this@GameCoordinatorImpl) {
                if (state != State.ReadyForUserInput || !undoRedoDataBridgeSideB.isEnabled) { throw IllegalStateException() }
                tilesManager.disableSelectionAndRemoveHighlight()
                val successful = gameCore.redo()
                if (!successful) { throw InternalError() }
                piecesManager.loadPieces()
                tilesManager.loadAndHighlightPossibleMoves()
            }
        }
    }

    private val gameLogicConfigListener = object : ConfigListener {
        override fun configurationHasChanged() {
            synchronized(this@GameCoordinatorImpl) {
                stopCurrentCycleAndRefreshGame()
                determineCurrentPlayer()
            }
        }
    }

    private val gameType: GameTypeEnum
        get() {
            val isVirtualPlayer1Exist = (virtualFirstPlayer != null)
            val isVirtualPlayer2Exist = (virtualSecondPlayer != null)
            return when {
                isVirtualPlayer1Exist && isVirtualPlayer2Exist -> GameTypeEnum.VirtualGame
                isVirtualPlayer1Exist || isVirtualPlayer2Exist -> GameTypeEnum.SinglePlayer
                else -> GameTypeEnum.Multiplayer
            }
        }

    private fun pauseGame() {
        stopCurrentCycleAndRefreshGame()
        state = State.GameIsPaused
    }
    private fun resumeGame() {
        state = State.GameIsResumed
        determineCurrentPlayer()
    }

    private fun isPlayerVirtual(player: Player): Boolean {
        val virtualFirstPlayer = virtualFirstPlayer?.playerOrTeam
        val virtualSecondPlayer = virtualSecondPlayer?.playerOrTeam
        return (virtualFirstPlayer == player || virtualSecondPlayer == player)
    }
    
    private fun stopCurrentCycleAndRefreshGame() {
        when (state) {
            State.ReadyForUserInput -> {
                tilesManager.removeListener(tilesManagerListener)
                tilesManager.disableSelectionAndRemoveHighlight()
                gameCore.refreshGame()
                tilesManager.addListener(tilesManagerListener)
            }
            State.VirtualPlayerIsCalculating -> {
                virtualFirstPlayer?.removeListener(virtualPlayerListener)
                virtualSecondPlayer?.removeListener(virtualPlayerListener)
                virtualFirstPlayer?.cancelCalculationIfRunning()
                virtualSecondPlayer?.cancelCalculationIfRunning()
                gameCore.refreshGame()
                virtualFirstPlayer?.addListener(virtualPlayerListener)
                virtualSecondPlayer?.addListener(virtualPlayerListener)
                toolbar.progressBarVisible = false
            }
            State.MoveAnimationIsInProgress -> {
                piecesManager.removeListener(piecesManagerListener)
                piecesManager.cancelAnimationIfRunning()
                gameCore.refreshGame()
                piecesManager.addListener(piecesManagerListener)
                piecesManager.loadPieces()
            }
            State.GameIsPaused, State.GameIsResumed, State.GameIsInitialized -> {
                gameCore.refreshGame()
            }
        }
    }

    private fun killCycle() {
        when (state) {
            State.ReadyForUserInput -> {
                tilesManager.disableSelectionAndRemoveHighlight()
            }
            State.VirtualPlayerIsCalculating -> {
                virtualFirstPlayer?.cancelCalculationIfRunning()
                virtualSecondPlayer?.cancelCalculationIfRunning()
            }
            State.MoveAnimationIsInProgress -> {
                piecesManager.cancelAnimationIfRunning()
            }
        }
    }


    private fun updateIfCanUndoOrRedo() {
        if (undoRedoDataBridgeSideB.canUndo != gameCore.canUndo) {
            undoRedoDataBridgeSideB.canUndo = gameCore.canUndo
        }
        if (undoRedoDataBridgeSideB.canRedo != gameCore.canRedo) {
            undoRedoDataBridgeSideB.canRedo = gameCore.canRedo
        }
    }

    private fun initGameCore() {
        if (virtualFirstPlayer == null) {
            gameCore.saveSnapshotAsLatest()
        }
    }

    private fun initActivityComponents() {
        undoRedoDataBridgeSideB.reloadState()
        toolbar.progressBarVisible = false
        winnerMessage.hide()
        tilesManager.buildLayout()
        piecesManager.loadPieces()
    }

    private fun showWinnerMessage() {
        winnerMessage.show(true, winningTypeIfGameWasFinished!!)
    }

    private fun addAllListeners() {
        gameCore.config.addListener(gameLogicConfigListener)
        val a = preferenceChangedListenerKingBehaviour
        appRoot.
        gamePreferencesManager.
        kingBehaviour.addListener(
            preferenceChangedListenerKingBehaviour)
        appRoot.gamePreferencesManager.canPawnCaptureBackwards.addListener(preferenceChangedListenerCanPawnCaptureBackwards)
        appRoot.gamePreferencesManager.isCapturingMandatory.addListener(preferenceChangedListenerIsCapturingMandatory)
        piecesManager.addListener(piecesManagerListener)
        tilesManager.addListener(tilesManagerListener)
        virtualFirstPlayer?.addListener(virtualPlayerListener)
        virtualSecondPlayer?.addListener(virtualPlayerListener)
        undoRedoDataBridgeSideB.addListener(undoRedoDataBridgeListener)
    }

    private fun removeAllListeners() {
        appRoot.gamePreferencesManager.kingBehaviour.removeListener(preferenceChangedListenerKingBehaviour)
        appRoot.gamePreferencesManager.canPawnCaptureBackwards.removeListener(preferenceChangedListenerCanPawnCaptureBackwards)
        appRoot.gamePreferencesManager.isCapturingMandatory.removeListener(preferenceChangedListenerIsCapturingMandatory)
        piecesManager.removeListener(piecesManagerListener)
        tilesManager.removeListener(tilesManagerListener)
        virtualFirstPlayer?.removeListener(virtualPlayerListener)
        virtualSecondPlayer?.removeListener(virtualPlayerListener)
        undoRedoDataBridgeSideB.removeListener(undoRedoDataBridgeListener)
    }
    
    private fun determineCurrentPlayer() {
        if (state == State.GameIsPaused || state == State.GameIsFinished) {
            return
        }

        tilesManager.disableSelectionAndRemoveHighlight()

        if (gameCore.winner != null) {
            state = State.GameIsFinished
            showWinnerMessage()
            //that's it. game it dead
        } else if (isFirstPlayerTurn() && virtualFirstPlayer != null) {
            state = State.VirtualPlayerIsCalculating
            virtualFirstPlayer.startCalculation()
            println("gago virtual player 1 turn") //todo remove
        } else if (isSecondPlayerTurn() && virtualSecondPlayer != null) {
            state = State.VirtualPlayerIsCalculating
            virtualSecondPlayer.startCalculation()
            println("gago virtual player 2 turn")
        } else {
            state = State.ReadyForUserInput
            tilesManager.loadAndHighlightPossibleMoves()
            println("gago user turn")
        }
    }
    private fun isFirstPlayerTurn() = (firstPlayer == gameCore.currentPlayer)
    private fun isSecondPlayerTurn() = (firstPlayer != gameCore.currentPlayer)

    private fun TilesManager.buildLayout() = this.buildLayout(appRoot.gamePreferencesManager.boardSize.value, whitePiecesStartOnBoardTop)
    private fun PiecesManager.loadPieces() = this.loadPieces(gameCore.allPiecesInBoard, tilesManager.tilesLocationInWindow, tilesManager.tileLengthInPx, appRoot.gamePreferencesManager.boardSize.value)
    private fun TilesManager.loadAndHighlightPossibleMoves() = this.enableTileSelection(PossibleMovesForTurnBuilder.build(gameCore))
    private fun VirtualPlayer.startCalculation() = this.calculateNextMoveAsync(gameCore.clone())

    private fun makeAMove(from: TileCoordinates, to: TileCoordinates) {
        state = State.MoveAnimationIsInProgress

        val pieceBefore: Piece = gameCore.getPiece(from.x, from.y)!!
        val captures: TileCoordinates? = gameCore.makeAMove(from.x, from.y, to.x, to.y)?.let { tile: Tile -> TileCoordinates(tile.x, tile.y) }

        val pieceAfter: Piece = gameCore.getPiece(to.x, to.y)!!
        val pieceChangedDuringMove = if (pieceBefore != pieceAfter) pieceAfter else null
        piecesManager.movePieceWithAnimation(from, to, captures, pieceChangedDuringMove)
    }

    init {
        addAllListeners()
        initGameCore()
        initActivityComponents()
        determineCurrentPlayer()
    }

}