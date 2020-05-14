package el.arn.opencheckers

import android.os.AsyncTask
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import el.arn.opencheckers.checkers_game.virtual_player.CheckersGameState
import el.arn.opencheckers.checkers_game.virtual_player.CheckersMove
import el.arn.opencheckers.checkers_game.virtual_player.MinimaxVirtualPlayerImpl

class VirtualPlayer(private val gameData: GameData) {

    private val virtualPlayer = MinimaxVirtualPlayerImpl<CheckersMove>()

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


    fun chooseAMove() {
        moveAsyncTask = MoveAsyncTask(delegateWrapper, gameData, virtualPlayer, 4)
        moveAsyncTask?.execute()
    }

    fun cancelTaskIfRunning() {
        moveAsyncTask?.cancel(true)
    }


    private class MoveAsyncTask(
        val delegateWrapper: DelegateWrapper,
        val gameData: GameData, //Todo depend it
        val virtualPlayer: MinimaxVirtualPlayerImpl<CheckersMove>,
        val depthLevel: Int
    ) : AsyncTask<Unit, Unit, CheckersMove>() {

        override fun onPreExecute(){
            if (Application.isVirtualPlayerCalculatingMove) throw InternalError()
            Application.isVirtualPlayerCalculatingMove = true
            delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
        }

        override fun doInBackground(vararg params: Unit): CheckersMove? {
            return gameData.game.let {
                virtualPlayer.getMove(CheckersGameState(it, gameData.player2), depthLevel)
            }
        }

        override fun onPostExecute(result: CheckersMove?) {
            Application.isVirtualPlayerCalculatingMove = false
            delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
            if (result == null) {
                return //TODo what is this?
            }
            val from = result.fromTile
            val to = result.move.to
            delegateWrapper.delegate?.choseAMove(from.x, from.y, to.x, to.y)
        }


        override fun onCancelled(result: CheckersMove?) { //TODO it's not cancelling immediately!
            Application.isVirtualPlayerCalculatingMove = false
            delegateWrapper.delegate?.virtualPlayerDelegateStateHasChanged()
        }
    }


    interface Delegate {
        fun virtualPlayerDelegateStateHasChanged()
        fun choseAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int)
    }
}