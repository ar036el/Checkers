package el.arn.opencheckers.mainActivity.toolbars

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import el.arn.opencheckers.ALPHA_FULL
import el.arn.opencheckers.ALPHA_ICON_DISABLED
import el.arn.opencheckers.MainActivity
import el.arn.opencheckers.R

class ToolbarTop(
    menu: Menu,
    activity: Activity
) : Toolbar() {

    override var undoButtonEnabled: Boolean = true
        set(value) {
            undoButton.isEnabled = value
            undoButton.icon.alpha = if (value) ALPHA_FULL else ALPHA_ICON_DISABLED
            field = value
        }
    override var redoButtonEnabled: Boolean = true
        set(value) {
            redoButton.isEnabled = value
            redoButton.icon.alpha = if (value) ALPHA_FULL else ALPHA_ICON_DISABLED
            field = value
        }
    override var progressBarVisible: Boolean = true
        set(value) {
            progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { toolbarView.title = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) {
            toolbarView.subtitle = formatSecondsToTimerString(value); field = value }

    private val toolbarView: androidx.appcompat.widget.Toolbar = activity.findViewById(R.id.toolbar_top)
    private var menuButton: ImageButton = activity.findViewById(R.id.menuButton_titlebar)
    private var undoButton: MenuItem
    private var redoButton: MenuItem
    private var newGameButton: MenuItem
    private var settingsButton: MenuItem
    private var progressBar: ProgressBar = activity.findViewById(R.id.progressBarTop)

    init {
        activity.menuInflater.inflate(R.menu.toolbar_top, menu)
        undoButton =  menu.findItem(R.id.undo_menu_item)
        redoButton = menu.findItem(R.id.redo_menu_item)
        newGameButton =  menu.findItem(R.id.refresh_menu_item)
        settingsButton =  menu.findItem(R.id.settings_menu_item)

        menuButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        undoButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.menuButtonClicked() }; true }
        redoButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.menuButtonClicked() }; true}
        newGameButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.menuButtonClicked() }; true }
        settingsButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.menuButtonClicked() }; true }

        undoButtonEnabled = true
        redoButtonEnabled = true
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
        timerTimeInSeconds = 0
    }

}