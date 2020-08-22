package el.arn.checkers.game

import el.arn.checkers.AppRoot
import el.arn.checkers.android_widgets.main_activity.WinnerMessage
import el.arn.checkers.game.game_core.checkers_game.implementations.BoardImpl
import el.arn.checkers.game.game_core.checkers_game.implementations.GameImpl
import el.arn.checkers.game.game_core.checkers_game.implementations.GameLogicConfigImpl
import el.arn.checkers.game.game_core.checkers_game.structs.Player
import el.arn.checkers.android_widgets.main_activity.board.PiecesManager
import el.arn.checkers.android_widgets.main_activity.board.TilesManager
import el.arn.checkers.android_widgets.main_activity.toolbar.ToolbarAbstract
import el.arn.checkers.game.game_core.VirtualPlayer
import el.arn.checkers.game.game_core.VirtualPlayerImpl

class NewGameFactory(private val appRoot: AppRoot) {

    fun startNewSinglePlayerGame(userTeam: Player, userPlaysFirst: Boolean,
                                 piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB
    ) {
        val (firstPlayer, virtualFirstPlayer, virtualSecondPlayer) = setPlayersForSingleGame(userTeam, userPlaysFirst)
        val whitePiecesStartOnBoardTop = (userTeam == Player.Black)
        val gameCore = createNewGame(firstPlayer)

        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, virtualFirstPlayer, virtualSecondPlayer, appRoot.timer, appRoot.gamePreferencesManager, appRoot.soundEffectsManager, firstPlayer, whitePiecesStartOnBoardTop)
    }

    fun startNewMultiplayerGame(firstPlayerTeam: Player, piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB) {
        val gameCore = createNewGame(firstPlayerTeam)
        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, null, null, appRoot.timer, appRoot.gamePreferencesManager, appRoot.soundEffectsManager, firstPlayerTeam, (firstPlayerTeam == Player.Black))
    }

    fun startNewVirtualGame(firstPlayerTeam: Player, piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB) {
        val difficulty = appRoot.gamePreferencesManager.difficulty.value
        val boardSize = appRoot.gamePreferencesManager.boardSize.value

        val gameCore = createNewGame(firstPlayerTeam)
        val virtualFirstPlayer = VirtualPlayerImpl(firstPlayerTeam, difficulty, boardSize)
        val virtualSecondPlayer = VirtualPlayerImpl(firstPlayerTeam.opponent(), difficulty, boardSize)

        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, virtualFirstPlayer, virtualSecondPlayer, appRoot.timer, appRoot.gamePreferencesManager, appRoot.soundEffectsManager, firstPlayerTeam, (firstPlayerTeam == Player.Black))
    }

    private fun setPlayersForSingleGame(userTeam: Player, userPlaysFirst: Boolean): Triple<Player, VirtualPlayer?, VirtualPlayer?> {
        val opponentsDifficulty = appRoot.gamePreferencesManager.difficulty.value
        val boardSize = appRoot.gamePreferencesManager.boardSize.value

        var virtualFirstPlayer: VirtualPlayer? = null
        var virtualSecondPlayer: VirtualPlayer? = null
        var firstPlayer: Player? = null

        when {
            userTeam == Player.White && userPlaysFirst -> {
                firstPlayer = Player.White
                virtualSecondPlayer = VirtualPlayerImpl(Player.Black, opponentsDifficulty, boardSize)
            }
            userTeam == Player.White && !userPlaysFirst -> {
                firstPlayer = Player.Black
                virtualFirstPlayer = VirtualPlayerImpl(Player.Black, opponentsDifficulty, boardSize)
            }
            userTeam == Player.Black && userPlaysFirst -> {
                firstPlayer = Player.Black
                virtualSecondPlayer = VirtualPlayerImpl(Player.White, opponentsDifficulty, boardSize)
            }
            userTeam == Player.Black && !userPlaysFirst -> {
                firstPlayer = Player.White
                virtualFirstPlayer = VirtualPlayerImpl(Player.White, opponentsDifficulty, boardSize)
            }
        }

        return Triple(firstPlayer!!, virtualFirstPlayer, virtualSecondPlayer)
    }

    private fun createNewGame(startingPlayer: Player): GameCoreImpl {
        val gameLogicConfig = GameLogicConfigImpl(
            appRoot.gamePreferencesManager.isCapturingMandatory.value,
            appRoot.gamePreferencesManager.kingBehaviour.value,
            appRoot.gamePreferencesManager.canPawnCaptureBackwards.value)

        val board = BoardImpl(appRoot.gamePreferencesManager.boardSize.value, appRoot.gamePreferencesManager.startingRows.value)
        val game = GameImpl(gameLogicConfig, board, startingPlayer, null) //todo why null?
        return GameCoreImpl(game)
    }
}