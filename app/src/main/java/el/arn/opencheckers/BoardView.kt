package el.arn.opencheckers

import android.animation.Animator
import android.app.Activity
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import kotlin.math.abs

//TOdo when rotating when virtual player is playing- bug


class BoardView(
    private val activity: Activity,
    boardLayout: FrameLayout,
    val gameData: GameData?,
    tilesInBoard: Int,
    var delegate: Delegate? = null
) {

    private val tiles = Tiles(tilesInBoard)
    private val tileSize: Int

    private val board: GridLayout = boardLayout.findViewById(R.id.board)
    private val boardCover: GridLayout = boardLayout.findViewById(R.id.boardCover)
    private val boardBackground: ImageView = boardLayout.findViewById(R.id.boardBackground)
    private val piecesContainer: FrameLayout = boardLayout.findViewById(R.id.boardPiecesContainer)

    private var isEnabled = false
    
    private var selectedTile: Tile? = null
    private val availableTiles = mutableSetOf<Tile>()
    private val activatedTilesForSelectedTile = mutableSetOf<Tile>()

    init {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val boardSize = displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels)

        boardBackground.layoutParams = FrameLayout.LayoutParams(boardSize, boardSize)
        piecesContainer.layoutParams = FrameLayout.LayoutParams(boardSize, boardSize)
        board.columnCount = tilesInBoard;
        board.rowCount = tilesInBoard;
        boardCover.columnCount = tilesInBoard;
        boardCover.rowCount = tilesInBoard;

        val tileSize = boardSize.toDouble() / tilesInBoard;
        this.tileSize = tileSize.toInt()

        board.removeAllViews()
        boardCover.removeAllViews()
        createTiles(tileSize, tilesInBoard)

        boardBackground.invalidate() //TODO do it everywhere the width is being changed https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        boardBackground.requestLayout()
        piecesContainer.invalidate() //TODO do it everywhere the width is being changed https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        piecesContainer.requestLayout()
        boardCover.invalidate() //TOdo neseccary here?
        boardCover.requestLayout()
    }



    fun initGamePieces() {
        if (gameData == null) { throw IllegalStateException("game was not created yet") }
        putPiecesOnBoard()
    }

    fun enableSelection() {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        isEnabled = true
        updateAvailableTiles()
    }

    val hasUndo
        get() = (gameData?.gameHistory?.hasUndo == true)
    val hasRedo
        get() = (gameData?.gameHistory?.hasRedo == true)

    fun undo() {
        if (!hasUndo || gameData == null) throw IllegalStateException()
        if (isEnabled) {
            loadSnapshotFromHistory(false)
        }
    }

    fun redo() {
        if (!hasRedo || gameData == null) throw IllegalStateException()
        if (isEnabled) {
            loadSnapshotFromHistory(true)
        }
    }

    fun saveSnapshotToHistory() {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        gameData.gameHistory.saveEntry(
            gameData.game,
            gameData.player1CapturedPieces,
            gameData.player2CapturedPieces
        )
    }

    fun makeAMoveManually(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int) {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        val fromTile = tiles.get(xFrom, yFrom)
        val toTile = tiles.get(xTo, yTo)
        makeAMove(fromTile, toTile)
    }

    private fun createTiles(tileSize: Double, tilesInBoard: Int) {
        val tileSizeCorrectorForX = DoubleToIntCorrector(tileSize)
        val tileSizeCorrectorForY = DoubleToIntCorrector(tileSize)
        val boardDirection = if (gameData?.player1 == Player.White) (tilesInBoard - 1 downTo 0) else (0 until tilesInBoard)
        for (y in boardDirection) {
            for (x in boardDirection) {
                val tileLengthFixed =
                    if (x == tilesInBoard - 1)
                        tileSizeCorrectorForX.getInt()
                    else if (y == tilesInBoard - 1)
                        tileSizeCorrectorForY.getInt()
                    else tileSize.toInt()

                val isDarkTile = (x % 2 == y % 2)
                addTile(isDarkTile, tileLengthFixed, x, y);
            }
        }
    }

    private fun addTile(isDarkTile: Boolean, tileLengthInPx: Int, x: Int, y: Int) {
        val tileLayout: View = activity.layoutInflater.inflate(R.layout.element_tile, null)
        val tile: ImageView = tileLayout.findViewById(R.id.tile)
        tile.setBackgroundColor(
            ContextCompat.getColor(
                activity,
                if (isDarkTile) R.color.tile_dark else R.color.tile_light
            )
        )
        tile.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)
        val tileHighlightBottom: ImageView = tileLayout.findViewById(R.id.tileHighlightBottom)
        tileHighlightBottom.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)
        board.addView(tileLayout)

        val tileTopLayout: View = activity.layoutInflater.inflate(R.layout.element_tile_cover, null)
        val tileHighlightTop: ImageView = tileTopLayout.findViewById(R.id.tileHighlightTop)
        tileHighlightTop.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)
        tileHighlightTop.tag = "$x,$y"
        if (x % 2 == y % 2) {
            tileHighlightTop.setOnClickListener { tileClicked(it) }
        }
        boardCover.addView(tileTopLayout)

        tiles.setTile(x, y, tile, tileHighlightTop, tileHighlightBottom)
    }

    private fun loadSnapshotFromHistory(isRedo: Boolean) {
        val entry = if (isRedo) gameData!!.gameHistory.redo() else gameData!!.gameHistory.undo()
        gameData.loadFromHistory(entry)
        closeAvailableTiles()
        unselectPieceAndCloseRelatedTiles()

        putPiecesOnBoard()
        updateAvailableTiles()

        delegate?.boardDelegateSnapshotWasLoadedFromHistory(
            gameData.player1CapturedPieces,
            gameData.player2CapturedPieces
        )
    }

    private fun activateTilesForSelectedPiece(tile: Tile) {
        if (tile.state != Tile.State.Selected) throw IllegalStateException()
        activatedTilesForSelectedTile.clear()

        for (move in gameData!!.game.getAvailableMovesForPiece(tile.x, tile.y)) {
            val tileToLand = tiles.get(move.to.x, move.to.y)
            if (tileToLand.state != Tile.State.Default) throw IllegalStateException("expected ${Tile.State.Default} but was ${tileToLand.state}") //Todo use this pattern!
            tileToLand.state = Tile.State.CanLand
            activatedTilesForSelectedTile.add(tileToLand)

            if (move.capture != null) {
                val tileToCapture = tiles.get(move.capture.x, move.capture.y)
                if (tileToCapture.state != Tile.State.Default && tileToCapture.state != Tile.State.CanCapture) {
                    throw IllegalStateException()
                }
                tileToCapture.state = Tile.State.CanCapture
                activatedTilesForSelectedTile.add(tileToCapture)
            }

            if (gameData.game.isExtraTurn) {
                tile.state = Tile.State.CanPassTurn
            }
        }
    }

    private fun selectPieceAndOpenRelatedTiles(tile: Tile) {
        if (tile.pieceType == null) throw InternalError()
        selectedTile = tile
        tile.state = Tile.State.Selected
        activateTilesForSelectedPiece(tile)
    }

    private fun unselectPieceAndCloseRelatedTiles() {
        selectedTile?.state = Tile.State.Default
        selectedTile = null
        activatedTilesForSelectedTile.forEach { it.state = Tile.State.Default }
        activatedTilesForSelectedTile.clear()
    }

    private fun openAvailableTiles() {
        availableTiles.forEach { it.state = Tile.State.Available }
    }

    private fun closeAvailableTiles() {
        availableTiles.forEach { it.state = Tile.State.Default }
    }

    private fun makeAMove(fromTile: Tile, toTile: Tile) {
        isEnabled = false

        val player = gameData!!.game.currentPlayer
        val captured = gameData.game.makeAMove(fromTile.x, fromTile.y, toTile.x, toTile.y)
        val pieceAtDest = gameData.game.getPiece(toTile.x, toTile.y)
        notifyDelegateAMoveWasMade()

        if (captured != null) {
            val capturedPieces = if (player == gameData.player1) gameData.player1CapturedPieces else gameData.player2CapturedPieces
            capturedPieces.add(captured.piece)
        }
        val capturedTile = if (captured != null) tiles.get(captured.x, captured.y) else null
        fromTile.movePiece(toTile, capturedTile, pieceAtDest) {
            notifyDelegateTurnWasFinished()
        }
    }

    private fun notifyDelegateTurnWasFinished() {
        delegate?.boardDelegateFinishedTurn(
            if (gameData!!.game.winner == null) gameData.game.currentPlayer else null,
            gameData.player1CapturedPieces,
            gameData.player2CapturedPieces
        )
    }

    private fun notifyDelegateAMoveWasMade() = delegate?.boardDelegateMadeAMove()

    private fun passTurn() {
        gameData!!.game.passTurn()
        notifyDelegateAMoveWasMade()
        notifyDelegateTurnWasFinished()
    }

    private fun tileClicked(tileHighlightTop: View) {
        val (x, y) = tileHighlightTop.tag.toString().split(",").map { it.toInt() }
        val tile = tiles.get(x, y)

        when (tile.state) {
            Tile.State.Available -> {
                closeAvailableTiles()
                selectPieceAndOpenRelatedTiles(tile)
            }
            Tile.State.Selected -> {
                unselectPieceAndCloseRelatedTiles()
                openAvailableTiles()
            }
            Tile.State.CanLand -> {
                makeAMove(selectedTile!!, tile)
                unselectPieceAndCloseRelatedTiles()
            }
            Tile.State.Default -> {
                if (selectedTile != null) {
                    unselectPieceAndCloseRelatedTiles()
                    openAvailableTiles()
                }
            }
            Tile.State.CanPassTurn -> {
                unselectPieceAndCloseRelatedTiles()
                passTurn()
            }
            Tile.State.CanCapture -> {
            }
        }
    }

    private fun updateAvailableTiles() {
        availableTiles.clear()
        activatedTilesForSelectedTile.clear()

        for (tileData in gameData!!.game.availablePieces) {
            val tile = tiles.get(tileData.x, tileData.y)
            if (tile.state != Tile.State.Default) throw java.lang.IllegalStateException("tile state is not default")
            availableTiles.add(tile)
        }
        openAvailableTiles()
    }


    private fun putPiecesOnBoard() {
        piecesContainer.removeAllViews()
        tiles.traversePlayableTiles { it.removePiece() }

        val piecesOnBoard =
            gameData!!.game.getAllPiecesForPlayer(Player.White) + gameData.game.getAllPiecesForPlayer(
                Player.Black
            )
        for (tileData in piecesOnBoard) {
            val tile = tiles.get(tileData.x, tileData.y)
            val pieceLayout: FrameLayout =
                activity.layoutInflater.inflate(R.layout.element_piece, null) as FrameLayout
            pieceLayout.findViewById<ImageView>(R.id.pieceImage).layoutParams =
                FrameLayout.LayoutParams(tileSize, tileSize)
            piecesContainer.addView(pieceLayout)

            tile.setPiece(pieceLayout, tileData.piece)


            val tileView = tile.tileImageView
            val tileLocation = IntArray(2) { 0 }
            tileView.getLocationInWindow(tileLocation)
            val boardStartLocation = IntArray(2) { 0 }
            piecesContainer.getLocationInWindow(boardStartLocation)

            pieceLayout.x = (tileLocation[0] - boardStartLocation[0]).toFloat()
            pieceLayout.y = (tileLocation[1] - boardStartLocation[1]).toFloat()
        }
    }


    class Tiles(private val tilesInBoard: Int) {
        private val array = Array(tilesInBoard) { Array<Tile?>(tilesInBoard) { null } }

        fun get(x: Int, y: Int): Tile {
            return array[x][y]!!
        }

        fun setTile(
            x: Int,
            y: Int,
            tileImageView: ImageView,
            tileHighlightTop: ImageView,
            tileHighlightBottom: ImageView
        ) {
            array[x][y] =
                Tile(x, y, tileImageView, tileHighlightTop, tileHighlightBottom, tilesInBoard)
        }

        fun traversePlayableTiles(apply: (playableTile: Tile) -> Unit) {
            for (x in array.indices) {
                for (y in array[x].indices) {
                    if (x % 2 == y % 2) {
                        apply(array[x][y]!!)
                    }
                }
            }
        }
    }


    interface Delegate {
        fun boardDelegateMadeAMove()
        fun boardDelegateFinishedTurn(
            currentPlayer: Player?,
            player1CapturedPieces: List<Piece>,
            player2CapturedPieces: List<Piece>
        )

        fun boardDelegateSnapshotWasLoadedFromHistory(
            player1CapturedPieces: List<Piece>,
            player2CapturedPieces: List<Piece>
        )
    }
}


class Tile(
    var x: Int,
    var y: Int,
    var tileImageView: ImageView,
    var tileHighlightTop: ImageView,
    var tileHighlightBottom: ImageView,
    private var tilesInBoard: Int
) {

    var pieceLayout: FrameLayout? = null
        private set
    var pieceType: Piece? = null
        private set

    fun setPiece(pieceLayout: FrameLayout, pieceType: Piece) {
        this.pieceLayout = pieceLayout
        this.pieceType = pieceType
        setPieceViewImage(pieceType)
    }

    fun removePiece() {
        this.pieceLayout = null
        this.pieceType = null
    }

    enum class State {
        Default, Available, Selected, CanLand, CanCapture, CanPassTurn
    }

    var state: State = State.Default
        set(state) {
            when (state) {
                State.Default -> {
                    tileHighlightBottom.setBackgroundResource(R.color.transparent)
                }
                State.Available -> {
                    tileHighlightBottom.setBackgroundResource(R.color.tileHighlight_available)
                }
                State.Selected, State.CanPassTurn -> {
                    tileHighlightBottom.setBackgroundResource(R.color.tileHighlight_selected)
                }
                State.CanLand -> {
                    tileHighlightBottom.setBackgroundResource(R.color.tileHighlight_canLand)
                }
                State.CanCapture -> {
                    tileHighlightBottom.setBackgroundResource(R.color.tileHighlightBottom_canCapture)
                }
            }
            tileHighlightTop.setImageResource(if (state == State.CanPassTurn) R.drawable.crap_pass_turn_highlight else R.color.transparent)
            field = state
        }

    fun movePiece(
        destinationTile: Tile,
        capturedTile: Tile? = null,
        newPieceType: Piece,
        doAfterAnimation: () -> Unit
    ) {
        if (pieceType == null || destinationTile.pieceType != null
            || pieceLayout == null || destinationTile.pieceLayout != null
        ) {
            throw IllegalStateException()
        }

        val destinationTileLoc = IntArray(2)
        destinationTile.tileImageView.getLocationInWindow(destinationTileLoc)
        val containerStartLoc = IntArray(2)
        (pieceLayout!!.parent as ViewGroup).getLocationInWindow(containerStartLoc)

        val distance = abs(x - destinationTile.x)
        val maxDistance = tilesInBoard - 1
        val duration = 300L + 450 / (maxDistance / distance)

        movePieceByAnimation(
            destinationTileLoc[0] - containerStartLoc[0],
            destinationTileLoc[1] - containerStartLoc[1],
            duration, doAfterAnimation
        )

        if (newPieceType != pieceType) {
            setPieceViewImage(newPieceType)
        }

        if (capturedTile != null) {
            removeCapturedPieceWhenCapturingPieceIsOnTop(
                capturedTile,
                destinationTile,
                duration,
                distance
            )
        }

        destinationTile.pieceType = pieceType
        destinationTile.pieceLayout = pieceLayout
        pieceType = null
        pieceLayout = null

    }

    private fun movePieceByAnimation(
        xTranslation: Int,
        yTranslation: Int,
        duration: Long,
        doAfterAnimation: () -> Unit
    ) {
        pieceLayout!!.animate().translationX(xTranslation.toFloat())
            .translationY(yTranslation.toFloat()).setDuration(duration).setListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) = doAfterAnimation()
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                }
            )

    }

    private fun removeCapturedPieceWhenCapturingPieceIsOnTop(
        capturedTile: Tile,
        destinationTile: Tile,
        duration: Long,
        distance: Int
    ) {
        capturedTile.pieceLayout!!.elevation -= 1

        val distanceCaptured = abs(capturedTile.x - destinationTile.x)
        val durationReachesCaptured =
            (duration * ((distance - distanceCaptured).toFloat() / distance)).toLong()

        Handler().postDelayed({
            (capturedTile.pieceLayout!!.parent as ViewGroup).removeView(capturedTile.pieceLayout)
            capturedTile.pieceLayout = null
            capturedTile.pieceType = null
        }, durationReachesCaptured)
    }

    private fun setPieceViewImage(pieceType: Piece) {
        pieceLayout!!.findViewById<ImageView>(R.id.pieceImage)
            .setImageResource(getPieceImageResource(pieceType))
    }

}

fun getPieceImageResource(pieceType: Piece) =
    when (pieceType) {
        Piece.BlackPawn -> R.drawable.piece_black_pawn
        Piece.BlackKing -> R.drawable.piece_black_king
        Piece.WhitePawn -> R.drawable.piece_red_pawn
        Piece.WhiteKing -> R.drawable.piece_red_king
    }
