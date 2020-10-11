/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.gameCore

import com.arealapps.ultimatecheckers.activityWidgets.mainActivity.WinnerMessage
import com.arealapps.ultimatecheckers.helpers.points.TileCoordinates
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.*
import com.arealapps.ultimatecheckers.activityWidgets.mainActivity.board.PiecesManager
import com.arealapps.ultimatecheckers.activityWidgets.mainActivity.board.PossibleMovesForTurnBuilder
import com.arealapps.ultimatecheckers.activityWidgets.mainActivity.board.TilesManager
import com.arealapps.ultimatecheckers.activityWidgets.mainActivity.toolbar.ToolbarAbstract
import com.arealapps.ultimatecheckers.gameCore.game_core.VirtualPlayer
import com.arealapps.ultimatecheckers.helpers.functions.LimitedAccessFunction
import com.arealapps.ultimatecheckers.helpers.game_enums.GameTypeEnum
import com.arealapps.ultimatecheckers.helpers.game_enums.WinningTypeOptions
import com.arealapps.ultimatecheckers.helpers.listeners_engine.LimitedListener
import com.arealapps.ultimatecheckers.helpers.listeners_engine.LimitedListenerImpl
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.configurations.ConfigListener
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.configurations.GameLogicConfig
import com.arealapps.ultimatecheckers.helpers.selections.OutOf2
import com.arealapps.ultimatecheckers.managers.SoundEffectsManager
import com.arealapps.ultimatecheckers.managers.Timer
import com.arealapps.ultimatecheckers.managers.preferences_managers.GamePreferencesManager
import com.arealapps.ultimatecheckers.managers.preferences_managers.Preference
import java.lang.IllegalStateException

//todo needs more synchronization work..


interface GameCoordinator {
    fun replaceActivityComponents(piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage)
    fun destroyGame()
    var isPaused: Boolean
    val isGameOn:  /**[false] means game was finished**/ Boolean
    val winningTypeIfGameWasFinished: WinningTypeOptions?
    val gameCore: GameCore
}

class GameCoordinatorImpl(
    override val gameCore: GameCore,
    private var piecesManager: PiecesManager,
    private var tilesManager: TilesManager,
    private var toolbar: ToolbarAbstract,
    private var winnerMessage: WinnerMessage,
    private val undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB,
    private val virtualFirstPlayer: VirtualPlayer?,
    private val virtualSecondPlayer: VirtualPlayer?,
    private val timer: Timer,
    private val gamePreferencesManager: GamePreferencesManager,
    private val soundEffectsManager: SoundEffectsManager,
    private val firstPlayer: Player,
    private val boardSize: Int,
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
            determineCurrentPlayerAndState()
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

    override val winningTypeIfGameWasFinished: WinningTypeOptions?
        get () {
            if (isGameOn || gameCore.winner == null) {
                return null
            }
            return if (gameType == GameTypeEnum.SinglePlayer) {
                if (usersTeamIfGameTypeIsSinglePlayer!! == gameCore.winner) {
                    WinningTypeOptions.Win
                } else {
                    WinningTypeOptions.Lose
                }
            } else {
                if (gameCore.winner == firstPlayer) {
                    WinningTypeOptions.Player1Wins
                } else {
                    WinningTypeOptions.Player2Wins
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
                gameCore.passTurn()
                determineCurrentPlayerAndState()
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
                playSoundEffectOfTurnedIntoKing.invokeIfHasAccess()
                if (!isPlayerVirtual(gameCore.currentPlayer)) {
                    gameCore.saveSnapshotAsLatest()
                }
                updateIfCanUndoOrRedo()
                determineCurrentPlayerAndState()
            }
        }

        override fun pieceWasEatenDuringAnimation() {
            soundEffectsManager.playSoundEffectIfAny(SoundEffectsManager.SoundEffectOptions.PieceCaptured)
        }
    }

    private val playSoundEffectOfTurnedIntoKing = LimitedAccessFunction({
        soundEffectsManager.playSoundEffectIfAny(SoundEffectsManager.SoundEffectOptions.TurnedIntoKing)
    })

    private val preferenceChangedListenerKingBehaviour = object : Preference.Listener<GameLogicConfig.KingBehaviourOptions> {
        override fun prefHasChanged(preference: Preference<GameLogicConfig.KingBehaviourOptions>, value: GameLogicConfig.KingBehaviourOptions) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.kingBehaviour = value
            }
        }
    }
    private val preferenceChangedListenerCanPawnCaptureBackwards = object : Preference.Listener<GameLogicConfig.CanPawnCaptureBackwardsOptions> {
        override fun prefHasChanged(preference: Preference<GameLogicConfig.CanPawnCaptureBackwardsOptions>, value: GameLogicConfig.CanPawnCaptureBackwardsOptions) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.canPawnCaptureBackwards = value
            }
        }
    }
    private val preferenceChangedListenerIsCapturingMandatory = object : Preference.Listener<Boolean> {
        override fun prefHasChanged(preference: Preference<Boolean>, value: Boolean) {
            synchronized(this@GameCoordinatorImpl) {
                gameCore.config.isCapturingMandatory = value
            }
        }
    }

    private val undoRedoDataBridgeListener = object : UndoRedoDataBridge.Listener {
        override fun undoWasInvoked() {
            synchronized(this@GameCoordinatorImpl) {
                undoOrRedo(OutOf2(0))
            }
        }
        override fun redoWasInvoked() {
            synchronized(this@GameCoordinatorImpl) {
                undoOrRedo(OutOf2(1))
            }
        }
    }

    private val gameLogicConfigListener = object : ConfigListener {
        override fun configurationHasChanged() {
            synchronized(this@GameCoordinatorImpl) {
                stopCurrentCycleAndRefreshGame()
                determineCurrentPlayerAndState()
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

    private fun undoOrRedo(isUndoOrRedo: OutOf2) {
        if (state == State.GameIsFinished) {
            state = State.ReadyForUserInput
            winnerMessage.hide()
        }
        if (state != State.ReadyForUserInput || !undoRedoDataBridgeSideB.isEnabled) { throw IllegalStateException() }
        tilesManager.disableSelectionAndRemoveHighlight()
        val successful = if (isUndoOrRedo.isFirst()) gameCore.undo() else gameCore.redo()
        if (!successful) { throw InternalError() }
        piecesManager.loadPieces()
        determineCurrentPlayerAndState()
    }

    private fun pauseGame() {
        stopCurrentCycleAndRefreshGame()
        state = State.GameIsPaused
    }
    private fun resumeGame() {
        state = State.GameIsResumed
        determineCurrentPlayerAndState()
    }

    private fun isPlayerVirtual(player: Player): Boolean {
        val virtualFirstPlayer = virtualFirstPlayer?.playerOrTeam
        val virtualSecondPlayer = virtualSecondPlayer?.playerOrTeam
        return (virtualFirstPlayer == player || virtualSecondPlayer == player)
    }
    
    private fun stopCurrentCycleAndRefreshGame() {
        timer.stop()
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
        timer.stop()
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

    private fun initGameItself() {
        if (virtualFirstPlayer == null) {
            gameCore.saveSnapshotAsLatest()
        }
        timer.reset()
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
        gamePreferencesManager.kingBehaviour.addListener(preferenceChangedListenerKingBehaviour)
        gamePreferencesManager.canPawnCaptureBackwards.addListener(preferenceChangedListenerCanPawnCaptureBackwards)
        gamePreferencesManager.isCapturingMandatory.addListener(preferenceChangedListenerIsCapturingMandatory)
        piecesManager.addListener(piecesManagerListener)
        tilesManager.addListener(tilesManagerListener)
        virtualFirstPlayer?.addListener(virtualPlayerListener)
        virtualSecondPlayer?.addListener(virtualPlayerListener)
        undoRedoDataBridgeSideB.addListener(undoRedoDataBridgeListener)
    }

    private fun removeAllListeners() {
        gamePreferencesManager.kingBehaviour.removeListener(preferenceChangedListenerKingBehaviour)
        gamePreferencesManager.canPawnCaptureBackwards.removeListener(preferenceChangedListenerCanPawnCaptureBackwards)
        gamePreferencesManager.isCapturingMandatory.removeListener(preferenceChangedListenerIsCapturingMandatory)
        piecesManager.removeListener(piecesManagerListener)
        tilesManager.removeListener(tilesManagerListener)
        virtualFirstPlayer?.removeListener(virtualPlayerListener)
        virtualSecondPlayer?.removeListener(virtualPlayerListener)
        undoRedoDataBridgeSideB.removeListener(undoRedoDataBridgeListener)
    }

    private val usersTeamIfGameTypeIsSinglePlayer: Player? get() {
        if (gameType != GameTypeEnum.SinglePlayer) {
            return null
        }
        if (virtualFirstPlayer != null) {
            return virtualFirstPlayer.playerOrTeam.opponent()
        } else {
            return virtualSecondPlayer!!.playerOrTeam.opponent()
        }
    }

    private fun updateToolbarText() {
        if (gameType == GameTypeEnum.SinglePlayer) {
            if (state == State.GameIsFinished) {
                if (gameCore.winner!! == usersTeamIfGameTypeIsSinglePlayer!!) {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.USER_WON
                } else {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.USER_LOST
                }
            } else {
                if (gameCore.currentPlayer == usersTeamIfGameTypeIsSinglePlayer!!) {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.USER_TURN
                } else {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.PC_TURN
                }
            }
        } else {
            if (state == State.GameIsFinished) {
                if (gameCore.winner!! == firstPlayer) {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.FIRST_PLAYER_WON
                } else {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.SECOND_PLAYER_WON
                }
            } else {
                if (gameCore.currentPlayer == firstPlayer) {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.FIRST_PLAYER_TURN
                } else {
                    toolbar.titleText = ToolbarAbstract.TitleTextOptions.SECOND_PLAYER_TURN
                }
            }
        }
    }
    
    private fun determineCurrentPlayerAndState() {
        if (state == State.GameIsPaused || state == State.GameIsFinished) {
            return
        }

        tilesManager.disableSelectionAndRemoveHighlight()

        if (gameCore.winner != null) {
            state = State.GameIsFinished
            showWinnerMessage()
            timer.stop()
            toolbar.titleText = if (gameCore.winner == firstPlayer) ToolbarAbstract.TitleTextOptions.FIRST_PLAYER_WON else ToolbarAbstract.TitleTextOptions.SECOND_PLAYER_WON
            //that's it. game it dead
        } else if (isFirstPlayerTurn() && virtualFirstPlayer != null) {
            state = State.VirtualPlayerIsCalculating
            virtualFirstPlayer.startCalculation()
            timer.stop()
            toolbar.titleText = ToolbarAbstract.TitleTextOptions.FIRST_PLAYER_TURN
        } else if (isSecondPlayerTurn() && virtualSecondPlayer != null) {
            state = State.VirtualPlayerIsCalculating
            virtualSecondPlayer.startCalculation()
            timer.stop()
            toolbar.titleText = ToolbarAbstract.TitleTextOptions.SECOND_PLAYER_TURN
        } else {
            state = State.ReadyForUserInput
            tilesManager.loadAndHighlightPossibleMoves()
            timer.start()
        }

        updateToolbarText()
    }
    private fun isFirstPlayerTurn() = (firstPlayer == gameCore.currentPlayer)
    private fun isSecondPlayerTurn() = (firstPlayer != gameCore.currentPlayer)

    private fun TilesManager.buildLayout() = this.buildLayout(boardSize, whitePiecesStartOnBoardTop)
    private fun PiecesManager.loadPieces() = this.loadPieces(gameCore.allPiecesInBoard, tilesManager.tilesLocationInWindow, tilesManager.tileLengthInPx, boardSize)
    private fun TilesManager.loadAndHighlightPossibleMoves() = this.enableTileSelection(PossibleMovesForTurnBuilder.build(gameCore))
    private fun VirtualPlayer.startCalculation() = this.calculateNextMoveAsync(gameCore.clone())

    private fun makeAMove(from: TileCoordinates, to: TileCoordinates) {
        state = State.MoveAnimationIsInProgress

        val pieceBefore: Piece = gameCore.getPiece(from.x, from.y)!!
        var wasFirstPlayerTurn = isFirstPlayerTurn()
        val captures: TileCoordinates? = gameCore.makeAMove(from.x, from.y, to.x, to.y)?.let { tile: Tile -> TileCoordinates(tile.x, tile.y) }

        val pieceAfter: Piece = gameCore.getPiece(to.x, to.y)!!
        val pieceChangedDuringMove = if (pieceBefore != pieceAfter) pieceAfter else null
        piecesManager.movePieceWithAnimation(from, to, captures, pieceChangedDuringMove)

        if (pieceChangedDuringMove?.type == Piece.Type.King) {
            playSoundEffectOfTurnedIntoKing.grantOneAccess()
        }

        soundEffectsManager.playSoundEffectIfAny(
            if (wasFirstPlayerTurn) {
                SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer1
            } else {
                SoundEffectsManager.SoundEffectOptions.PieceMovedPlayer2
            }
        )
    }

    init {
        addAllListeners()
        initGameItself()
        initActivityComponents()
        determineCurrentPlayerAndState()
    }

}