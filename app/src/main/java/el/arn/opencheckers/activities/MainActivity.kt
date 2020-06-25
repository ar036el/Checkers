package el.arn.opencheckers.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import el.arn.opencheckers.*
import el.arn.opencheckers.complementaries.LimitedAccessFunction
import el.arn.opencheckers.complementaries.android.Orientations
import el.arn.opencheckers.complementaries.android.isDirectionRTL
import el.arn.opencheckers.complementaries.android.orientation
import el.arn.opencheckers.dialogs.NewGameDialog
import el.arn.opencheckers.game.UndoRedoDataBridgeGateA
import el.arn.opencheckers.widgets.main_activity.toolbar.AbstractToolbar
import el.arn.opencheckers.widgets.main_activity.toolbar.ToolbarSide
import el.arn.opencheckers.widgets.main_activity.UndoRedoFloatingActionButtonGroup
import el.arn.opencheckers.widgets.main_activity.WinnerMessage
import el.arn.opencheckers.widgets.main_activity.main_board.PiecesManager
import el.arn.opencheckers.widgets.main_activity.main_board.PiecesManager_impl
import el.arn.opencheckers.widgets.main_activity.main_board.TilesManager
import el.arn.opencheckers.widgets.main_activity.main_board.TilesManager_impl
import el.arn.opencheckers.widgets.main_activity.toolbar.ToolbarTop


class MainActivity : AppCompatActivity() {

    lateinit var sideDrawerLayout: DrawerLayout

    var toolbar: AbstractToolbar? = null

    lateinit var undoRedoFabGroup: UndoRedoFloatingActionButtonGroup


    lateinit var piecesManager: PiecesManager
    lateinit var tilesManager: TilesManager

    lateinit var winnerMessage: WinnerMessage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_top))

        initToolbar()
        initBoardComponents()
        initUndoRedoFabGroup.grantOneAccess()
    }
    
    private fun initBoardComponents() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val boardSizeInPx = displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels)
        findViewById<ImageView>(R.id.boardBackground).layoutParams = FrameLayout.LayoutParams(boardSizeInPx, boardSizeInPx)
        findViewById<FrameLayout>(R.id.boardPiecesContainer).layoutParams = FrameLayout.LayoutParams(boardSizeInPx, boardSizeInPx)
        findViewById<GridLayout>(R.id.boardTilesContainer).layoutParams = FrameLayout.LayoutParams(boardSizeInPx, boardSizeInPx)

        sideDrawerLayout = findViewById(R.id.drawerLayout)

        tilesManager = TilesManager_impl(
            findViewById(R.id.boardTilesContainer),
            boardSizeInPx.toFloat(),
            appRoot.gamePreferencesManager.boardSize.value,
            true
        )

        initPiecesManager.grantOneAccess()
    }
    
    private val initPiecesManager = LimitedAccessFunction({
        piecesManager = PiecesManager_impl(
            findViewById(R.id.boardPiecesContainer),
            appRoot.gamePreferencesManager.boardSize.value,
            findViewById<FrameLayout>(R.id.boardPiecesContainer).x,
            tilesManager
        )
    })

    private val initUndoRedoFabGroup = LimitedAccessFunction({
        undoRedoFabGroup =
            UndoRedoFloatingActionButtonGroup(
                appRoot.undoRedoDataBridge as UndoRedoDataBridgeGateA,
                findViewById(R.id.undoButton_FAB),
                findViewById(R.id.redoButton_FAB),
                this,
                isDirectionRTL
            )
    })
    
    private val initToolbarSide = LimitedAccessFunction({
        if (orientation == Orientations.Landscape) {
            if (toolbar != null) { throw InternalError() }
            val toolbar = ToolbarSide(findViewById(R.id.toolbar_side), this)
            toolbar.addListener(toolbarListener)
            this.toolbar = toolbar
        }
    })

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        //all needs location-related variables that become available only here
        initUndoRedoFabGroup.invokeIfHasAccess()
        initPiecesManager.invokeIfHasAccess()
        initToolbarSide.invokeIfHasAccess()
    }

    private fun initToolbar() {
        val toolbarLayoutTop: Toolbar = findViewById(R.id.toolbar_top)
        val toolbarLayoutSide: LinearLayout = findViewById(R.id.toolbar_side)
        val progressBarTop: ProgressBar = findViewById(R.id.progressBarTop)
        val progressBarSide: ProgressBar = findViewById(R.id.progressBar_side)

        if (orientation == Orientations.Portrait) {
            toolbarLayoutSide.visibility = View.GONE
            progressBarSide.visibility = View.GONE

            //continues in [onCreateOptionsMenu]
        } else {
            toolbarLayoutTop.visibility = View.GONE
            progressBarTop.visibility = View.GONE
            initToolbarSide.grantOneAccess()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (orientation == Orientations.Portrait) {
            if (toolbar != null) { throw InternalError() }
            val toolbar = ToolbarTop(findViewById(R.id.toolbar_top), this, menu, menuInflater)
            toolbar.addListener(toolbarListener)
            this.toolbar = toolbar
        }
        return true
    }

    private val toolbarListener = object: AbstractToolbar.Listener {
        override fun menuButtonClicked() {
            openSideDrawer()
        }

        override fun undoButtonClicked() {
            TODO("Not yet implemented")
        }

        override fun redoButtonClicked() {
            TODO("Not yet implemented")
        }

        override fun newGameButtonClicked() {
            NewGameDialog(this@MainActivity) { _,_,_ -> }
        }

        override fun settingsButtonClicked() {
            TODO("Not yet implemented")
        }

    }

    private fun openSideDrawer() {
        if (!sideDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            sideDrawerLayout.openDrawer(GravityCompat.START)
        }
    }


}