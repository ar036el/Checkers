package el.arn.opencheckers.virtual_player

import android.os.AsyncTask
import el.arn.opencheckers.Difficulty
import el.arn.opencheckers.checkers_game.game_core.Game
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import el.arn.opencheckers.checkers_game.virtual_player.CheckersGameState
import el.arn.opencheckers.checkers_game.virtual_player.CheckersMove
import el.arn.opencheckers.checkers_game.virtual_player.MinimaxVirtualPlayer
import el.arn.opencheckers.checkers_game.virtual_player.MinimaxVirtualPlayerImpl
import el.arn.opencheckers.delegationMangement.HoldsDelegate
import el.arn.opencheckers.delegationMangement.DelegateManager

class VirtualPlayer(
    private val assignedPlayer: Player,
    private val difficulty: Difficulty,
    private val boardSize: Int,
    private val delegationMgr: DelegateManager<Delegate>
) : HoldsDelegate<VirtualPlayer.Delegate> by delegationMgr {

    var isCalculating = true
        private set

    fun getNextMove(gameClone: Game) {
        val _taskInProgress = MoveCalculation(this, gameClone)
        taskInProgress = _taskInProgress
        _taskInProgress.execute()
    }

    fun cancelCalculationIfRunning() {
        taskInProgress?.cancel(true)
        minimaxAgent.cancelComputationProcess()
    }

    interface Delegate {
        fun startedCalculating()
        fun finishedCalculatingAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int)
        fun calculationWasCanceled()
    }

    private var taskInProgress: MoveCalculation? = null
    private val minimaxAgent: MinimaxVirtualPlayer<CheckersMove> = MinimaxVirtualPlayerImpl()

    private fun getDepthLevel(): Int {
        return when (difficulty) {
            Difficulty.Easy -> 2
            Difficulty.Medium -> if (boardSize >= 12) 2 else 3
            Difficulty.Hard -> if (boardSize >= 12) 3 else 4
        }
    }

    private class MoveCalculation(
        private val root: VirtualPlayer,
        private val gameClone: Game
    ) : AsyncTask<Unit, Unit, CheckersMove>() {

        override fun onPreExecute(){
            if (root.isCalculating) throw InternalError()
            root.isCalculating = true
            root.delegationMgr.notify { it.startedCalculating() }
        }

        override fun doInBackground(vararg params: Unit): CheckersMove? {
            return gameClone.let {
                root.minimaxAgent.getMove(CheckersGameState(it, root.assignedPlayer), root.getDepthLevel())
            }
        }

        override fun onPostExecute(result: CheckersMove?) {
            if (!root.isCalculating) throw InternalError()
            root.isCalculating = false

            if (result == null) { return } //TODo what is this???
            val from = result.fromTile
            val to = result.move.to
            root.delegationMgr.notify { it.finishedCalculatingAMove(from.x, from.y, to.x, to.y) }
        }

        override fun onCancelled(result: CheckersMove?) {
            if (!root.isCalculating) throw InternalError()
            root.isCalculating = false

            root.delegationMgr.notify { it.calculationWasCanceled() }
        }
    }

}