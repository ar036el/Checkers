package el.arn.opencheckers

import el.arn.opencheckers.checkers_game.game_core.Game
import el.arn.opencheckers.checkers_game.game_core.implementations.GameImpl
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player

class GameData(
    private val gameHistory: GameHistoryManager,
    val player1: Player,
    game: Game ,
    player1CapturedPieces: MutableList<Piece> = mutableListOf(),
    player2CapturedPieces: MutableList<Piece> = mutableListOf()
) {

    var game = game
        private set

    var player1CapturedPieces = player1CapturedPieces
        private set

    var player2CapturedPieces = player2CapturedPieces
        private set

    val player2: Player = player1.opponent()


    val hasUndo get() = gameHistory.haveEntriesBehind
    fun undo() {
        copyHistoryEntryIntoPublicData(gameHistory.getPrevEntry())
    }

    val hasRedo get() = gameHistory.haveEntriesInFront
    fun redo() {
        copyHistoryEntryIntoPublicData(gameHistory.getNextEntry())
    }

    fun saveStateToHistory() {
        gameHistory.saveEntry(game, player1CapturedPieces, player2CapturedPieces)
    }

    private fun copyHistoryEntryIntoPublicData(entry: GameHistoryManager.Entry) {
        game = GameImpl(entry.game as GameImpl)
        player1CapturedPieces = entry.player1CapturedPieces.toMutableList()
        player2CapturedPieces = entry.player2CapturedPieces.toMutableList()
    }

}


class GameHistoryManager() {
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

    val haveEntriesBehind get() = (currentIndex > 0)
    fun getPrevEntry(): Entry {
        if (!haveEntriesBehind) throw InternalError()
        return list[--currentIndex]
    }

    val haveEntriesInFront get() = (currentIndex < list.lastIndex)
    fun getNextEntry(): Entry {
        if (!haveEntriesInFront) throw InternalError()
        return list[++currentIndex]
    }

    enum class Operation { Undo, Redo}

}