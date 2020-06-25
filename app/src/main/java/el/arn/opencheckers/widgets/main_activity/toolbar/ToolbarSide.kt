package el.arn.opencheckers.widgets.main_activity.toolbar

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.android.ALPHA_FULL
import el.arn.opencheckers.complementaries.android.ALPHA_ICON_DISABLED
import el.arn.opencheckers.complementaries.android.isDirectionRTL
import el.arn.opencheckers.widgets.main_activity.toolbar.AbstractToolbar

class ToolbarSide(
    private val layout: LinearLayout,
    private val activity: Activity
) : AbstractToolbar() {

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
            undoButton.imageAlpha = if (value) ALPHA_FULL else ALPHA_ICON_DISABLED
            field = value }
    override var redoButtonEnabled: Boolean = true
        set(value) {
            redoButton.isEnabled = value
            redoButton.imageAlpha = if (value) ALPHA_FULL else ALPHA_ICON_DISABLED
            field = value }
    override var progressBarVisible: Boolean = true
        set(value) {
            progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { title.text = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) { timer.text = formatSecondsToTimerString(value); field = value }

    init {
        initSideProgressBar()

        menuButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        undoButton.setOnClickListener { delegationMgr.notifyAll { it.undoButtonClicked() } }
        redoButton.setOnClickListener { delegationMgr.notifyAll { it.redoButtonClicked() } }
        newGameButton.setOnClickListener { delegationMgr.notifyAll { it.newGameButtonClicked() } }
        settingsButton.setOnClickListener { delegationMgr.notifyAll { it.settingsButtonClicked() } }

        undoButtonEnabled = true
        redoButtonEnabled = true
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
        timerTimeInSeconds = 0
    }

    private fun initSideProgressBar() {
        //TOdo it has a little dent on top. fix it?
        val h = progressBar.height.toFloat()
        progressBar.scaleX = layout.height.toFloat() / progressBar.width
        progressBar.pivotX = h
        progressBar.pivotY = 0f
        progressBar.rotation = 90f
        progressBar.x = if (activity.isDirectionRTL) layout.x + layout.width else layout.x - h
        progressBar.y += h

        progressBar.invalidate()
        progressBar.requestLayout()
    }
}