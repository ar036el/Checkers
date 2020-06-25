package el.arn.opencheckers.game.parts

import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.GameLogicListener
import el.arn.opencheckers.game.game_core.game_core.ReadableBoard
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig
import el.arn.opencheckers.game.game_core.game_core.structs.Move
import el.arn.opencheckers.game.game_core.game_core.structs.Piece
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.game.game_core.game_core.structs.Tile

interface Undoable : HoldsListeners<Undoable.Listener> {
    fun saveAsLatest()
    val canUndo: Boolean
    val canRedo: Boolean
    fun undo():/** @return if successful. If unsuccessful, Listeners are not notified*/ Boolean
    fun redo():/** @return if successful. If unsuccessful, Listeners are not notified*/ Boolean

    interface Listener {
        fun undoWasMade()
        fun redoWasMade()
    }
}

class UndoableWithSnapshots<S>(
    private val snapshotable: Snapshotable<S>,
    private val delegationMgr: ListenersManager<Undoable.Listener> = ListenersManager()
) : Undoable, HoldsListeners<Undoable.Listener> by delegationMgr {
    private val history = mutableListOf<Snapshotable.Snapshot<S>>()
    private var currentIndex = -1

    private fun MutableList<*>.removeAllAfterIndexOf(index: Int) =
        this.subList(index+1, history.size).clear()

    override fun saveAsLatest() {
        history.removeAllAfterIndexOf(currentIndex)
        val snapshot = snapshotable.createSnapshot()
        history.add(snapshot)
        currentIndex = history.lastIndex
    }

    override val canUndo: Boolean
        get() = currentIndex > 0
    override val canRedo: Boolean
        get() = currentIndex < history.lastIndex

    override fun undo(): Boolean {
        if (!canUndo) { return false }
        snapshotable.loadFromSnapshot(history[--currentIndex])
        return true
    }
    override fun redo(): Boolean {
        if (!canRedo) { return false }
        snapshotable.loadFromSnapshot(history[++currentIndex])
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
    override fun getWinner(): Player = applySync{ it.winner }
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
    override fun reloadAvailableMoves() = applySync{ it.reloadAvailableMoves() }
    override fun makeAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int): Tile = applySync{ it.makeAMove(xFrom, yFrom, xTo, yTo) }
    override fun setListener(Listener: GameLogicListener) = applySync{ it.setListener(Listener) }

    override fun createSnapshot(): Snapshotable.Snapshot<Game> {
        return synchronized(gameWrapper){
            Snapshotable.Snapshot(gameWrapper.game.clone())
        }
    }

    override fun loadFromSnapshot(snapshot: Snapshotable.Snapshot<Game>) {
        synchronized(gameWrapper){
            gameWrapper.game = snapshot.content
        }
    }

}