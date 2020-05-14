package el.arn.opencheckers

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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

    var initForLocationDependentOperations_Invoked = false
    fun initForLocationDependentOperations() {
        if (initForLocationDependentOperations_Invoked) { return }
        initForLocationDependentOperations_Invoked = true

        redoButtonFab.y = undoButtonFab.y + (undoButtonFab.height - redoButtonFab.height)/2
        redoButtonFab.elevation = undoButtonFab.elevation - 1


        initSideProgressBar()
        updateHistoryButtons(false)

        if (boardView.gameData != null ) {
            isNewGame = true
        }

    }

    private fun isCurrentPlayerPlayable(): Boolean {
        return (boardView.gameData?.game?.currentPlayer == boardView.gameData?.player1)
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


    var isNewGame = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        initForLocationDependentOperations()

        if (isNewGame) {
            isNewGame = false
            boardView = BoardView(this, boardLayout, Application.gameData, Application.boardConfig.boardSize,  this)
            boardView.initGamePieces()
            updatePlayer1CaptureBox(boardView.gameData!!.player1CapturedPieces)
            updatePlayer2CaptureBox(boardView.gameData!!.player2CapturedPieces)
            if (isCurrentPlayerPlayable()) {
                boardView.enableSelection()
            }
        }

        Application.settingThatRequiresANewGameManager.showDialogIfTriggered(this)
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

    private lateinit var boardView: BoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //todo save practice?
        setContentView(R.layout.activity_main)
        findViews()
        setSupportActionBar(titleBar)

        initToolbar()
        initButtons()

        setProgressBarsVisibilityByVirtualPlayerCurrentState()
        Application.virtualPlayer?.delegate = virtualPlayerDelegate


        boardView = BoardView(this, boardLayout, Application.gameData, Application.boardConfig.boardSize,  this)

        //MakeAMoveByVirtualPlayerAsyncTask.boardView = boardView
        //MakeAMoveByVirtualPlayerAsyncTask.updateProgressBarsFunc = ::setProgressBarsVisibilityByVirtualPlayerCurrentState

    }

    private val virtualPlayerDelegate = object : VirtualPlayer.Delegate {
        override fun virtualPlayerDelegateStateHasChanged() {
            setProgressBarsVisibilityByVirtualPlayerCurrentState()
        }

        override fun choseAMove(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int) {
            boardView.makeAMoveManually(xFrom, yFrom, xTo, yTo)
        }
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


    private fun turnButtonsIntoSingleSelection(buttons: Set<View>, applyToSelected: (View) -> Unit, applyToUnselected: (View) -> Unit): SingleSelectionApplier {
        val selectionApplier = SingleSelectionApplier()
        for (button in buttons) {
            button.setOnClickListener{ btn ->
                selectionApplier.select(
                    btn,
                    applyToSelected,
                    applyToUnselected
                )
            }
        }
        return selectionApplier
    }

    fun getRandomPlayer(): Player {
        return if (Math.random() > 0.5) Player.White else Player.Black
    }

    enum class GameType { SinglePlayer, Multiplayer }

    fun showNewGameDialog(): Unit {
        val dialogContentLayout = layoutInflater.inflate(R.layout.dialog_new_game, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Delete entry")
            .setView(dialogContentLayout)
//            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.
            .setNegativeButton("Cancel", null)

        val statingPlayer = SingleSelectionButtonSet(
            arrayOf(
                R.id.newGameDialog_SelectPlayerButton_WhitePlayer,
                R.id.newGameDialog_SelectPlayerButton_BlackPlayer,
                R.id.newGameDialog_SelectPlayerButton_Random
            ),
            arrayOf(
                Player.White,
                Player.Black,
                getRandomPlayer()
            ),
            0,
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonSelected) },
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonNotSelected) },
            resources.getString(R.string.pref_starting_player),
            dialogContentLayout,
            applicationContext

        )

        builder.setPositiveButton("Start Game!"
        ) { dialog, which ->
            Application.instance.createANewSinglePlayerGame(statingPlayer.selectedValue, Difficulty.Easy, virtualPlayerDelegate)
            boardView = BoardView(this, boardLayout, Application.gameData, Application.boardConfig.boardSize,  this)
            isNewGame = true
        } // A null listener allows the button to dismiss the dialog and take no further action.


        val spinner = dialogContentLayout.findViewById<Spinner>(R.id.spinner)

        val gametype = SingleSelectionButtonSet(
            arrayOf(
                R.id.newGameDialog_GameType_singlePlayer,
                R.id.newGameDialog_GameType_twoPlayers
            ),
            arrayOf(
                GameType.SinglePlayer,
                GameType.Multiplayer
            ),
            1,
            {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonSelected)
                spinner.isEnabled = it.id == R.id.newGameDialog_GameType_singlePlayer
            },
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonNotSelected) },
            resources.getString(R.string.pref_game_type),
            dialogContentLayout,
            this
        )

        builder.show()
    }

    var player1capturedPieces: List<Piece> = listOf()
    var player2capturedPieces: List<Piece> = listOf()

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

        if (piecesStackMaxLength > captureBoxLength) {
            if (applyVertically) {
                captureBox.layoutParams.width = pieceSizeCorrector.getInt()
            } else {
                captureBox.layoutParams.height = pieceSizeCorrector.getInt()
            }
        }

        captureBox.invalidate()
        captureBox.requestLayout()
    }


    //TODo the mimpap doesnt take a possibility of passing a turn

    private fun setProgressBarsVisibilityByVirtualPlayerCurrentState() {
        val visibility = if (Application.isVirtualPlayerCalculatingMove) View.VISIBLE else View.INVISIBLE
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
        if (currentPlayer != null && currentPlayer == boardView.gameData?.player1) {
            boardView.saveSnapshotToHistory()
            boardView.enableSelection()
            updateHistoryButtons(true)
        } else if (currentPlayer == boardView.gameData?.player2) {
            Application.virtualPlayer!!.chooseAMove()
        } else if (currentPlayer == null) {
            boardView.saveSnapshotToHistory()
            boardView.enableSelection()
            updateHistoryButtons(true)
            updateWinnerMessage()
        }
    }

    private fun updateWinnerMessage() {
        val winner = boardView.gameData!!.game.winner
        if (winner != null) {
            val winnerMessage: ImageView = findViewById(R.id.winnerMessage)
            winnerMessage.animate().alpha(1f).setDuration(300)
        } else {
            val winnerMessage: ImageView = findViewById(R.id.winnerMessage)
            winnerMessage.animate().alpha(0f).setDuration(0)
        }
    }

    override fun boardDelegateSnapshotWasLoadedFromHistory(player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
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

class SingleSelectionButtonSet<T>(
    private val buttonsId: Array<Int>,
    private val values: Array<T>,
    private val defaultValueIndex: Int,
    private val applyToSelectedButton: (View) -> Unit,
    private val applyToUnselectedButton: (View) -> Unit,
    private val prefKey: String,
    context: View,
    private val applicationContext: Context
) {

    var selectedValue: T
        private set

    private val sharedPref = applicationContext.getSharedPreferences(
        applicationContext.resources.getString(R.string.prefFile_settings), Context.MODE_PRIVATE)

    private var selected: View? = null

    private fun getIndexFromPref(): Int {
        return sharedPref.getInt(prefKey, defaultValueIndex)
    }

    private fun writeIndexToPref(index: Int) {
        with (sharedPref.edit()) {
            putInt(prefKey, index)
            apply()
        }
    }


    init {
        selectedValue = values[getIndexFromPref()]
        if (buttonsId.size != values.size) { throw InternalError() }
        for (buttonId in buttonsId) {
            context.findViewById<View>(buttonId).setOnClickListener{ select(it) }
        }
        val defaultButtonId = buttonsId[getIndexFromPref()]
        select(context.findViewById(defaultButtonId))
    }

    private fun select(button: View) {
        this.selected?.let { applyToUnselectedButton(it) }
        applyToSelectedButton(button)
        this.selected = button
        val index = buttonsId.indexOf(button.id)
        selectedValue = values[index]
        writeIndexToPref(index)
    }
}

