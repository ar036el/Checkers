package el.arn.checkers.activity_widgets.main_activity.toolbar

import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.android.stringFromRes
import el.arn.checkers.helpers.listeners_engine.ListenersManager
import el.arn.checkers.helpers.listeners_engine.HoldsListeners

abstract class ToolbarAbstract(
    protected val listenersMgr: ListenersManager<Listener> = ListenersManager()
) : HoldsListeners<ToolbarAbstract.Listener> by listenersMgr {

    object TitleTextOptions {
        val NO_GAME_LOADED = stringFromRes(R.string.toolbar_title_noGameLoaded)
        val FIRST_PLAYER_TURN = stringFromRes(R.string.toolbar_title_firstPlayersTurn)
        val SECOND_PLAYER_TURN = stringFromRes(R.string.toolbar_title_secondPlayersTurn)
        val USER_TURN = stringFromRes(R.string.toolbar_title_userTurn)
        val PC_TURN = stringFromRes(R.string.toolbar_title_pcTurn)

        val FIRST_PLAYER_WON = stringFromRes(R.string.toolbar_title_firstPlayerIsWinner)
        val SECOND_PLAYER_WON = stringFromRes(R.string.toolbar_title_secondPlayerIsWinner)
        val USER_WON = stringFromRes(R.string.toolbar_title_userWon)
        val USER_LOST = stringFromRes(R.string.toolbar_title_userLost)
    }

    abstract var undoButtonEnabled: Boolean
    abstract var redoButtonEnabled: Boolean
    abstract var progressBarVisible: Boolean
    abstract var titleText: String
    abstract var timerTimeInSeconds: Int


    interface Listener {
        fun menuButtonWasClicked()
        fun undoButtonWasClicked()
        fun redoButtonWasClicked()
        fun newGameButtonWasClicked()
        fun settingsButtonWasClicked()
    }

    protected fun timeInSecondsToTimerTime(totalSeconds: Int): String {
        val hours: Int = totalSeconds / 3600
        val minutes: Int = (totalSeconds % 3600) / 60
        val seconds: Int = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}