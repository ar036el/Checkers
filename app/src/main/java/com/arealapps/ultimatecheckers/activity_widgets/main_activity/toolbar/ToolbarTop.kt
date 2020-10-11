package com.arealapps.ultimatecheckers.activity_widgets.main_activity.toolbar

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.arealapps.ultimatecheckers.R
import com.arealapps.ultimatecheckers.appRoot
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_ENABLED
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_DISABLED

class ToolbarTop(
    private val toolbarView: Toolbar,
    private val activity: Activity,
    toolbarMenuComponent: Menu,
    menuInflater: MenuInflater
) : ToolbarAbstract() {

    override var undoButtonEnabled: Boolean = true
        set(value) {
            undoButton.isEnabled = value
            undoButton.icon.alpha = if (value) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED
            field = value
        }
    override var redoButtonEnabled: Boolean = true
        set(value) {
            redoButton.isEnabled = value
            redoButton.icon.alpha = if (value) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED
            field = value
        }
    override var progressBarVisible: Boolean = true
        set(value) { progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { title.text = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) {
            timer.text = timeInSecondsToTimerTime(value); field = value
        }

    private var menuButton: ImageButton = toolbarView.findViewById(R.id.menuButton_titlebar)
    private var undoButton: MenuItem
    private var redoButton: MenuItem
    private var newGameButton: MenuItem
    private var settingsButton: MenuItem
    private var progressBar: ProgressBar = activity.findViewById(R.id.progressBarTop)
    private var title: TextView
    private var timer: TextView

    private fun initButtonsClickListeners() {
        menuButton.setOnClickListener { listenersMgr.notifyAll { it.menuButtonWasClicked() } }
        undoButton.setOnMenuItemClickListener { listenersMgr.notifyAll { it.undoButtonWasClicked() }; true }
        redoButton.setOnMenuItemClickListener { listenersMgr.notifyAll { it.redoButtonWasClicked() }; true}
        newGameButton.setOnMenuItemClickListener { listenersMgr.notifyAll { it.newGameButtonWasClicked() }; true }
        settingsButton.setOnMenuItemClickListener { listenersMgr.notifyAll { it.settingsButtonWasClicked() }; true }
    }

    private fun initComponentsStates() {
        undoButtonEnabled = appRoot.undoRedoDataBridgeSideB.canUndo
        redoButtonEnabled = appRoot.undoRedoDataBridgeSideB.canRedo
        timerTimeInSeconds = appRoot.timer.timeInSeconds
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
    }

    init {
        menuInflater.inflate(R.menu.toolbar_top, toolbarMenuComponent)

        undoButton =  toolbarMenuComponent.findItem(R.id.undo_menu_item)
        redoButton = toolbarMenuComponent.findItem(R.id.redo_menu_item)
        newGameButton =  toolbarMenuComponent.findItem(R.id.refresh_menu_item)
        settingsButton =  toolbarMenuComponent.findItem(R.id.settings_menu_item)

        title = toolbarView.findViewById(R.id.title_titlebar)
        timer = toolbarView.findViewById(R.id.timer_titlebar)

        initButtonsClickListeners()
        initComponentsStates()
    }

}