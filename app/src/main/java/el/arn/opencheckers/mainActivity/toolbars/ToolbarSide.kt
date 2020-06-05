package el.arn.opencheckers.mainActivity.toolbars

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import el.arn.opencheckers.ALPHA_FULL
import el.arn.opencheckers.ALPHA_ICON_DISABLED
import el.arn.opencheckers.R
import kotlinx.android.synthetic.main.toolbar_side.view.*

class ToolbarSide(
    layout: LinearLayout
) : Toolbar() {

    private var menuButton: ImageButton = layout.findViewById(R.id.menuButton_sidebar)
    private var undoButton: ImageButton = layout.findViewById(R.id.undoButton_sidebar)
    private var redoButton: ImageButton = layout.findViewById(R.id.redoButton_sidebar)
    private var newGameButton: ImageButton = layout.findViewById(R.id.refreshButton_sidebar)
    private var settingsButton: ImageButton = layout.findViewById(R.id.settingsButton_sidebar)
    private var progressBar: ProgressBar = layout.findViewById(R.id.progressBar_side)
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
        set(value) { progressBar.visibility = if (value) View.VISIBLE else View.INVISIBLE; field = value }
    override var titleText: String = ""
        set(value) { title.text = value; field = value }
    override var timerTimeInSeconds: Int = 0
        set(value) { timer.text = formatSecondsToTimerString(value); field = value }

    init {
        menuButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        undoButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        redoButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        newGameButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }
        settingsButton.setOnClickListener { delegationMgr.notifyAll { it.menuButtonClicked() } }

        undoButtonEnabled = true
        redoButtonEnabled = true
        progressBarVisible = false
        titleText = TitleTextOptions.NO_GAME_LOADED
        timerTimeInSeconds = 0
    }
}