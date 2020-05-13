package el.arn.opencheckers

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.AsyncTask
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
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import el.arn.opencheckers.checkers_game.virtual_player.CheckersGameState
import el.arn.opencheckers.checkers_game.virtual_player.CheckersMove


const val ALPHA_ICON_ENABLED = 255
const val ALPHA_ICON_DISABLED = 130

class MainActivity : AppCompatActivity(), Board.Delegate {

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
            R.id.undo_menu_item -> board.undo()
            R.id.redo_menu_item -> board.redo()
            R.id.refresh_menu_item -> newGameClicked()
            R.id.settings_menu_item -> settingsClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    var initForLocationDependentOperations_Invoked = false
    fun initForLocationDependentOperations() {
        if (initForLocationDependentOperations_Invoked) {
            return
        }

        redoButtonFab.y = undoButtonFab.y + (undoButtonFab.height - redoButtonFab.height)/2
        redoButtonFab.elevation = undoButtonFab.elevation - 1


        initSideProgressBar()

        if (board.gameData != null) {
            board.initWhenLocationRelatedValuesAreAvailable()
            board.enableSelection()
            updatePlayer1CaptureBox(board.gameData.player1CapturedPieces)
            updatePlayer2CaptureBox(board.gameData.player2CapturedPieces)
            updateHistoryButtons(false)
        }



        initForLocationDependentOperations_Invoked = true
    }

    fun updateHistoryButtons(animateFab: Boolean) {
        val redoButtonCurrentX =
            if (board.hasRedo) {
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


        val isUndoEnabled = (board.hasUndo == true)
        undoButtonTop.icon.alpha = if (isUndoEnabled) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED//this effects all instances of this icon
        undoButtonTop.isEnabled = isUndoEnabled
        undoButtonSide.isEnabled = isUndoEnabled
        undoButtonFab.isEnabled = isUndoEnabled

        val isRedoEnabled = (board.hasRedo == true)
        redoButtonTop.icon.alpha = if (isRedoEnabled) ALPHA_ICON_ENABLED else ALPHA_ICON_DISABLED//this effects all instances of this icon
        redoButtonTop.isEnabled = isRedoEnabled
        redoButtonSide.isEnabled = isRedoEnabled
        redoButtonFab.isEnabled = isRedoEnabled

        redoButtonFab.elevation = undoButtonFab.elevation - 1

    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        initForLocationDependentOperations()
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

    lateinit var board: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //todo save practice?
        setContentView(R.layout.activity_main)
        findViews()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setSupportActionBar(titleBar)

        initToolbar()
        initButtons()

        board = Board(this, boardLayout, Application.gameData!!, this)


        progressBarSide.visibility = View.INVISIBLE
        progressBarTop.visibility = View.INVISIBLE

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
        undoButtonSide.setOnClickListener { board.undo() }
        redoButtonSide.setOnClickListener { board.redo() }
        refreshButtonSide.setOnClickListener { newGameClicked() }
        settingsButtonSide.setOnClickListener { settingsClicked() }

        undoButtonFab.setOnClickListener { board.undo() }
        redoButtonFab.setOnClickListener { board.redo() }

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


    private fun turnButtonsIntoSingleSelection(buttons: Set<View>, applyToSelected: (View) -> Unit, applyToUnselected: (View) -> Unit) {
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
    }


    private fun showNewGameDialog(): Unit {
        val dialogContentLayout = layoutInflater.inflate(R.layout.dialog_new_game, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Delete entry")
            .setView(dialogContentLayout)
//            .setMessage("Are you sure you want to delete this entry?") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Start Game!",
                { dialog, which ->
                    // Continue with delete operation
                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
        //.setIcon(android.R.drawable.ic_dialog_alert)


        turnButtonsIntoSingleSelection(
            setOf (
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_WhitePlayer),
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_BlackPlayer),
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_Random)
            ),
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonSelected) },
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonNotSelected) }
        )


        val spinner = dialogContentLayout.findViewById<Spinner>(R.id.spinner)

        turnButtonsIntoSingleSelection(
            setOf (
                dialogContentLayout.findViewById(R.id.newGameDialog_GameType_singlePlayer),
                dialogContentLayout.findViewById(R.id.newGameDialog_GameType_twoPlayers)
            ),
            {
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonSelected)
                spinner.isEnabled = it.id == R.id.newGameDialog_GameType_singlePlayer
            },
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonNotSelected) })


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

    fun showProgressBar() {
        progressBarTop.visibility = View.VISIBLE
        progressBarSide.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBarTop.visibility = View.GONE
        progressBarSide.visibility = View.GONE
    }


    class MakeAMoveByVirtualPlayer( //Todo cancel when activity is destoryes
        private val board: Board,
        private val showProgressBar: () -> Unit,
        private val hideProgressBar: () -> Unit
    ) : AsyncTask<Unit, Unit, CheckersMove>() {

        override fun onPreExecute() = showProgressBar()

        override fun doInBackground(vararg params: Unit): CheckersMove? =
            Application.virtualPlayer.getMove(CheckersGameState(board.gameData.game, Player.Black), 3)


        override fun onPostExecute(result: CheckersMove?) {
            hideProgressBar()

            if (result == null) {
                if  (board.gameData.game.winner != Player.White) { throw InternalError() }
                return
            }

            val from = result.fromTile
            val to = result.move.to
            board.makeAMoveManually(from.x, from.y, to.x, to.y)
        }
    }

    override fun boardDelegateMadeAMove() {
        updateHistoryButtons(true)
    }

    override fun boardDelegateFinishedTurn(currentPlayer: Player?, player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
        updatePlayer1CaptureBox(player1CapturedPieces)
        updatePlayer2CaptureBox(player2CapturedPieces)
        if (currentPlayer == Player.White) {
            board.saveSnapshotToHistory()
            board.enableSelection()
            updateHistoryButtons(true)
        } else if (currentPlayer == Player.Black) {
            MakeAMoveByVirtualPlayer(board, ::showProgressBar, ::hideProgressBar).execute()
        }
    }

    override fun boardDelegateSnapshotWasLoadedFromHistory(player1CapturedPieces: List<Piece>, player2CapturedPieces: List<Piece>) {
        updatePlayer1CaptureBox(player1CapturedPieces)
        updatePlayer2CaptureBox(player2CapturedPieces)
        updateHistoryButtons(true)
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
    private var lastSelected: View? = null
    fun select(selected: View, applyToSelected: (View) -> Unit, applyToUnselected: (View) -> Unit) {
        lastSelected?.let { applyToUnselected(it) }
        applyToSelected(selected)
        lastSelected = selected
    }
}

