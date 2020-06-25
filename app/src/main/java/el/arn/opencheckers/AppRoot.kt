package el.arn.opencheckers

import android.content.Context
import androidx.annotation.StringRes
import el.arn.opencheckers.complementaries.game.Difficulties
import el.arn.opencheckers.game.game_core.game_core.implementations.BoardImpl
import el.arn.opencheckers.game.game_core.game_core.implementations.GameImpl
import el.arn.opencheckers.game.game_core.game_core.implementations.GameLogicConfigImpl
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.tools.feedback_manager.CustomReportSenderFactory
import el.arn.opencheckers.tools.feedback_manager.FeedbackManager
import el.arn.opencheckers.game.*
import el.arn.opencheckers.tools.ToastManager
import el.arn.opencheckers.widgets.main_activity.main_board.PiecesManager
import el.arn.opencheckers.widgets.main_activity.main_board.TilesManager
import el.arn.opencheckers.tools.preferences_managers.GamePreferencesManager
import el.arn.opencheckers.tools.purchase_manager.PurchasesManager
import org.acra.annotation.AcraCore

lateinit var appRoot: AppRoot

@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
)
class AppRoot : android.app.Application() {

    lateinit var userFeedbackManager: FeedbackManager
    lateinit var purchasingManager: PurchasesManager
    lateinit var toastMessageManager: ToastManager
    lateinit var gamePreferencesManager: GamePreferencesManager
    val undoRedoDataBridge: UndoRedoDataBridge = UndoRedoDataBridgeImpl()

    fun getStringRes(@StringRes stringRes: Int) = resources.getString(stringRes)


    var gameAsyncCoordinator: GameAsyncCoordinator? = null

    override fun onCreate() {
        super.onCreate()

        appRoot = this

        userFeedbackManager = FeedbackManager()
        purchasingManager = PurchasesManager(this)
        toastMessageManager = ToastManager(this)
        gamePreferencesManager = GamePreferencesManager()

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        ACRA.init(this)
    }

    private val NewGameFactory = object {
        fun startNewSinglePlayerGame(usersTeam: Player, userPlaysFirst: Boolean, opponentsDifficulty: Difficulties,
                                     boardSize: Int, piecesManager: PiecesManager, tilesManager: TilesManager, undoRedoDataBridgeGateB: UndoRedoDataBridgeGateB) {

            var virtualFirstPlayer: el.arn.opencheckers.game.VirtualPlayer? = null
            var virtualSecondPlayer: el.arn.opencheckers.game.VirtualPlayer? = null
            var firstPlayer: Player? = null

            when {
                usersTeam == Player.White && userPlaysFirst -> {
                    firstPlayer = Player.White
                    virtualSecondPlayer = el.arn.opencheckers.game.VirtualPlayer(Player.Black, opponentsDifficulty, boardSize)
                }
                usersTeam == Player.White && !userPlaysFirst -> {
                    firstPlayer = Player.Black
                    virtualFirstPlayer = el.arn.opencheckers.game.VirtualPlayer(Player.Black, opponentsDifficulty, boardSize)
                }
                usersTeam == Player.Black && userPlaysFirst -> {
                    firstPlayer = Player.Black
                    virtualSecondPlayer = el.arn.opencheckers.game.VirtualPlayer(Player.White, opponentsDifficulty, boardSize)
                }
                usersTeam == Player.Black && !userPlaysFirst -> {
                    firstPlayer = Player.White
                    virtualFirstPlayer = el.arn.opencheckers.game.VirtualPlayer(Player.White, opponentsDifficulty, boardSize)
                }
            }


            val gameCore = newGameCore(firstPlayer!!)
            gameAsyncCoordinator = GameAsyncCoordinator(gameCore, piecesManager, tilesManager, undoRedoDataBridgeGateB, virtualFirstPlayer, virtualSecondPlayer, firstPlayer)
        }

        fun startNewMultiplayerGame(firstPlayerTeam: Player, boardSize: Int, piecesManager: PiecesManager, tilesManager: TilesManager, undoRedoDataBridgeGateB: UndoRedoDataBridgeGateB) {
            val gameCore = newGameCore(firstPlayerTeam)
            gameAsyncCoordinator = GameAsyncCoordinator(gameCore, piecesManager, tilesManager, undoRedoDataBridgeGateB, null, null, firstPlayerTeam)
        }

        fun startNewAllVirtualGame(firstPlayerTeam: Player, difficulty: Difficulties, boardSize: Int, piecesManager: PiecesManager, tilesManager: TilesManager, undoRedoDataBridgeGateB: UndoRedoDataBridgeGateB) {

            val gameCore = newGameCore(firstPlayerTeam)
            val virtualFirstPlayer = el.arn.opencheckers.game.VirtualPlayer(firstPlayerTeam, difficulty, boardSize)
            val virtualSecondPlayer = el.arn.opencheckers.game.VirtualPlayer(firstPlayerTeam.opponent(), difficulty, boardSize)

            gameAsyncCoordinator = GameAsyncCoordinator(gameCore, piecesManager, tilesManager, undoRedoDataBridgeGateB, virtualFirstPlayer, virtualSecondPlayer, firstPlayerTeam)
        }

        private fun newGameCore(startingPlayer: Player): GameCoreImpl {
            val logicConfig = GameLogicConfigImpl(
                gamePreferencesManager.isCapturingMandatory.value,
                gamePreferencesManager.kingBehaviour.value,
                gamePreferencesManager.canPawnCaptureBackwards.value)

            val board = BoardImpl(gamePreferencesManager.boardSize.value, gamePreferencesManager.startingRows.value)
            val game = GameImpl(logicConfig, board, startingPlayer, null) //todo why null?
            return GameCoreImpl(game)
        }
    }

}