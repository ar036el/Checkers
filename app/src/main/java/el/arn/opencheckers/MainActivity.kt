package el.arn.opencheckers

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
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


class MainActivity : AppCompatActivity() {

    var counter = 0;
    companion object {
        var staticIsRedoEnabled = false
    }
    var McapturePieces = 0
    var drawMode = false



    lateinit var prefs: SharedPreferences

    lateinit var player2CaptureStack: LinearLayout

    lateinit var undoButtonFab: FloatingActionButton
    lateinit var redoButtonFab: FloatingActionButton


    lateinit var drawer: DrawerLayout
    lateinit var titleBar: Toolbar
    var titlebarMenu: Menu? = null
    lateinit var sideBar: LinearLayout


    lateinit var menuButtonSide: ImageButton
    lateinit var progressBarSide: ProgressBar
    lateinit var toolbarSide: LinearLayout

    lateinit var menuButtonTop: ImageButton
    lateinit var progressBarTop: ProgressBar

    lateinit var board: GridLayout
    lateinit var boardCover: GridLayout
    lateinit var boardBackground: ImageView
    lateinit var piecesContainer: FrameLayout



    var windowHeight = -1
    var windowWidth = -1

    var captureStackWidth = -1



    val isDirectionRTL
        get() =  resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    private val isLandscapeMode: Boolean
        get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE //TODO also put screen size

    fun getRedoButtonFabCurrentX(): Float {
        return if (staticIsRedoEnabled) {
            if (isDirectionRTL) {
                undoButtonFab.x - redoButtonFab.width - resources.getDimension(R.dimen.fab_spacing)
            } else {
                undoButtonFab.x + undoButtonFab.width + resources.getDimension(R.dimen.fab_spacing)
            }
        } else {
            undoButtonFab.x + (undoButtonFab.width - redoButtonFab.width) / 2
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //if (findViewById<Toolbar>(R.id.toolbar_top) != null) {
        menuInflater.inflate(R.menu.toolbar_top, menu)
        this.titlebarMenu = menu
        //}
        //undoButtonMenuItem = menu?.findItem(R.id.RedoButton)!!
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo_item -> {
                Toast.makeText(this, "Action clicked", Toast.LENGTH_LONG).show()
                updatePlayer2CaptureStack(McapturePieces++)
            }
            R.id.redo_item -> {
                Toast.makeText(this, "Refereshed", Toast.LENGTH_LONG).show()
                drawMode = !drawMode
            }
            R.id.refresh_item -> {
                showNewGameDialog()
            }
            R.id.settings_item -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    var initWhenLocationValuesAreAvailable_Invoked = false
    fun initWhenLocationValuesAreAvailable() {
        if (initWhenLocationValuesAreAvailable_Invoked) {
            return
        }

        redoButtonFab.x = getRedoButtonFabCurrentX()
        redoButtonFab.y = undoButtonFab.y + (undoButtonFab.height - redoButtonFab.height)/2
        redoButtonFab.elevation = undoButtonFab.elevation - 1

        captureStackWidth = player2CaptureStack.width

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(DisplayMetrics())
        windowHeight = displayMetrics.heightPixels
        windowWidth = displayMetrics.widthPixels

        val remainder = progressBarSide.height.toFloat()
        progressBarSide.layoutParams.width = windowHeight
        progressBarSide.pivotX = remainder
        progressBarSide.pivotY = 0f
        progressBarSide.rotation = 90f
        progressBarSide.x = if (isDirectionRTL) toolbarSide.x + toolbarSide.width else toolbarSide.x - remainder
        progressBarSide.y += remainder

        initWhenLocationValuesAreAvailable_Invoked = true
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        initWhenLocationValuesAreAvailable()
    }

    private fun findViews() {
        undoButtonFab = findViewById(R.id.undoButton_FAB)
        redoButtonFab = findViewById(R.id.redoButton_FAB)
        menuButtonSide = findViewById(R.id.menuButton_sidebar)
        menuButtonTop = findViewById(R.id.menuButton_titlebar)
        drawer = findViewById(R.id.drawerLayout)
        toolbarSide = findViewById(R.id.toolbar_side)
        progressBarTop = findViewById(R.id.progressBarTop)
        progressBarSide = findViewById(R.id.progressBar_side)
        player2CaptureStack = findViewById(R.id.player2CaptureStack)
        titleBar = findViewById(R.id.toolbar_top)
        sideBar = findViewById(R.id.toolbar_side)
        board = findViewById(R.id.board)
        boardCover = findViewById(R.id.boardCover)
        boardBackground = findViewById(R.id.boardBackground)
        piecesContainer = findViewById(R.id.boardPiecesContainer)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) //todo save practice?
        setContentView(R.layout.activity_main)
        findViews()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setSupportActionBar(titleBar)

        initToolbar()
        initButtons()
        initBoard()
    }

    fun initToolbar() {
        if (isLandscapeMode) {
            titleBar.visibility = View.GONE
            sideBar.visibility = View.VISIBLE
        } else {
            titleBar.visibility = View.VISIBLE
            sideBar.visibility = View.GONE
        }
        OpenCheckersApplication.counter++
    }

    fun initButtons() {
        menuButtonTop.setOnClickListener { openSideDrawer() }
        menuButtonSide.setOnClickListener { openSideDrawer() }
        undoButtonFab.setOnClickListener {
            crapToggleRedoButton()
        }
        redoButtonFab.setOnClickListener {
            if (staticIsRedoEnabled) {
                getToast("whoa" + prefs.getString("reply121", "not found"))
            }
        }
    }


    fun initBoard() {
        val tilesInBoard = OpenCheckersApplication.counter;


        //TODo why it's not wotking from windowHeight/windowWidth
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val boardSize = displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels)

        boardBackground.layoutParams = FrameLayout.LayoutParams(boardSize, boardSize)
        piecesContainer.layoutParams = FrameLayout.LayoutParams(boardSize, boardSize)
        board.columnCount = tilesInBoard;
        board.rowCount = tilesInBoard;
        boardCover.columnCount = tilesInBoard;
        boardCover.rowCount = tilesInBoard;


        val tileSize = boardSize.toDouble() / tilesInBoard;

        //crap
        val pieceLayoutCrap: View = layoutInflater.inflate(R.layout.element_piece, null)
        val specialCrapPiece: ImageView = pieceLayoutCrap.findViewById(R.id.piece)
        specialCrapPiece.layoutParams = FrameLayout.LayoutParams(tileSize.toInt(), tileSize.toInt())
        specialCrapPiece.setImageResource(R.drawable.winner_message_white)
        piecesContainer.addView(pieceLayoutCrap)


        var pieceCounter = 0
        fun addTile(color: String, tileLengthInPx: Int) {
            val tileLayout: View = layoutInflater.inflate(R.layout.element_tile, null)

            val tile: ImageView = tileLayout.findViewById(R.id.tile)
            tile.setBackgroundColor(Color.parseColor(color))
            tile.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)

            val tileBottomHighlight: ImageView = tileLayout.findViewById(R.id.tileHighlightBottom)
            tileBottomHighlight.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)

            tileBottomHighlight.setOnClickListener {

                progressBarTop.visibility = if (progressBarTop.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
                progressBarSide.visibility = if (progressBarSide.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
                crapEnableDisable()
                counter++

                if (drawMode) {
                    piecesContainer.setLayoutParams(FrameLayout.LayoutParams(boardSize, boardSize))

                    val piece: View = layoutInflater.inflate(R.layout.element_piece, null)
                    //pieceLayout.setPadding(330, 100, 0, 0)
                    val pieceImage: ImageView = piece.findViewById(R.id.piece)
                    pieceImage.layoutParams = FrameLayout.LayoutParams(tileSize.toInt(), tileSize.toInt())
                    pieceCounter++
                    pieceImage.setImageResource(when {
                        pieceCounter % 4 == 0 -> R.drawable.piece_black_king
                        pieceCounter % 3 == 0 -> R.drawable.piece_black_pawn
                        pieceCounter % 2 == 0 -> R.drawable.piece_red_king
                        else  -> R.drawable.piece_red_pawn
                    })
                    piecesContainer.addView(piece)

                    val loc = intArrayOf(1, 2)
                    it.getLocationInWindow(loc)

                    piece.translationX = loc[0].toFloat()
                    piece.y = loc[1].toFloat()

                    getToast("yo seeing this? ${ loc[0]} ${ loc[1]}")
                } else {
                    it.alpha = 0.7f
                    val loc = intArrayOf(1, 2)
                    it.getLocationInWindow(loc)
                    val boardLoc = intArrayOf(1, 2)
                    boardBackground.getLocationInWindow(boardLoc)
                    specialCrapPiece.animate().translationX((loc[0] - boardLoc[0]).toFloat())
                        .translationY((loc[1] - boardLoc[1]).toFloat()).setDuration(300)
                }
            }

            board.addView(tileLayout)


            val tileTopLayout: View = layoutInflater.inflate(R.layout.element_tile_cover, null)
            val tileTopHighlight: View = tileTopLayout.findViewById(R.id.tileHighlightTop)
            tileTopHighlight.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)
            boardCover.addView(tileTopLayout)
        } //TODo it gets creates it every time it flips. do something with it


        val tileSizeCorrectorForX = DoubleToIntCorrector(tileSize)
        val tileSizeCorrectorForY = DoubleToIntCorrector(tileSize)

        for (x in 1..tilesInBoard) {
            for (y in 1..tilesInBoard) {

                val tileLengthFixed =
                    if (x == 1)
                        tileSizeCorrectorForY.getInt()
                    else if (y == 1)
                        tileSizeCorrectorForX.getInt()
                    else tileSize.toInt()

                val color = if (x % 2 == y % 2) "#dfd23a" else "#a33222"
                addTile(color, tileLengthFixed);
            }
        }



        boardBackground.invalidate() //TODO do it everywhere the width is being changed!! https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        boardBackground.requestLayout()
        piecesContainer.invalidate() //TODO do it everywhere the width is being changed!! https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        piecesContainer.requestLayout()
        boardCover.invalidate() //TOdo neseccary here?
        boardCover.requestLayout()
    }


    fun getToast(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        )
            .show()
    }

    fun openSideDrawer() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    //Todo the dialogs will be dismissed when screen rotates

    fun dpToPx(dp: Int) =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )

    fun crapToggleRedoButton() {
        staticIsRedoEnabled = !staticIsRedoEnabled
        redoButtonFab.animate().x(getRedoButtonFabCurrentX()).setInterpolator(FastOutSlowInInterpolator()).setDuration(200)
        redoButtonFab.elevation = undoButtonFab.elevation - 1
    }

    fun crapEnableDisable() {
        val redoItem: MenuItem? = titlebarMenu?.findItem(R.id.redo_item)
        if (redoItem != null) {
            if (counter%2 == 0) {
                redoItem.isEnabled = true;
                redoItem.icon.alpha = 255;
            } else {
                // disabled
                redoItem.isEnabled = false;
                redoItem.icon.alpha = 130;
            }
        } else {
            Toast.makeText(this, "not found", Toast.LENGTH_LONG).show()

        }
    }


    fun initSingleSelectionButtonSet(buttons: Set<View>, applyToSelected: (View) -> Unit, applyToUnselected: (View) -> Unit) {
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


        initSingleSelectionButtonSet(
            setOf (
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_WhitePlayer),
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_BlackPlayer),
                dialogContentLayout.findViewById(R.id.newGameDialog_SelectPlayerButton_Random)
            ),
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonSelected) },
            { it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttonNotSelected) }
        )


        val spinner = dialogContentLayout.findViewById<Spinner>(R.id.spinner)!!

        initSingleSelectionButtonSet(
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


    fun updatePlayer2CaptureStack(capturedPieces: Int) = updateCaptureStack(player2CaptureStack, capturedPieces)

    fun updateCaptureStack(captureStack: LinearLayout, capturedPieces: Int) {
        captureStack.removeAllViews();

        val capturedPieceMaxSize = resources.getDimensionPixelOffset(R.dimen.capturedPieceMaxSize)
        val capturedPieceMaxPadding = resources.getDimensionPixelOffset(R.dimen.capturedPieceMaxPadding)
        val piecesMaxWidth = capturedPieces * (capturedPieceMaxSize + capturedPieceMaxPadding)
        val pieceSize = DoubleToIntCorrector(capturedPieceMaxSize * captureStackWidth.toDouble() / piecesMaxWidth)
        val piecePadding = DoubleToIntCorrector(capturedPieceMaxPadding * captureStackWidth.toDouble() / piecesMaxWidth)


        for (i in 1..capturedPieces) {
            val capturedPiece: View = layoutInflater.inflate(R.layout.element_captured_piece, null)
            capturedPiece.findViewById<ImageView>(R.id.capturedPiece_image)
                .setImageResource(R.drawable.piece_red_pawn)

            val imageView: ImageView = capturedPiece.findViewById(R.id.capturedPiece_image)
            if (piecesMaxWidth > captureStackWidth) {
                capturedPiece.setPaddingRelative(piecePadding.getInt(), 0, 0, 0)
                imageView.layoutParams.width = pieceSize.getInt()
                imageView.layoutParams.height = pieceSize.getInt()
            }
            captureStack.addView(capturedPiece)
        }
        if (piecesMaxWidth > captureStackWidth) {
            captureStack.layoutParams.height = pieceSize.getInt()
        }
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