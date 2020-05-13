package el.arn.opencheckers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
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
import el.arn.opencheckers.checkers_game.virtual_player.CheckersMove
import el.arn.opencheckers.checkers_game.virtual_player.MinimaxVirtualPlayerImpl

class GameHistory(game: Game, player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
    private val list = mutableListOf<Entry>()
    private var currentIndex = -1

    data class Entry(val game: Game,
                     val player1CapturedPieces: List<Piece>,
                     val player2CapturedPieces: List<Piece>)

    fun saveEntry(game: Game, player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>)   {
        list.subList(currentIndex+1, list.size).clear() //removing redo entries if any
        list.add(Entry(GameImpl(game as GameImpl), player1CapturedPieces.toList(), player2CapturedPieces.toList()))
        currentIndex = list.lastIndex
    }

    val hasUndo get() = (currentIndex > 0)
    fun undo(): Entry {
        if (!hasUndo) throw InternalError()
        return list[--currentIndex]
    }

    val hasRedo get() = (currentIndex < list.lastIndex)
    fun redo(): Entry {
        if (!hasRedo) throw InternalError()
        return list[++currentIndex]
    }

    init {
        saveEntry(game, player1CapturedPieces, player2CapturedPieces)
    }
}

class Application : android.app.Application() {

    companion object Obj {
        lateinit var instance: Application
        lateinit var resources: Resources
        lateinit var settingPrefs: SharedPreferences

        lateinit var gameLogicConfig: GameLogicConfig
        lateinit var boardConfig: BoardConfig

        val player1 = Player.White
        val player2 = Player.Black


        val virtualPlayer = MinimaxVirtualPlayerImpl<CheckersMove>()

        var gameData: GameData? = null
    }

    //TODo put a mark that virtual player can be slow with large boards


    override fun onCreate() {
        super.onCreate()
        instance = this

        settingPrefs = getSharedPreferences(resources.getString(R.string.prefFile_settings), Context.MODE_PRIVATE)
        Obj.resources = resources

        try {
            initGameConfigurations()
        } catch (e: ConfigurationException) {
            settingPrefs.edit().clear().apply() //TODo it cancels also the onboarting preference
            initGameConfigurations()
        }




        val board: PlayableBoard = BoardImpl(boardConfig.boardSize, boardConfig.startingRowsForEachPlayer)
        val game: Game = GameImpl(gameLogicConfig, board, player1, null)
        val player1CapturedPieces = mutableListOf<Piece>()
        val player2CapturedPieces = mutableListOf<Piece>()
        val gameHistory = GameHistory(game, player1CapturedPieces, player2CapturedPieces)

        gameData = GameData(gameHistory, player1, player2, game, player1CapturedPieces, player2CapturedPieces)

    }

    fun initGameConfigurations() {
        gameLogicConfig = GameLogicConfigImpl()
        boardConfig = BoardConfigImpl()

        gameLogicConfig.isCapturingMandatory_setFromPrefs()
        gameLogicConfig.kingBehaviour_setFromPrefs()
        gameLogicConfig.canPawnCaptureBackwards_setFromPrefs()

        boardConfig.boardSize_setFromPrefs()
        boardConfig.startingRowsForEachPlayer_setFromPrefs()

    }

    fun initGameConfigurationsNewButCrap() {
        val toString: (Int) -> String = Application.resources::getString
        val toStringArray: (Int) -> Array<String> = Application.resources::getStringArray
        val toBoolean: (Int) -> Boolean = Application.resources::getBoolean
        val toInt: (Int) -> Int = { Application.resources.getString(it).toInt() }

        gameLogicConfig = GameLogicConfigImpl()
        val isCapturingMandatoryAdapter = BooleanPrefToConfigAdapter(
            toString(R.string.pref_is_capturing_mandatory),
            toBoolean(R.bool.pref_is_capturing_mandatory_defValue),
            gameLogicConfig::setIsCapturingMandatory
        )
        val canPawnCaptureBackwards = StringPrefToConfigAdapter(
            toString(R.string.pref_can_man_capture_backwards),
            toStringArray(R.array.can_man_capture_backwards_entryValues),
            toString(R.string.pref_can_man_capture_backwards_defValue),
            gameLogicConfig::setCanPawnCaptureBackwards,
            GameLogicConfig.CanPawnCaptureBackwardsOptions.values()
        )
        val kingBehaviour = StringPrefToConfigAdapter(
            toString(R.string.pref_king_behaviour),
            toStringArray(R.array.king_behaviour_entryValues),
            toString(R.string.pref_king_behaviour_defValue),
            gameLogicConfig::setKingBehaviour,
            GameLogicConfig.KingBehaviourOptions.values()
        )

        boardConfig = BoardConfigImpl()
        val boardSize = IntPrefToConfigAdapter(
            toString(R.string.pref_board_size),
            toInt(R.string.pref_board_size_defValue),
            boardConfig::setBoardSize
        )
        val startingRowsForEachPlayer = IntPrefToConfigAdapter(
            toString(R.string.pref_custom_starting_rows),
            toInt(R.string.pref_custom_starting_rows_defValue),
            boardConfig::setStartingRowsForEachPlayer
        )

    }
}

fun GameLogicConfig.isCapturingMandatory_setFromPrefs() {
    this.isCapturingMandatory = Application.settingPrefs.getBoolean(
        Application.resources.getString(R.string.pref_is_capturing_mandatory),
        false
    )
}

fun GameLogicConfig.canPawnCaptureBackwards_setFromPrefs() {
    this.canPawnCaptureBackwards = run {
        val entries =
            Application.resources.getStringArray(R.array.can_man_capture_backwards_entryValues)
        when (Application.settingPrefs.getString(
            Application.resources.getString(R.string.pref_can_man_capture_backwards),
            null
        )) {
            entries[0] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.Always
            entries[1] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.OnlyWhenMultiCapture
            entries[2] -> GameLogicConfig.CanPawnCaptureBackwardsOptions.Never
            else -> GameLogicConfig.CanPawnCaptureBackwardsOptions.Never
        }
    }
}

fun GameLogicConfig.kingBehaviour_setFromPrefs() {
    this.kingBehaviour = run {
        val entries = Application.resources.getStringArray(R.array.king_behaviour_entryValues)
        when (Application.settingPrefs.getString(
            Application.resources.getString(R.string.pref_king_behaviour),
            null
        )) {
            entries[0] -> GameLogicConfig.KingBehaviourOptions.FlyingKings
            entries[1] -> GameLogicConfig.KingBehaviourOptions.LandsRightAfterCapture
            entries[2] -> GameLogicConfig.KingBehaviourOptions.NoFlyingKings
            else -> GameLogicConfig.KingBehaviourOptions.NoFlyingKings
        }
    }
}

fun BoardConfig.boardSize_setFromPrefs() {
    Application.boardConfig.boardSize = run {
        if (Application.settingPrefs.getBoolean(
                Application.resources.getString(R.string.pref_enable_custom_settings),
                false
            )
        ) {
            Application.settingPrefs.getString(
                Application.resources.getString(R.string.pref_custom_board_size),
                "8"
            )!!.toInt()
        } else {
            Application.settingPrefs.getString(Application.resources.getString(R.string.pref_board_size), "8")!!
                .toInt()
        }
    }
}

fun BoardConfig.startingRowsForEachPlayer_setFromPrefs() {
    this.startingRowsForEachPlayer = run {
        if (Application.settingPrefs.getBoolean(Application.resources.getString(R.string.pref_enable_custom_settings), false)) {
            Application.settingPrefs.getString(
                Application.resources.getString(R.string.pref_custom_starting_rows),
                "3"
            )!!.toInt()
        } else {
            3
        }
    }
}

class StringPrefToConfigAdapter<ConfigValueType>(
    private val prefKey: String,
    private val prefValues: Array<String>,
    private val prefDefaultValue: String,
    private val configSetter: (value: ConfigValueType) -> Unit,
    private val configValues: Array<ConfigValueType>,
    var delegate: Delegate<ConfigValueType>? = null
    ) {

    private val sharedPrefs = Application.instance.getSharedPreferences(
        Application.instance.resources.getString(R.string.prefFile_settings),
        Context.MODE_PRIVATE
    )

    init {
        if (configValues.size != prefValues.size) throw InternalError("arrays needs to be in the same size")
        configSetter.invoke(getConfigValueFromPref())
        registerPrefListener()
    }

    private fun registerPrefListener() {
        sharedPrefs.registerOnSharedPreferenceChangeListener { _: SharedPreferences, key: String ->
            if (key == prefKey) {
                val newValue = getConfigValueFromPref()
                configSetter.invoke(newValue)
                delegate?.configHasChanged(newValue)
            }
        }
    }

    private fun getConfigValueFromPref(): ConfigValueType {
        val prefValue = sharedPrefs.getString(prefKey, prefDefaultValue)
        return configValues[prefValues.indexOf(prefValue)]
    }


    interface Delegate<ConfigValueType> {
        fun configHasChanged(newValue: ConfigValueType)
    }

}

class BooleanPrefToConfigAdapter(
    private val prefKey: String,
    private val prefDefaultValue: Boolean,
    private val configSetter: (value: Boolean) -> Unit,
    var delegate: Delegate? = null
) {

    private val sharedPrefs = Application.instance.getSharedPreferences(
        Application.instance.resources.getString(R.string.prefFile_settings),
        Context.MODE_PRIVATE
    )

    init {
        configSetter.invoke(getConfigValueFromPref())
        registerPrefListener()
    }

    private fun registerPrefListener() {
        sharedPrefs.registerOnSharedPreferenceChangeListener { _: SharedPreferences, key: String ->
            if (key == prefKey) {
                val newValue = getConfigValueFromPref()
                configSetter.invoke(newValue)
                delegate?.configHasChanged(newValue)
            }
        }
    }

    private fun getConfigValueFromPref(): Boolean {
        return sharedPrefs.getBoolean(prefKey, prefDefaultValue)
    }


    interface Delegate {
        fun configHasChanged(newValue: Boolean)
    }

}

class IntPrefToConfigAdapter(
    private val prefKey: String,
    private val prefDefaultValue: Int,
    private val configSetter: (value: Int) -> Unit,
    var delegate: Delegate? = null
) {

    private val sharedPrefs = Application.instance.getSharedPreferences(
        Application.instance.resources.getString(R.string.prefFile_settings),
        Context.MODE_PRIVATE
    )

    init {
        configSetter.invoke(getConfigValueFromPref())
        registerPrefListener()
    }

    private fun registerPrefListener() {
        sharedPrefs.registerOnSharedPreferenceChangeListener { _: SharedPreferences, key: String ->
            if (key == prefKey) {
                val newValue = getConfigValueFromPref()
                configSetter.invoke(newValue)
                delegate?.configHasChanged(newValue)
            }
        }
    }

    private fun getConfigValueFromPref(): Int {
        return sharedPrefs.getString(prefKey, prefDefaultValue.toString())!!.toInt()
    }


    interface Delegate {
        fun configHasChanged(newValue: Int)
    }

}

class GameData(
    val gameHistory: GameHistory,
    val player1: Player,
    val player2: Player,
    game: Game,
    player1CapturedPieces: MutableList<Piece> = mutableListOf(),
    player2CapturedPieces: MutableList<Piece> = mutableListOf()
) {

    var game = game
        get() { detachFromHistoryIfLoadedFrom(); return field }
        private set

    var player1CapturedPieces = player1CapturedPieces
        get() { detachFromHistoryIfLoadedFrom(); return field }
        private set

    var player2CapturedPieces = player2CapturedPieces
        get() { detachFromHistoryIfLoadedFrom(); return field }
        private set


    private var didLoadFromHistory = false

    var selectedTile: Tile? = null
    val availableTiles = mutableSetOf<Tile>()
    val activatedTilesForSelectedTile = mutableSetOf<Tile>()

    fun loadFromHistory(entry: GameHistory.Entry) {
        didLoadFromHistory = true
        game = entry.game
        player1CapturedPieces = entry.player1CapturedPieces.toMutableList()
        player2CapturedPieces = entry.player2CapturedPieces.toMutableList()
    }

    private fun detachFromHistoryIfLoadedFrom() {
        if (didLoadFromHistory) {
            didLoadFromHistory = false //TODO not thread safe
            game = GameImpl(game as GameImpl)
            player1CapturedPieces = player1CapturedPieces.toMutableList()
            player2CapturedPieces = player2CapturedPieces.toMutableList()
        }
    }
}