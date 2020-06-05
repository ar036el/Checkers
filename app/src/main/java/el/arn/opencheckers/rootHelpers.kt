package el.arn.opencheckers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import el.arn.opencheckers.checkers_game.game_core.Game
import el.arn.opencheckers.checkers_game.game_core.PlayableBoard
import el.arn.opencheckers.checkers_game.game_core.configurations.BoardConfig
import el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig
import el.arn.opencheckers.checkers_game.game_core.exceptions.ConfigurationException
import el.arn.opencheckers.checkers_game.game_core.implementations.BoardConfigImpl
import el.arn.opencheckers.checkers_game.game_core.implementations.BoardImpl
import el.arn.opencheckers.checkers_game.game_core.implementations.GameImpl
import el.arn.opencheckers.checkers_game.game_core.implementations.GameLogicConfigImpl
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import el.arn.opencheckers.feedback_manager.CustomReportSenderFactory
import el.arn.opencheckers.feedback_manager.FeedbackManager
import el.arn.opencheckers.kol_minei.ToastMaker
import el.arn.opencheckers.purchase_manager.PurchasesManager
import org.acra.ACRA
import org.acra.annotation.AcraCore


object StringsRes {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return App.instance.resources.getString(stringRes, *formatArgs)
    }

}
object Integers {
    fun get(@IntegerRes integerRes: Int): Int {
        return App.instance.resources.getInteger(integerRes)
    }
}
object Booleans {
    fun get(@BoolRes booleanRes: Int): Boolean {
        return App.instance.resources.getBoolean(booleanRes)
    }
}




val resources: Resources
    get() = App.instance.resources

val sharedPrefs
    get() = App.instance.sharedPrefs



@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
)
class App : android.app.Application() {
    companion object Obj {
        lateinit var instance: App private set

        lateinit var gameLogicConfig: GameLogicConfig
        lateinit var boardConfig: BoardConfig

        var tilesInBoard = 0

        var virtualPlayerWhite: VirtualPlayer? = null
        var virtualPlayerBlack: VirtualPlayer? = null

        lateinit var settingsThatRequiresANewGame: SettingsThatRequiresANewGame

        lateinit var gameLogicConfigListener: SharedPreferences.OnSharedPreferenceChangeListener

        var gameData: GameData? = null
    }

    //TODo put a mark that virtual player can be slow with large boards

    lateinit var sharedPrefs: SharedPreferences

    val feedbackManager = FeedbackManager()
    val purchasesManager = PurchasesManager(this)
    val toastMaker = ToastMaker(this)

    override fun onCreate() {
        super.onCreate()
        instance = this

        sharedPrefs = getSharedPreferences(resources.getString(R.string.prefCategory_main), Context.MODE_PRIVATE)

        try {
            gameLogicConfig = CreateConfigFromPrefs.gameLogicConfig()
            boardConfig = CreateConfigFromPrefs.boardConfig()
        } catch (e: ConfigurationException) {
            sharedPrefs.edit().clear().commit() //TODo it cancels also the onboarting preference
            gameLogicConfig = CreateConfigFromPrefs.gameLogicConfig()
            boardConfig = CreateConfigFromPrefs.boardConfig()
        }

        tilesInBoard = getValueFromSettings.boardSize()

        settingsThatRequiresANewGame = SettingsThatRequiresANewGame(this)
        registerGameLogicConfigurationListener()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }


    fun registerGameLogicConfigurationListener() {
        gameLogicConfigListener = SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences, key: String ->
            when (key) {
                getString(R.string.pref_isCapturingMandatory) -> gameLogicConfig.isCapturingMandatory = getValueFromSettings.isCapturingMandatory()
                getString(R.string.pref_kingBehaviour) -> {
                    gameLogicConfig.kingBehaviour = getValueFromSettings.kingBehaviour()
                }
                getString(R.string.pref_canManCaptureBackwards) -> gameLogicConfig.canPawnCaptureBackwards = getValueFromSettings.canPawnCaptureBackwards()
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(gameLogicConfigListener)
    }

    object CreateConfigFromPrefs {
        fun gameLogicConfig(): GameLogicConfig {
            return GameLogicConfigImpl(
                getValueFromSettings.isCapturingMandatory(),
                getValueFromSettings.kingBehaviour(),
                getValueFromSettings.canPawnCaptureBackwards())
        }
        fun boardConfig(): BoardConfig {
            return BoardConfigImpl(
                getValueFromSettings.boardSize(),
                getValueFromSettings.startingRows()
            )
        }
    }

    fun startNewMultiplayerGame(startingPlayer: Player) {
        startNewGame(startingPlayer)
    }

    fun startNewSinglePlayerGame(startingPlayer: Player, virtualPlayerSide: Player, difficulty: Difficulty) {
        startNewGame(startingPlayer, virtualPlayerSide, difficulty)
    }

    private fun startNewGame(startingPlayer: Player, virtualPlayerSide: Player? = null, difficulty: Difficulty? = null) {

        virtualPlayerBlack?.cancelTaskIfRunning()
        virtualPlayerWhite?.cancelTaskIfRunning()
        virtualPlayerBlack?.delegate = null
        virtualPlayerWhite?.delegate = null

        gameLogicConfig = CreateConfigFromPrefs.gameLogicConfig()
        boardConfig = CreateConfigFromPrefs.boardConfig()

        tilesInBoard = getValueFromSettings.boardSize()

        val board: PlayableBoard = BoardImpl(tilesInBoard, getValueFromSettings.startingRows())
        val game: Game = GameImpl(gameLogicConfig, board, startingPlayer, null)
        val player1CapturedPieces = mutableListOf<Piece>()
        val player2CapturedPieces = mutableListOf<Piece>()
        val gameHistory = GameHistoryManager()

        gameData = GameData(gameHistory, startingPlayer, game, player1CapturedPieces, player2CapturedPieces)

        if (virtualPlayerSide != null && difficulty != null) {
            virtualPlayerWhite = VirtualPlayer(gameData!!, difficulty, tilesInBoard, Player.White)
            virtualPlayerBlack = VirtualPlayer(gameData!!, difficulty, tilesInBoard, Player.Black)
//            virtualPlayerWhite = if (virtualPlayerSide == Player.White) VirtualPlayer(gameData!!, difficulty, tilesInBoard, Player.White) else null
//            virtualPlayerBlack = if (virtualPlayerSide == Player.Black) VirtualPlayer(gameData!!, difficulty, tilesInBoard, Player.Black) else null
        }

        if (virtualPlayerWhite == null || virtualPlayerBlack == null) {
            gameData!!.saveStateToHistory()
        }

    }

}

class SettingsThatRequiresANewGame(val context: Context) { //TODo Terrible name

    private var triggered = false

    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val boardSizePrefKey = context.resources.getString(R.string.pref_boardSize)
    private val customBoardSizePrefKey = context.resources.getString(R.string.pref_customBoardSize)
    private val customStartingRowsPrefKey = context.resources.getString(R.string.pref_customStartingRows)
    private val isCustomEnabledPrefKey = context.resources.getString(R.string.pref_customSettingsEnabled)

    init {
        registerPrefListeners()
    }

    fun showDialogIfTriggered(activity: MainActivity) { //TODO needs to become Activity and the showNewGameDialogNeedsToBeExctracted
        if (triggered) {
            triggered = false
            showDialog(activity)
        }
    }

    private fun registerPrefListeners() {
        val sharedPrefs = App.instance.getSharedPreferences(
            App.instance.resources.getString(R.string.prefCategory_main),
            Context.MODE_PRIVATE
        )

        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            if (key == boardSizePrefKey
                || key == customBoardSizePrefKey
                || key == customStartingRowsPrefKey
                || key == isCustomEnabledPrefKey) {

                val boardSizeHasChanged = isPrefDifferentFromValue(
                    sharedPrefs,
                    boardSizePrefKey,
                    R.string.pref_boardSize_defaultValue,
                    App.boardConfig.boardSize,
                    false)

                val startingRowsHasChanged = isPrefDifferentFromValue(
                    sharedPrefs,
                    customStartingRowsPrefKey,
                    R.string.pref_customStartingRows_defaultValue,
                    context.resources.getString(R.string.pref_customStartingRows_defaultValue).toInt(),
                    false)

                val customBoardSizeHasChanged = isPrefDifferentFromValue(
                    sharedPrefs,
                    customBoardSizePrefKey,
                    R.string.pref_customBoardSize_defaultValue,
                    App.boardConfig.boardSize,
                    true)


                val customStartingRowsHasChanged = isPrefDifferentFromValue(
                    sharedPrefs,
                    customStartingRowsPrefKey,
                    R.string.pref_customStartingRows_defaultValue,
                    App.boardConfig.startingRowsForEachPlayer,
                    true)


                triggered = boardSizeHasChanged || startingRowsHasChanged || customBoardSizeHasChanged || customStartingRowsHasChanged
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    private fun isPrefDifferentFromValue(pref: SharedPreferences, key: String, defValueId: Int, currentValue: Int, belongToCustomSetting: Boolean): Boolean {
        val newValue = pref.getString(key, context.resources.getString(defValueId))!!.toInt()
        val isCustomSettingEnabled = pref.getBoolean(isCustomEnabledPrefKey, context.resources.getBoolean(R.bool.pref_customSettingsEnabled_defaultValue))

        return (newValue != currentValue && belongToCustomSetting == isCustomSettingEnabled)
    }

    private fun showDialog(activity: MainActivity) {
        AlertDialog.Builder(activity)
//            .setTitle("Delete entry")
            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.
            .setNegativeButton("continue current game", null)
            .setPositiveButton("new game") { _, _ -> activity.showNewGameDialog() } //TODo make NewGameDialog(activity)
            .show()
    }
}