package el.arn.opencheckers.game

import el.arn.opencheckers.AppRoot
import el.arn.opencheckers.android_widgets.main_activity.WinnerMessage
import el.arn.opencheckers.game.game_core.game_core.implementations.BoardImpl
import el.arn.opencheckers.game.game_core.game_core.implementations.GameImpl
import el.arn.opencheckers.game.game_core.game_core.implementations.GameLogicConfigImpl
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.android_widgets.main_activity.board.PiecesManager
import el.arn.opencheckers.android_widgets.main_activity.board.TilesManager
import el.arn.opencheckers.android_widgets.main_activity.toolbar.ToolbarAbstract

class NewGameFactory(private val appRoot: AppRoot) {

    fun startNewSinglePlayerGame(userTeam: Player, userPlaysFirst: Boolean,
                                 piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB
    ) {
        val (firstPlayer, virtualFirstPlayer, virtualSecondPlayer) = setPlayersForSingleGame(userTeam, userPlaysFirst)
        val whitePiecesStartOnBoardTop = (userTeam == Player.Black)
        val gameCore = createNewGame(firstPlayer)

        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, virtualFirstPlayer, virtualSecondPlayer, firstPlayer, whitePiecesStartOnBoardTop)
    }

    fun startNewMultiplayerGame(firstPlayerTeam: Player, piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB) {
        val gameCore = createNewGame(firstPlayerTeam)
        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, null, null, firstPlayerTeam, (firstPlayerTeam == Player.Black))
    }

    fun startNewVirtualGame(firstPlayerTeam: Player, piecesManager: PiecesManager, tilesManager: TilesManager, toolbar: ToolbarAbstract, winnerMessage: WinnerMessage, undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB) {
        val difficulty = appRoot.gamePreferencesManager.difficulty.value
        val boardSize = appRoot.gamePreferencesManager.boardSize.value

        val gameCore = createNewGame(firstPlayerTeam)
        val virtualFirstPlayer = VirtualPlayerImpl(firstPlayerTeam, difficulty, boardSize)
        val virtualSecondPlayer = VirtualPlayerImpl(firstPlayerTeam.opponent(), difficulty, boardSize)

        appRoot.gameCoordinator?.destroyGame()
        appRoot.gameCoordinator = GameCoordinatorImpl(gameCore, piecesManager, tilesManager, toolbar, winnerMessage, undoRedoDataBridgeSideB, virtualFirstPlayer, virtualSecondPlayer, firstPlayerTeam, (firstPlayerTeam == Player.Black))
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
        val logicConfig = GameLogicConfigImpl(
            appRoot.gamePreferencesManager.isCapturingMandatory.value,
            appRoot.gamePreferencesManager.kingBehaviour.value,
            appRoot.gamePreferencesManager.canPawnCaptureBackwards.value)

        val board = BoardImpl(appRoot.gamePreferencesManager.boardSize.value, appRoot.gamePreferencesManager.startingRows.value)
        val game = GameImpl(logicConfig, board, startingPlayer, null) //todo why null?
        return GameCoreImpl(game)
    }
}