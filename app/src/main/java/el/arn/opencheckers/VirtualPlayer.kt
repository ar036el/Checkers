package el.arn.opencheckers

import android.os.AsyncTask
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import el.arn.opencheckers.checkers_game.virtual_player.CheckersGameState
import el.arn.opencheckers.checkers_game.virtual_player.CheckersMove
import el.arn.opencheckers.checkers_game.virtual_player.MinimaxVirtualPlayerImpl

class VirtualPlayer(private val gameData: GameData, private var difficulty: Difficulty, private var boardSize: Int, val player: Player) {

    private val minimaxAgent = MinimaxVirtualPlayerImpl<CheckersMove>()

    class DelegateWrapper(var delegate: Delegate?)
    private val delegateWrapper = DelegateWrapper(null)

    var delegate: Delegate? = null
        set(value) {
            delegateWrapper.delegate = value
            field = delegate
        }

    var isCalculating = false
        private set

    private var moveAsyncTask: MoveAsyncTask? = null


    fun calculateNextMove() {
        moveAsyncTask = MoveAsyncTask(this)
        moveAsyncTask?.execute()
    }

    fun cancelTaskIfRunning() {
        moveAsyncTask?.cancel(true)
        minimaxAgent.cancelComputationProcess()
    }

    fun setDifficulty(difficulty: Difficulty, boardSize: Int) {
        this.difficulty = difficulty
        this.boardSize = boardSize
    }

    private fun getDepthLevel() =
        when (difficulty) {
            Difficulty.Easy -> 1
            Difficulty.Medium -> 2
            Difficulty.Hard -> 3 //TODO what about board size?
        }

    private class MoveAsyncTask(val parent: VirtualPlayer) : AsyncTask<Unit, Unit, CheckersMove>() {

        override fun onPreExecute(){

            if (parent.isCalculating) throw InternalError()
            parent.isCalculating = true
            parent.delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
        }

        override fun doInBackground(vararg params: Unit): CheckersMove? {
            return parent.gameData.game.let {
                parent.minimaxAgent.getMove(CheckersGameState(it, parent.player), parent.getDepthLevel())
            }
        }

        override fun onPostExecute(result: CheckersMove?) {
            parent.isCalculating = false
            parent.delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
            if (result == null) {
                return //TODo what is this?
            }
            val from = result.fromTile
            val to = result.move.to
            parent.delegateWrapper.delegate?.choseAMove(from.x, from.y, to.x, to.y)
        }


        override fun onCancelled(result: CheckersMove?) { //TODO it's not cancelling immediately!
            parent.isCalculating = false
            parent.delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
        }
    }


    interface Delegate {
        fun virtualPlayerDelegateStateHasChanged()
        fun choseAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int)
    }
}

enum class Difficulty { Easy, Medium, Hard }
