package el.arn.opencheckers.game

import android.os.AsyncTask
import el.arn.opencheckers.complementaries.game.Difficulties
import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.game.game_core.game_core.structs.Point
import el.arn.opencheckers.game.game_core.virtual_player.CheckersGameState
import el.arn.opencheckers.game.game_core.virtual_player.CheckersMove
import el.arn.opencheckers.game.game_core.virtual_player.MinimaxVirtualPlayer
import el.arn.opencheckers.game.game_core.virtual_player.MinimaxVirtualPlayerImpl
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners

class VirtualPlayer(
    val assignedPlayer: Player,
    private val difficulty: Difficulties,
    private val boardSize: Int,
    private val delegationMgr: ListenersManager<Listener> = ListenersManager()
) : HoldsListeners<VirtualPlayer.Listener> by delegationMgr {

    var isCalculating = true
        private set

    fun calculateNextMoveInBackground(gameClone: Game) {
        taskInProgress?.let { if (it.status != AsyncTask.Status.FINISHED) { throw IllegalStateException("calculation is already running") } }
        val taskInProgress =
            MoveCalculationAsyncTask(
                this,
                gameClone
            )
        this.taskInProgress = taskInProgress
        taskInProgress.execute()
    }

    fun cancelCalculationIfRunning() {
        taskInProgress?.cancel(true)
        minimaxAgent.cancelComputationProcess()
    }

    interface Listener {
        fun startedCalculating() {}
        fun finishedCalculatingFoundAMove(from: Point, to: Point) {}
        fun calculationWasCanceled() {}
    }

    private var taskInProgress: MoveCalculationAsyncTask? = null
    private val minimaxAgent: MinimaxVirtualPlayer<CheckersMove> = MinimaxVirtualPlayerImpl()

    private fun getDepthLevel(): Int {
        return when (difficulty) {
            Difficulties.Easy -> 2
            Difficulties.Medium -> if (boardSize >= 12) 2 else 3
            Difficulties.Hard -> if (boardSize >= 12) 3 else 4
        }
    }

    private class MoveCalculationAsyncTask(
        private val root: VirtualPlayer,
        private val gameClone: Game
    ) : AsyncTask<Unit, Unit, CheckersMove>() {

        override fun onPreExecute(){
            if (root.isCalculating) throw InternalError()
            root.isCalculating = true
            root.delegationMgr.notifyAll { it.startedCalculating() }
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
            root.delegationMgr.notifyAll { it.finishedCalculatingFoundAMove(Point(from.x, from.y), Point(to.x, to.y)) }
        }

        override fun onCancelled(result: CheckersMove?) {
            if (!root.isCalculating) throw InternalError()
            root.isCalculating = false

            root.delegationMgr.notifyAll { it.calculationWasCanceled() }
        }
    }

}