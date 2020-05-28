package el.arn.opencheckers

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player


const val ALPHA_FULL = 255
const val ALPHA_ICON_DISABLED = 130

class MainActivity : AppCompatActivity(), BoardView.Delegate {

    var counter = 0;



    lateinit var prefs: SharedPreferences

    lateinit var drawer: DrawerLayout

    lateinit var titleBar: Toolbar
    lateinit var titleBarMenu: Menu
    lateinit var menuButtonTop: ImageButton
    lateinit var undoButtonTop: MenuItem
    lateinit var redoButtonTop: MenuItem
    lateinit var refreshButtonTop: MenuItem
    lateinit var settingsButtonTop: MenuItem
    lateinit var progressBarTop: ProgressBar



    lateinit var sideBar: LinearLayout
    lateinit var menuButtonSide: ImageButton
    lateinit var undoButtonSide: ImageButton
    lateinit var redoButtonSide: ImageButton
    lateinit var refreshButtonSide: ImageButton
    lateinit var settingsButtonSide: ImageButton
    lateinit var progressBarSide: ProgressBar

    lateinit var undoButtonFab: FloatingActionButton
    lateinit var redoButtonFab: FloatingActionButton

    lateinit var captureBoxTop: LinearLayout
    lateinit var captureBoxBottom: LinearLayout
    lateinit var captureBoxStart: LinearLayout
    lateinit var captureBoxEnd: LinearLayout

    lateinit var boardLayout: FrameLayout




    private val isDirectionRTL
        get() =  resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    private val isLandscapeMode: Boolean
        get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE //TODO also put screen size


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //if (findViewById<Toolbar>(R.id.toolbar_top) != null) {
        menuInflater.inflate(R.menu.toolbar_top, menu)
        this.titleBarMenu = menu

        undoButtonTop =  menu.findItem(R.id.undo_menu_item)
        redoButtonTop = menu.findItem(R.id.redo_menu_item)
        refreshButtonTop =  menu.findItem(R.id.refresh_menu_item)
        settingsButtonTop =  menu.findItem(R.id.settings_menu_item)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo_menu_item -> boardView.undo()
            R.id.redo_menu_item -> boardView.redo()
            R.id.refresh_menu_item -> newGameClicked()
            R.id.settings_menu_item -> settingsClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isCurrentPlayerPlayable(): Boolean {
        return (boardView.currentPlayer == boardView.player1)
    }

    fun updateHistoryButtons(animateFab: Boolean) {
        val redoButtonCurrentX =
            if (boardView.hasRedo) {
                if (isDirectionRTL) {
                    undoButtonFab.x - redoButtonFab.width - resources.getDimension(R.dimen.fab_spacing)
                } else {
                    undoButtonFab.x + undoButtonFab.width + resources.getDimension(R.dimen.fab_spacing)
                }
            } else {
                undoButtonFab.x + (undoButtonFab.width - redoButtonFab.width) / 2
            }
        if (animateFab) {
            redoButtonFab.animate().x(redoButtonCurrentX)
                .setInterpolator(FastOutSlowInInterpolator()).setDuration(200)
        } else {
            redoButtonFab.x = redoButtonCurrentX
        }


        val isUndoEnabled = (boardView.hasUndo)
        undoButtonTop.icon.alpha = if (isUndoEnabled) ALPHA_FULL else ALPHA_ICON_DISABLED//this effects all instances of this icon
        undoButtonTop.isEnabled = isUndoEnabled
        undoButtonSide.isEnabled = isUndoEnabled
        undoButtonFab.isEnabled = isUndoEnabled

        val isRedoEnabled = (boardView.hasRedo)
        redoButtonTop.icon.alpha = if (isRedoEnabled) ALPHA_FULL else ALPHA_ICON_DISABLED//this effects all instances of this icon
        redoButtonTop.isEnabled = isRedoEnabled
        redoButtonSide.isEnabled = isRedoEnabled
        redoButtonFab.isEnabled = isRedoEnabled

        redoButtonFab.elevation = undoButtonFab.elevation - 1

    }

    private fun initFabRedoButtonRestingPosition() {
        redoButtonFab.y = undoButtonFab.y + (undoButtonFab.height - redoButtonFab.height) / 2
        redoButtonFab.elevation = undoButtonFab.elevation - 1
    }

    private val initLocationRelatedElements = GatedFunction {
        initFabRedoButtonRestingPosition()
        initSideProgressBar()
        updateHistoryButtons(false)
    }


    private val initLocationRelatedElementsForNewGame = GatedFunction {
        boardView.initGamePieces()
        initCaptureBoxes()
        updateHistoryButtons(false)
        updateWinnerMessage()

        val virtualPlayer = boardView.currentPlayer?.let { getVirtualPlayerIfCurrentPlayerIsVirtual(it) }
        if (virtualPlayer == null) {
            boardView.enableUserInteraction()
        } else {
            virtualPlayer.calculateNextMove()
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        initLocationRelatedElements.invokeIfOpen()
        initLocationRelatedElementsForNewGame.invokeIfOpen()

        App.settingsThatRequiresANewGame.showDialogIfTriggered(this)
    }

    private fun findViews() {
        undoButtonFab = findViewById(R.id.undoButton_FAB)
        redoButtonFab = findViewById(R.id.redoButton_FAB)
        menuButtonSide = findViewById(R.id.menuButton_sidebar)
        menuButtonTop = findViewById(R.id.menuButton_titlebar)
        drawer = findViewById(R.id.drawerLayout)
        sideBar = findViewById(R.id.toolbar_side)
        progressBarTop = findViewById(R.id.progressBarTop)
        progressBarSide = findViewById(R.id.progressBar_side)
        captureBoxTop = findViewById(R.id.captureBoxTop)
        captureBoxBottom = findViewById(R.id.captureBoxBottom)
        captureBoxStart = findViewById(R.id.captureBoxStart)
        captureBoxEnd = findViewById(R.id.captureBoxEnd)

        titleBar = findViewById(R.id.toolbar_top)
        sideBar = findViewById(R.id.toolbar_side)
        undoButtonSide = findViewById(R.id.undoButton_sidebar)
        redoButtonSide = findViewById(R.id.redoButton_sidebar)
        refreshButtonSide = findViewById(R.id.refreshButton_sidebar)
        settingsButtonSide = findViewById(R.id.settingsButton_sidebar)

        boardLayout = findViewById(R.id.boardLayout)

    }

    private var boardViewHasInitialized = false
    private lateinit var boardView: BoardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //todo save practice?
        setContentView(R.layout.activity_main)
        findViews()
        setSupportActionBar(titleBar)

        initToolbar()
        initButtons()

        initLocationRelatedElements.giveOneAccess()

        buildNewBoard()
    }

    private fun buildNewBoard() {
        createNewBoardView()
        setVirtualPlayers()
        updateVirtualPlayerProgressBar()

        if (boardView.hasAGame) {
            initLocationRelatedElementsForNewGame.giveOneAccess()
        }
    }

    private fun createNewBoardView() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val boardSizeInPx = displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels)

        if (boardViewHasInitialized) {
            boardView.delegate = null
        }
        boardView = BoardView(
            boardLayout = boardLayout,
            gameData = App.gameData,
            tilesInBoard = App.tilesInBoard,
            boardSizeInPx = boardSizeInPx,
            delegate = this)
        boardViewHasInitialized = true
    }


    private var virtualPlayerBlack: VirtualPlayer? = null
    private var virtualPlayerWhite: VirtualPlayer? = null

    private fun setVirtualPlayers() {
        virtualPlayerBlack?.delegate = null
        virtualPlayerWhite?.delegate = null
        virtualPlayerBlack = App.virtualPlayerBlack
        virtualPlayerWhite = App.virtualPlayerWhite

        val delegate = object : VirtualPlayer.Delegate {
            override fun virtualPlayerDelegateStateHasChanged() {
                updateVirtualPlayerProgressBar()
            }

            override fun choseAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int) {
                boardView.makeAMoveManually(xFrom, yFrom, xTo, yTo)
            }
        }
        virtualPlayerBlack?.delegate = delegate
        virtualPlayerWhite?.delegate = delegate

    }

    fun initToolbar() {
        if (isLandscapeMode) {
            titleBar.visibility = View.GONE
            sideBar.visibility = View.VISIBLE
        } else {
            titleBar.visibility = View.VISIBLE
            sideBar.visibility = View.GONE
        }
    }

    fun initSideProgressBar() {
        //TOdo it has a little dent on top. fix it?
        val h = progressBarSide.height.toFloat()
        progressBarSide.scaleX = sideBar.height.toFloat() / progressBarSide.width
        progressBarSide.pivotX = h
        progressBarSide.pivotY = 0f
        progressBarSide.rotation = 90f
        progressBarSide.x = if (isDirectionRTL) sideBar.x + sideBar.width else sideBar.x - h
        progressBarSide.y += h

        progressBarSide.invalidate()
        progressBarSide.requestLayout()
    }

    fun initButtons() {
        menuButtonTop.setOnClickListener { openSideDrawer() }

        menuButtonSide.setOnClickListener { openSideDrawer() }
        undoButtonSide.setOnClickListener { boardView.undo() }
        redoButtonSide.setOnClickListener { boardView.redo() }
        refreshButtonSide.setOnClickListener { newGameClicked() }
        settingsButtonSide.setOnClickListener { settingsClicked() }

        undoButtonFab.setOnClickListener { boardView.undo() }
        redoButtonFab.setOnClickListener { boardView.redo() }

    }


    fun showNewGameDialog() {
        NewGameDialog(this) {
            startingPlayer: Player, difficulty: Difficulty ->
            App.instance.startNewSinglePlayerGame(startingPlayer, startingPlayer.opponent(), difficulty)
            buildNewBoard()
        }
    }
    

    //TODO new dialog dont fit on landscape
    fun newGameClicked() {
        showNewGameDialog()
    }
    fun settingsClicked() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openSideDrawer() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    //Todo the dialogs will be dismissed when screen rotates

    var player1capturedPieces: List<Piece> = listOf()
    var player2capturedPieces: List<Piece> = listOf()

    fun initCaptureBoxes() {
        updatePlayer1CaptureBox(boardView.player1CapturedPieces)
        updatePlayer2CaptureBox(boardView.player2CapturedPieces)
    }

    fun updatePlayer1CaptureBox(capturedPieces: List<Piece>) {
        if (player1capturedPieces.size != capturedPieces.size) {
            player1capturedPieces = capturedPieces.toList()
            updateCaptureBox(captureBoxBottom, capturedPieces)
            updateCaptureBox(captureBoxStart, capturedPieces, true)
        }
    }

    fun updatePlayer2CaptureBox(capturedPieces: List<Piece>) {
        if (player2capturedPieces.size != capturedPieces.size) {
            player2capturedPieces = capturedPieces.toList()
            updateCaptureBox(captureBoxTop, capturedPieces.asReversed())
            updateCaptureBox(captureBoxEnd, capturedPieces.asReversed(), true)
        }
    }

    fun updateCaptureBox(captureBox: LinearLayout, capturedPieces: List<Piece>, applyVertically: Boolean = false) {
        captureBox.removeAllViews();

        val pieceMaxSize = resources.getDimensionPixelOffset(R.dimen.capturedPieceMaxSize)
        val pieceMaxPadding = resources.getDimensionPixelOffset(R.dimen.capturedPieceMaxPadding)
        val captureBoxLength = if (applyVertically) captureBox.height else captureBox.width
        val piecesStackMaxLength = capturedPieces.size * (pieceMaxSize + pieceMaxPadding)

        val pieceSizeCorrector = DoubleToIntCorrector(pieceMaxSize * captureBoxLength.toDouble() / piecesStackMaxLength)
        val piecePaddingCorrector = DoubleToIntCorrector(pieceMaxPadding * captureBoxLength.toDouble() / piecesStackMaxLength)


        fun pieceSize():  Int =
            if (piecesStackMaxLength > captureBoxLength)
                pieceSizeCorrector.getInt()
            else
                pieceMaxSize

        fun piecePadding():  Int =
            if (piecesStackMaxLength > captureBoxLength)
                piecePaddingCorrector.getInt()
            else
                pieceMaxPadding



        for (i in 0..capturedPieces.lastIndex) {
            val capturedPiece: View = layoutInflater.inflate(R.layout.element_captured_piece, null)
            capturedPiece.findViewById<ImageView>(R.id.capturedPiece_image)
                .setImageResource(getPieceImageResource(capturedPieces[i]))

            val imageView: ImageView = capturedPiece.findViewById(R.id.capturedPiece_image)
            imageView.layoutParams.width = pieceSize()
            imageView.layoutParams.height = pieceSize()

            if (applyVertically) {
                capturedPiece.setPaddingRelative(0, 0, 0, piecePadding())
            } else {
                capturedPiece.setPaddingRelative(piecePadding(), 0, 0, 0)
            }
            captureBox.addView(capturedPiece)
        }

        if (applyVertically) {
            captureBox.layoutParams.width = pieceSize()
        } else {
            captureBox.layoutParams.height = pieceSize()
        }

        captureBox.invalidate()
        captureBox.requestLayout()
    }


    //TODo the mimpap doesnt take a possibility of passing a turn

    private fun updateVirtualPlayerProgressBar() {
        val visibility =
            if (virtualPlayerBlack?.isCalculating == true
                || virtualPlayerWhite?.isCalculating == true)
                View.VISIBLE else View.INVISIBLE

        progressBarTop.visibility = visibility
        progressBarSide.visibility = visibility

        if (isLandscapeMode) {
            progressBarTop.visibility = View.INVISIBLE
            progressBarSide.visibility = visibility
        } else {
            progressBarTop.visibility = visibility
            progressBarSide.visibility = View.INVISIBLE
        }
    }

    override fun boardDelegateMadeAMove() {
        updateHistoryButtons(true)
    }

    override fun boardDelegateFinishedTurn(currentPlayer: Player?, player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
        updatePlayer1CaptureBox(player1CapturedPieces)
        updatePlayer2CaptureBox(player2CapturedPieces)

        if (currentPlayer == null) {
            boardView.saveSnapshotToHistory()
            boardView.enableUserInteraction()
            updateHistoryButtons(true)
            updateWinnerMessage()
        } else {
            val virtualPlayer = getVirtualPlayerIfCurrentPlayerIsVirtual(currentPlayer)
            if (virtualPlayer == null) {
                boardView.saveSnapshotToHistory()
                boardView.enableUserInteraction()
                updateHistoryButtons(true)
            } else {
                virtualPlayer.calculateNextMove()
            }        }
    }

    private fun getVirtualPlayerIfCurrentPlayerIsVirtual(currentPlayer: Player): VirtualPlayer? {
        return when (currentPlayer) {
            virtualPlayerBlack?.player -> virtualPlayerBlack
            virtualPlayerWhite?.player -> virtualPlayerWhite
            else -> null
        }
    }

    override fun boardDelegateBoardClickedWhenGameIsNotRunning() { //TODO it's written bad
        if (boardView.boardGameState != BoardView.BoardGameState.GameIsOn) {
            showNewGameDialog()
        }
    }

    override fun boardDelegateGameHasFinished() {
    }

    private fun updateWinnerMessage() {
        if (boardView.winner != null) {
            val winnerMessage: ImageView = findViewById(R.id.winnerMessage)
            winnerMessage.animate().alpha(1f).setDuration(300)
        } else {
            val winnerMessage: ImageView = findViewById(R.id.winnerMessage)
            winnerMessage.animate().alpha(0f).setDuration(0)
        }
    }

    override fun boardDelegateSnapshotLoadedFromHistory(player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
        updatePlayer1CaptureBox(player1CapturedPieces)
        updatePlayer2CaptureBox(player2CapturedPieces)
        updateHistoryButtons(true)
        updateWinnerMessage()
    }


    fun getToast(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        )
            .show()
    }
}

class DoubleToIntCorrector(private val const: Double) {
    private var remainder: Double = 0.0
    fun getInt(): Int {
        remainder += const
        val result = remainder.toInt()
        remainder -= result.toDouble()
        return result
    }
}


class SingleSelectionApplier() {
    var selected: View? = null
        private set
    fun select(selected: View, applyToSelected: (View) -> Unit, applyToUnselected: (View) -> Unit) {
        this.selected?.let { applyToUnselected(it) }
        applyToSelected(selected)
        this.selected = selected
    }
}

enum class GameType { SinglePlayer, Multiplayer }
