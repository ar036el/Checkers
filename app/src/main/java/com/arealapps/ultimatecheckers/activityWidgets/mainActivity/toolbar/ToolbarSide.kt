package com.arealapps.ultimatecheckers.activityWidgets.mainActivity.toolbar

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.arealapps.ultimatecheckers.R
import com.arealapps.ultimatecheckers.appRoot
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_ENABLED
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_DISABLED
import com.arealapps.ultimatecheckers.helpers.android.isDirectionRTL

class ToolbarSide(
    private val layout: LinearLayout,
    private val activity: Activity
) : ToolbarAbstract() {

    private var menuButton: ImageButton = layout.findViewById(R.id.menuButton_sidebar)
    private var undoButton: ImageButton = layout.findViewById(R.id.undoButton_sidebar)
    private var redoButton: ImageButton = layout.findViewById(R.id.redoButton_sidebar)
    private var newGameButton: ImageButton = layout.findViewById(R.id.refreshButton_sidebar)
    private var settingsButton: ImageButton = layout.findViewById(R.id.settingsButton_sidebar)
    private var progressBar: ProgressBar = activity.findViewById(R.id.progressBar_side)
    private var title: TextView = layout.findViewById(R.id.titleSidebar)
    private var timer: TextView = layout.findViewById(R.id.timerSidebar)


    override var undoButtonEnabled: Boolean = true
        set(value) {
            undoButton.isEnabled = value
            undoButton.imageAlpha = if (value) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED
            field = value }
    override var redoButtonEnabled: Boolean = true
        set(value) {
            redoButton.isEnabled = value
            redoButton.imageAlpha = if (value) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED
            field = value }
    override var progressBarVisible: Boolean = true
        set(value) {
            progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { title.text = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) { timer.text = timeInSecondsToTimerTime(value); field = value }


    private fun initSideProgressBar() {
        rotateProgressBarAndAlignToEnd()
        progressBar.invalidate() //todo doesn't it need to put this everywhere??
        progressBar.requestLayout()
    }

    private fun rotateProgressBarAndAlignToEnd() {
        val h = progressBar.height.toFloat()
        progressBar.scaleX = layout.height.toFloat() / progressBar.width
        progressBar.pivotX = h
        progressBar.pivotY = 0f
        progressBar.rotation = 90f
        progressBar.x = if (activity.isDirectionRTL) layout.x + layout.width else layout.x - h
        progressBar.y += h
    }

    private fun initButtonsClickListeners() {
        menuButton.setOnClickListener { listenersMgr.notifyAll { it.menuButtonWasClicked() } }
        undoButton.setOnClickListener { listenersMgr.notifyAll { it.undoButtonWasClicked() } }
        redoButton.setOnClickListener { listenersMgr.notifyAll { it.redoButtonWasClicked() } }
        newGameButton.setOnClickListener { listenersMgr.notifyAll { it.newGameButtonWasClicked() } }
        settingsButton.setOnClickListener { listenersMgr.notifyAll { it.settingsButtonWasClicked() } }

    }

    private fun initComponentsStates() {
        undoButtonEnabled = appRoot.undoRedoDataBridgeSideB.canUndo
        redoButtonEnabled = appRoot.undoRedoDataBridgeSideB.canRedo
        timerTimeInSeconds = appRoot.timer.timeInSeconds
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
    }

    init {
        initSideProgressBar()
        initButtonsClickListeners()
        initComponentsStates()
    }
}