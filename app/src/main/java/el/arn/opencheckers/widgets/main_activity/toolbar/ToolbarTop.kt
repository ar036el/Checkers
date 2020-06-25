package el.arn.opencheckers.widgets.main_activity.toolbar

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.android.ALPHA_FULL
import el.arn.opencheckers.complementaries.android.ALPHA_ICON_DISABLED
import el.arn.opencheckers.widgets.main_activity.toolbar.AbstractToolbar

class ToolbarTop(
    private val toolbarView: Toolbar,
    private val activity: Activity,
    toolbarMenuComponent: Menu,
    menuInflater: MenuInflater
) : AbstractToolbar() {

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
        set(value) { progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { toolbarView.title = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) {
            toolbarView.subtitle = formatSecondsToTimerString(value); field = value }

    private var menuButton: ImageButton = toolbarView.findViewById(R.id.menuButton_titlebar)
    private var undoButton: MenuItem
    private var redoButton: MenuItem
    private var newGameButton: MenuItem
    private var settingsButton: MenuItem
    private var progressBar: ProgressBar = activity.findViewById(R.id.progressBarTop)

    init {
        menuInflater.inflate(R.menu.toolbar_top, toolbarMenuComponent)
        undoButton =  toolbarMenuComponent.findItem(R.id.undo_menu_item)
        redoButton = toolbarMenuComponent.findItem(R.id.redo_menu_item)
        newGameButton =  toolbarMenuComponent.findItem(R.id.refresh_menu_item)
        settingsButton =  toolbarMenuComponent.findItem(R.id.settings_menu_item)

        menuButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        undoButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.undoButtonClicked() }; true }
        redoButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.redoButtonClicked() }; true}
        newGameButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.newGameButtonClicked() }; true }
        settingsButton.setOnMenuItemClickListener { delegationMgr.notifyAll { it.settingsButtonClicked() }; true }

        undoButtonEnabled = true
        redoButtonEnabled = true
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
        timerTimeInSeconds = 0
    }

}