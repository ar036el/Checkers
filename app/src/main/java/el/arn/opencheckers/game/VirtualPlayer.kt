package el.arn.opencheckers.game

import android.os.AsyncTask
import el.arn.opencheckers.complementaries.game.DifficultyEnum
import el.arn.opencheckers.complementaries.game.TileCoordinates
import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.game.game_core.virtual_player.CheckersGameState
import el.arn.opencheckers.game.game_core.virtual_player.CheckersMove
import el.arn.opencheckers.game.game_core.virtual_player.MinimaxVirtualPlayer
import el.arn.opencheckers.game.game_core.virtual_player.MinimaxVirtualPlayerImpl
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners

interface VirtualPlayer : HoldsListeners<VirtualPlayer.Listener> {

    val playerOrTeam: Player
    val isCalculating: Boolean
    fun calculateNextMoveAsync(gameClone: Game)
    fun cancelCalculationIfRunning()

    interface Listener {
        fun startedCalculating() {}
        fun finishedCalculatingFoundAMove(from: TileCoordinates, to: TileCoordinates) {}
        fun calculationWasCanceled() {}
    }

}

class VirtualPlayerImpl(
    override val playerOrTeam: Player,
    private val difficulty: DifficultyEnum,
    private val boardSize: Int,
    private val listenersMgr: ListenersManager<VirtualPlayer.Listener> = ListenersManager()
) : VirtualPlayer,
    HoldsListeners<VirtualPlayer.Listener> by listenersMgr {

    override var isCalculating = false

    private var taskInProgress: MoveCalculationAsyncTask? = null

    override fun calculateNextMoveAsync(gameClone: Game) {
        taskInProgress?.let { if (isCalculating) {
            throw IllegalStateException("calculation is already running") }
        }
        cancelCalculation()
        val taskInProgress = MoveCalculationAsyncTask(this, gameClone)
        this.taskInProgress = taskInProgress
        taskInProgress.execute()
    }


    override fun cancelCalculationIfRunning() {
        cancelCalculation()
        listenersMgr.notifyAll { it.calculationWasCanceled() }
    }

    private fun cancelCalculation() {
        taskInProgress?.minimaxAgent?.cancelComputationProcess()
        taskInProgress?.cancel(true)
        isCalculating = false
    }

    private fun getDepthLevel(): Int {
        return when (difficulty) {
            DifficultyEnum.Easy -> 2
            DifficultyEnum.Medium -> if (boardSize >= 12) 2 else 3
            DifficultyEnum.Hard -> if (boardSize >= 12) 3 else 4
        }
    }

    private class MoveCalculationAsyncTask(
        private val root: VirtualPlayerImpl,
        private val gameClone: Game
    ) : AsyncTask<Unit, Unit, CheckersMove>() {

        val minimaxAgent: MinimaxVirtualPlayer<CheckersMove> = MinimaxVirtualPlayerImpl()

        override fun onPreExecute() {
             root.isCalculating = true
            root.listenersMgr.notifyAll { it.startedCalculating() }
        }

        override fun doInBackground(vararg params: Unit): CheckersMove? {
            return gameClone.let {
                minimaxAgent.getMove(CheckersGameState(it, root.playerOrTeam), root.getDepthLevel())
            }
        }

        override fun onPostExecute(result: CheckersMove?) {
            if (result == null || !root.isCalculating) { //calculation was cancelled by minimaxAgent but for some reason the asyncTask failed to cancel
                return
            }
            val from = result.fromTile
            val to = result.move.to
            root.isCalculating = false
            root.listenersMgr.notifyAll { it.finishedCalculatingFoundAMove(TileCoordinates(from.x, from.y), TileCoordinates(to.x, to.y)) }
        }

        override fun onCancelled(result: CheckersMove?) {
            root.isCalculating = false
        }
    }

}