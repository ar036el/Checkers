package el.arn.opencheckers.android_widgets.main_activity.toolbar

import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners

abstract class ToolbarAbstract(
    protected val listenersMgr: ListenersManager<Listener> = ListenersManager()
) : HoldsListeners<ToolbarAbstract.Listener> by listenersMgr {

    object TitleTextOptions {
        const val NO_GAME_LOADED = "Open Checkers" //Todo get it all to resource
        const val FIRST_PLAYER_TURN = "Player 1's turn"
        const val SECOND_PLAYER_TURN = "Player 2's turn"
        const val FIRST_PLAYER_WON = "Player 1 - Winner"
        const val SECOND_PLAYER_WON = "Player 2 - Winner"
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

    protected fun formatSecondsToTimerString(totalSeconds: Int): String {
        val hours: Int = totalSeconds / 3600
        val minutes: Int = (totalSeconds % 3600) / 60
        val seconds: Int = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}