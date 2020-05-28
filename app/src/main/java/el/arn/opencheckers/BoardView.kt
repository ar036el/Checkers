package el.arn.opencheckers

import android.animation.Animator
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import el.arn.opencheckers.checkers_game.game_core.structs.Piece
import el.arn.opencheckers.checkers_game.game_core.structs.Player
import kotlin.math.abs


//TOdo when rotating when virtual player is playing- bug


class BoardView(
    private val boardLayout: FrameLayout,
    private val gameData: GameData?,
    private val boardSizeInPx: Int,
    private val tilesInBoard: Int,
    var delegate: Delegate? = null
) {

    val currentPlayer: Player?
        get() = gameData?.game?.currentPlayer
    val player1: Player?
        get() = gameData?.player1
    val player2: Player?
        get() = gameData?.player2
    val winner: Player?
        get() = gameData?.game?.winner
    val player1CapturedPieces: List<Piece>
        get() = gameData?.player1CapturedPieces?.toList() ?: emptyList()
    val player2CapturedPieces: List<Piece>
    get() = gameData?.player2CapturedPieces?.toList() ?: emptyList()
    val hasAGame: Boolean
        get() = (gameData != null)

    enum class BoardGameState { NoGameLoaded, GameIsOn, GameWasFinished }
    lateinit var boardGameState: BoardGameState
        private set

    private val playableTiles = Tiles(tilesInBoard)
    private val tileSize: Int

    private val boardGridLayout: GridLayout = boardLayout.findViewById(R.id.board)
    private val boardBackground: ImageView = boardLayout.findViewById(R.id.boardBackground)
    private val piecesContainerLayout: FrameLayout = boardLayout.findViewById(R.id.boardPiecesContainer)

    var isUserInteractionEnabled = false
        private set

    private var selectedTile: Tile? = null
    private val availableTiles = mutableSetOf<Tile>()
    private val activatedTilesForSelectedTile = mutableSetOf<Tile>()

    private val notPlayableTilesImageViews = mutableSetOf<ImageView>()
    private lateinit var boardThemeChangedPrefListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var gameRulesChangedPrefListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val resources = boardLayout.resources

    private val layoutInflater = boardLayout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        initBoardGameState()

        boardGridLayout.removeAllViews()

        boardBackground.layoutParams = FrameLayout.LayoutParams(boardSizeInPx, boardSizeInPx)
        piecesContainerLayout.layoutParams = FrameLayout.LayoutParams(boardSizeInPx, boardSizeInPx)
        boardGridLayout.columnCount = tilesInBoard;
        boardGridLayout.rowCount = tilesInBoard;

        val tileSize = boardSizeInPx.toDouble() / tilesInBoard;
        this.tileSize = tileSize.toInt()

        createTiles(tileSize, tilesInBoard)

        boardBackground.invalidate() //TODO do it everywhere the width is being changed https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        boardBackground.requestLayout()
        piecesContainerLayout.invalidate() //TODO do it everywhere the width is being changed https://stackoverflow.com/questions/35279374/why-is-requestlayout-being-called-directly-after-invalidate/40402309
        piecesContainerLayout.requestLayout()

        registerPrefListenerWhenBoardThemeChanges()
        registerPrefListenerWhenGameRulesChanges()

    }

    private fun initBoardGameState() {
        boardGameState =
            when {
                gameData == null -> BoardGameState.NoGameLoaded
                gameData.game.winner == null -> BoardGameState.GameIsOn
                else -> BoardGameState.GameWasFinished
            }
    }

    fun initGamePieces() {
        if (gameData == null) { throw IllegalStateException("game was not created yet") }
        putPiecesOnBoard()
    }

    fun enableUserInteraction() {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        isUserInteractionEnabled = true
        updateAvailableTiles()
    }

    val hasUndo
        get() = (gameData?.hasUndo == true)
    val hasRedo
        get() = (gameData?.hasRedo == true)

    fun undo() {
        if (!hasUndo || gameData == null) throw IllegalStateException()
        if (isUserInteractionEnabled) {
            loadSnapshotFromHistory(GameHistoryManager.Operation.Undo)
        }
        boardGameState = if (gameData.game.winner == null) BoardGameState.GameIsOn else BoardGameState.GameWasFinished
    }

    fun redo() {
        if (!hasRedo || gameData == null) throw IllegalStateException()
        if (isUserInteractionEnabled) {
            loadSnapshotFromHistory(GameHistoryManager.Operation.Redo)
        }
        boardGameState = if (gameData.game.winner == null) BoardGameState.GameIsOn else BoardGameState.GameWasFinished
    }

    fun saveSnapshotToHistory() {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        gameData.saveStateToHistory()
    }

    fun makeAMoveManually(xFrom: Int, yFrom: Int, xTo: Int, yTo: Int) {
        if (gameData == null) throw IllegalStateException("game was not created yet")
        val fromTile = playableTiles.get(xFrom, yFrom)
        val toTile = playableTiles.get(xTo, yTo)
        makeAMove(fromTile, toTile)
    }

    fun hasGameFinished():Boolean = boardGameState == BoardGameState.GameWasFinished

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

                addTile(tileLengthFixed, x, y);
            }
        }
    }

    private fun addTile(tileLengthInPx: Int, x: Int, y: Int) {
        val tileLayout: View = layoutInflater.inflate(R.layout.element_tile, null)
        val tile: ImageView = tileLayout.findViewById(R.id.tile)

        tile.setImageResource(getTileColorResDefaultState(x, y))
        tile.alpha = resources.getFloat(R.dimen.tileAlpha_Default)
        tile.layoutParams = FrameLayout.LayoutParams(tileLengthInPx, tileLengthInPx)

        tile.tag = "$x,$y"


        val isPlayableTile = (x % 2 == y % 2)
        tile.setOnClickListener {
            delegate?.boardDelegateBoardClickedWhenGameIsNotRunning()
            if (isPlayableTile) {
                playableTileClicked(it)
            }
        }

        if (!isPlayableTile) {
            notPlayableTilesImageViews.add(tile)
        }

        boardGridLayout.addView(tileLayout)
        playableTiles.setTile(x, y, tile)
    }

    private fun registerPrefListenerWhenBoardThemeChanges() {
        boardThemeChangedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            if (key == resources.getString(R.string.pref_boardTheme)
                || key == resources.getString(R.string.pref_playerTheme)) {
                reloadBoardGraphics()
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(boardThemeChangedPrefListener)
    }

    private fun reloadBoardGraphics() {
        playableTiles.traversePlayableTiles {
            it.state = it.state //refreshes graphics
            if (it.pieceType != null) {
                it.pieceLayout!!
                    .findViewById<ImageView>(R.id.pieceImage)
                    .setBackgroundResource(getPieceImageResource(it.pieceType!!))
            }
        }
        notPlayableTilesImageViews.forEach { it.setImageResource(ResourcesByTheme.NotPlayableTile()) }
    }

    private fun registerPrefListenerWhenGameRulesChanges() {
        gameRulesChangedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            if (key == resources.getString(R.string.pref_isCapturingMandatory)
                || key == Strings.get(R.string.pref_kingBehaviour)
                || key == resources.getString(R.string.pref_canManCaptureBackwards)) {
                if (gameData != null) {
                    reloadAvailablePieces()
                }

            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(gameRulesChangedPrefListener)
    }

    private fun reloadAvailablePieces() {
        //TODO bad things happening here
        unselectPieceAndCloseRelatedTiles()
        closeAvailableTiles()
        gameData!!.game.reloadAvailableMoves()
        updateAvailableTiles()
    }

    private fun loadSnapshotFromHistory(operation: GameHistoryManager.Operation) {
        if (operation == GameHistoryManager.Operation.Undo) {
            gameData!!.undo()
        } else {
            gameData!!.redo()
        }

        closeAvailableTiles()
        unselectPieceAndCloseRelatedTiles()

        putPiecesOnBoard()
        updateAvailableTiles()

        delegate?.boardDelegateSnapshotLoadedFromHistory(
            gameData.player1CapturedPieces,
            gameData.player2CapturedPieces
        )
    }

    private fun activateTilesForSelectedPiece(tile: Tile) {
        if (tile.state != Tile.State.Selected) throw IllegalStateException()
        activatedTilesForSelectedTile.clear()

        for (move in gameData!!.game.getAvailableMovesForPiece(tile.x, tile.y)) {
            val tileToLand = playableTiles.get(move.to.x, move.to.y)
            if (tileToLand.state != Tile.State.Default) throw IllegalStateException("expected ${Tile.State.Default} but was ${tileToLand.state}") //Todo use this pattern!
            tileToLand.state = Tile.State.CanLand
            activatedTilesForSelectedTile.add(tileToLand)

            if (move.capture != null) {
                val tileToCapture = playableTiles.get(move.capture.x, move.capture.y)
                if (tileToCapture.state != Tile.State.Default && tileToCapture.state != Tile.State.CanCapture) {
                    throw IllegalStateException()
                }
                tileToCapture.state = Tile.State.CanCapture
                activatedTilesForSelectedTile.add(tileToCapture)
            }

            if (gameData.game.isExtraTurn && gameData.game.canPassExtraTurn()) {
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
        isUserInteractionEnabled = false

        val player = gameData!!.game.currentPlayer
        val captured = gameData.game.makeAMove(fromTile.x, fromTile.y, toTile.x, toTile.y)
        val pieceAtDest = gameData.game.getPiece(toTile.x, toTile.y)
        notifyDelegateAMoveWasMade()

        if (captured != null) {
            val capturedPieces = if (player == gameData.player1) gameData.player1CapturedPieces else gameData.player2CapturedPieces
            capturedPieces.add(captured.piece)
        }
        val capturedTile = if (captured != null) playableTiles.get(captured.x, captured.y) else null
        fromTile.movePiece(toTile, capturedTile, pieceAtDest) {
            if (gameData.game.winner != null) {
                boardGameState = BoardGameState.GameWasFinished
                delegate?.boardDelegateGameHasFinished()
            }
            notifyDelegateTurnWasFinished()
        }
    }

    private fun notifyDelegateTurnWasFinished() {
        delegate?.boardDelegateFinishedTurn(
            if (gameData!!.game.winner == null) gameData.game.currentPlayer else null,
            gameData.player1CapturedPieces.toList(),
            gameData.player2CapturedPieces.toList()
        )
    }

    private fun notifyDelegateAMoveWasMade() = delegate?.boardDelegateMadeAMove()

    private fun passTurn() {
        gameData!!.game.passTurn()
        notifyDelegateAMoveWasMade()
        notifyDelegateTurnWasFinished()
    }

    private fun playableTileClicked(tileHighlightTop: View) {
        val (x, y) = tileHighlightTop.tag.toString().split(",").map { it.toInt() }
        val tile = playableTiles.get(x, y)

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
            val tile = playableTiles.get(tileData.x, tileData.y)
            if (tile.state != Tile.State.Default) throw java.lang.IllegalStateException("tile state is not default")
            availableTiles.add(tile)
        }
        openAvailableTiles()
    }


    private fun putPiecesOnBoard() {
        val a: LinearLayout = piecesContainerLayout as LinearLayout
        val b = a.orientation
        piecesContainerLayout.removeAllViews()
        playableTiles.traversePlayableTiles { it.removePiece() }

        val piecesOnBoard =
            gameData!!.game.getAllPiecesForPlayer(Player.White) + gameData.game.getAllPiecesForPlayer(
                Player.Black
            )
        for (tileData in piecesOnBoard) {
            val tile = playableTiles.get(tileData.x, tileData.y)
            val pieceLayout: FrameLayout =
                layoutInflater.inflate(R.layout.element_piece, null) as FrameLayout
            pieceLayout.findViewById<ImageView>(R.id.pieceImage).layoutParams =
                FrameLayout.LayoutParams(tileSize, tileSize)
            piecesContainerLayout.addView(pieceLayout)

            tile.setPiece(pieceLayout, tileData.piece)


            val tileView = tile.tileImageView
            val tileLocation = IntArray(2) { 0 }
            tileView.getLocationInWindow(tileLocation)
            val boardStartLocation = IntArray(2) { 0 }
            piecesContainerLayout.getLocationInWindow(boardStartLocation)

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
            tileImageView: ImageView
        ) {
            array[x][y] =
                Tile(x, y, tileImageView, tilesInBoard)
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

        fun boardDelegateSnapshotLoadedFromHistory(
            player1CapturedPieces: List<Piece>,
            player2CapturedPieces: List<Piece>
        )

        fun boardDelegateBoardClickedWhenGameIsNotRunning()
        fun boardDelegateGameHasFinished()
    }
}


class Tile(
    val x: Int,
    val y: Int,
    val tileImageView: ImageView,
    private val tilesInBoard: Int
) {

    val resources = tileImageView.resources

    var pieceLayout: FrameLayout? = null
        private set
    var pieceType: Piece? = null
        private set

    fun setPiece(pieceLayout: FrameLayout, pieceType: Piece) {
        this.pieceLayout = pieceLayout
        this.pieceType = pieceType
        setPieceImage(pieceType)
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
                    tileImageView.setImageResource(getTileColorResDefaultState(x, y))
                    tileImageView.alpha = resources.getFloat(R.dimen.tileAlpha_Default)
                }
                State.Available -> {
                    tileImageView.setImageResource(R.color.tileHighlight_available)
                    tileImageView.alpha = resources.getFloat(R.dimen.tileAlpha_Highlighted)

                }
                State.Selected, State.CanPassTurn -> {
                    tileImageView.setImageResource(R.color.tileHighlight_selected)
                    tileImageView.alpha = resources.getFloat(R.dimen.tileAlpha_Highlighted)
                }
                State.CanLand -> {
                    tileImageView.setImageResource(R.color.tileHighlight_canLand)
                    tileImageView.alpha = resources.getFloat(R.dimen.tileAlpha_Highlighted)
                }
                State.CanCapture -> {
                    tileImageView.setImageResource(R.color.tileHighlight_canCapture)
                    tileImageView.alpha = tileImageView.context.resources.getFloat(R.dimen.tileAlpha_Highlighted)
                    }
            }

            if (state == State.CanPassTurn) {
                pieceLayout!!.findViewById<ImageView>(R.id.pieceImage)
                    .setImageResource(R.drawable.crap_pass_turn_highlight) //this will be on top of the piece image, because the piece image was defined in the background layer
            } else {
                pieceLayout?.findViewById<ImageView>(R.id.pieceImage)
                    ?.setImageResource(android.R.color.transparent) //this will be on top of the piece image, because the piece image was defined in the background layer
            }
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
            setPieceImage(newPieceType)
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

    private fun setPieceImage(pieceType: Piece) {
        pieceLayout!!.findViewById<ImageView>(R.id.pieceImage)
            .setBackgroundResource(getPieceImageResource(pieceType))
    }

}

fun getPieceImageResource(pieceType: Piece) =
    when (pieceType) {
        Piece.BlackPawn -> ResourcesByTheme.BlackPawn()
        Piece.BlackKing -> ResourcesByTheme.BlackKing()
        Piece.WhitePawn -> ResourcesByTheme.WhitePawn()
        Piece.WhiteKing -> ResourcesByTheme.WhiteKing()
    }


enum class TileType { Playable, NotPlayable }