package el.arn.checkers.game.game_core

import el.arn.checkers.helpers.listeners_engine.ListenersManager
import el.arn.checkers.helpers.listeners_engine.HoldsListeners
import el.arn.checkers.game.game_core.checkers_game.Game
import el.arn.checkers.game.game_core.checkers_game.GameLogicListener
import el.arn.checkers.game.game_core.checkers_game.ReadableBoard
import el.arn.checkers.game.game_core.checkers_game.configurations.GameLogicConfig
import el.arn.checkers.game.game_core.checkers_game.structs.Move
import el.arn.checkers.game.game_core.checkers_game.structs.Piece
import el.arn.checkers.game.game_core.checkers_game.structs.Player
import el.arn.checkers.game.game_core.checkers_game.structs.Tile

interface Undoable : HoldsListeners<Undoable.Listener> {
    fun saveSnapshotAsLatest()
    val canUndo: Boolean
    val canRedo: Boolean
    val currentSnapshotIndex: Int
    fun undo():/** @return if successful. If unsuccessful, Listeners are not notified*/ Boolean
    fun redo():/** @return if successful. If unsuccessful, Listeners are not notified*/ Boolean

    interface Listener {
        fun undoWasMade()
        fun redoWasMade()
    }
}

class UndoableWithSnapshots<S>(
    private val snapshotable: Snapshotable<S>,
    private val listenersMgr: ListenersManager<Undoable.Listener> = ListenersManager()
) : Undoable, HoldsListeners<Undoable.Listener> by listenersMgr {

    override var currentSnapshotIndex = -1
    private val history = mutableListOf<Snapshotable.Snapshot<S>>()

    private fun MutableList<*>.removeAllAfterIndexOf(index: Int) =
        this.subList(index+1, this.size).clear()

    override fun saveSnapshotAsLatest() {
        history.removeAllAfterIndexOf(currentSnapshotIndex)
        val snapshot = snapshotable.createSnapshot()
        history.add(snapshot)
        currentSnapshotIndex = history.lastIndex
    }

//    override fun removeSnapshotAt(indexToRemove: Int) {
//        if (currentSnapshotIndex > indexToRemove || (currentSnapshotIndex == indexToRemove && currentSnapshotIndex == history.lastIndex)) {
//            currentSnapshotIndex--
//        }
//        history.removeAt(indexToRemove)
//    }

    override val canUndo: Boolean
        get() = currentSnapshotIndex > 0
    override val canRedo: Boolean
        get() = currentSnapshotIndex < history.lastIndex

    override fun undo(): Boolean {
        if (!canUndo) { return false }
        --currentSnapshotIndex
        val a = history[currentSnapshotIndex]
        snapshotable.loadFromSnapshot(history[currentSnapshotIndex])

        return true
    }
    override fun redo(): Boolean {
        if (!canRedo) { return false }
        ++currentSnapshotIndex
        val a = history[currentSnapshotIndex]
        snapshotable.loadFromSnapshot(history[currentSnapshotIndex])
        return true
    }

}

interface Snapshotable<C> {
    fun createSnapshot(): Snapshot<C>
    fun loadFromSnapshot(snapshot: Snapshot<C>)
    class Snapshot<C>(val content: C)
}

//TODO because it's synchronized, does it need to be async when called from coordinator?
class SynchronizedSnapshotableGame(game: Game) : Game,
    Snapshotable<Game> {

    private val gameWrapper = object { var game: Game = game }
    private fun <R>applySync(action: (game: Game) -> R) : R {
        return synchronized(gameWrapper){
            action(gameWrapper.game)
        }
    }

    override fun getPiece(x: Int, y: Int): Piece = applySync{ it.getPiece(x, y) }
    override fun getAvailableMovesForPiece(x: Int, y: Int): MutableSet<Move> = applySync{ it.getAvailableMovesForPiece(x, y) }
    override fun getBoardSize() = applySync{ it.boardSize }
    override fun getStartingRows(): Int? = applySync{ it.startingRows }
    override fun getWinner(): Player? = applySync{ it.winner }
    override fun getBoard(): ReadableBoard = applySync{ it.board }
    override fun passTurn() = applySync{ it.passTurn() }
    override fun canPassExtraTurn() = applySync{ it.canPassExtraTurn() }
    override fun getCurrentPlayer(): Player = applySync{ it.currentPlayer }
    override fun refreshGame() = applySync{ it.refreshGame() }
    override fun getConfig(): GameLogicConfig = applySync{ it.config }
    override fun clone() = applySync{ it.clone() }
    override fun isExtraTurn() = applySync{ it.isExtraTurn }
    override fun getAvailablePieces(): Set<Tile> = applySync{ it.availablePieces }
    override fun getAllPiecesForPlayer(player: Player): Set<Tile> = applySync{ it.getAllPiecesForPlayer(player) }
    override fun getAllPiecesInBoard(): Set<Tile> = applySync { it.allPiecesInBoard }
    override fun reloadAvailableMoves() = applySync{ it.reloadAvailableMoves() }
    override fun makeAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int): Tile? = applySync{ it.makeAMove(xFrom, yFrom, xTo, yTo) }
    override fun setListener(Listener: GameLogicListener) = applySync{ it.setListener(Listener) }

    override fun createSnapshot(): Snapshotable.Snapshot<Game> {
        return synchronized(gameWrapper){
            Snapshotable.Snapshot(gameWrapper.game.clone())
        }
    }

    override fun loadFromSnapshot(snapshot: Snapshotable.Snapshot<Game>) {
        synchronized(gameWrapper){
            gameWrapper.game = snapshot.content.clone()
            gameWrapper.game.refreshGame()
        }
    }

}