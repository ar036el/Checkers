package el.arn.checkers.android_widgets.main_activity.toolbar

import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.complementaries.listener_mechanism.ListenersManager
import el.arn.checkers.complementaries.listener_mechanism.HoldsListeners

abstract class ToolbarAbstract(
    protected val listenersMgr: ListenersManager<Listener> = ListenersManager()
) : HoldsListeners<ToolbarAbstract.Listener> by listenersMgr {

    object TitleTextOptions {
        val NO_GAME_LOADED = appRoot.getStringRes(R.string.toolbar_title_noGameLoaded)
        val FIRST_PLAYER_TURN = appRoot.getStringRes(R.string.toolbar_title_firstPlayersTurn)
        val SECOND_PLAYER_TURN = appRoot.getStringRes(R.string.toolbar_title_secondPlayersTurn)
        val FIRST_PLAYER_WON = appRoot.getStringRes(R.string.toolbar_title_firstPlayerIsWinner)
        val SECOND_PLAYER_WON = appRoot.getStringRes(R.string.toolbar_title_secondPlayerIsWinner)
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