package el.arn.opencheckers.android_widgets.main_activity.board

import android.content.Context
import android.view.LayoutInflater
import android.widget.GridLayout
import el.arn.opencheckers.complementaries.IntIteratorWithRemainderCorrection
import el.arn.opencheckers.complementaries.TwoDimenPointsArray
import el.arn.opencheckers.complementaries.android.PixelCoordinate
import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.structs.Move
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.complementaries.game.TileCoordinates
import el.arn.opencheckers.game.game_core.game_core.structs.Point
import el.arn.opencheckers.android_widgets.main_activity.board.parts.PlayableTile
import el.arn.opencheckers.android_widgets.main_activity.board.parts.Tile
import el.arn.opencheckers.android_widgets.main_activity.board.parts.UnplayableTile


interface TilesManager : HoldsListeners<TilesManager.Listener>{

    fun buildLayout(tilesPerRow: Int, whitePiecesStartOnBoardTop: Boolean)
    fun enableTileSelection(availableTilesForTurn: AvailableTilesForTurn) /**get it from [PossibleMovesForTurnBuilder]*/;
    fun disableSelectionAndRemoveHighlight()

    val tileLengthInPx: Float
    val tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate>

    interface Listener {
        fun moveWasSelectedByUser(from: TileCoordinates, to: TileCoordinates) {}
        fun moveWasSelectedByUserPassedTurn() {}
        fun boardWasClickedWhenSelectionIsDisabled() {}
    }
}

class TilesManager_impl(
    private var layout: GridLayout,
    private var layoutLengthInPx: Float,
    private var tilesPerRow: Int,
    private var whitePiecesStartOnBoardTop: Boolean,
    private val listenersMgr: ListenersManager<TilesManager.Listener> = ListenersManager()
) : TilesManager,
    HoldsListeners<TilesManager.Listener> by listenersMgr {

    var isTileSelectionEnabled = false; private set

    override var tileLengthInPx = 0f
    override lateinit var tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate>

    private lateinit var tiles : TwoDimenPointsArray<Tile>
    private var availableTilesForCurrentTurn: AvailableTilesForTurn? = null

    init {
        buildLayout(layout, layoutLengthInPx, tilesPerRow, whitePiecesStartOnBoardTop)
    }

    private fun buildLayout(layout: GridLayout?, layoutLengthInPx: Float?, tilesPerRow: Int?, whitePiecesStartOnBoardTop: Boolean?) {
        if (layout != null) {
            this.layout = layout
        }
        this.layout.removeAllViews()

        if (tilesPerRow != null) {
            this.tilesPerRow = tilesPerRow
            this.layout.columnCount = tilesPerRow
            this.layout.rowCount = tilesPerRow
        }
        if (whitePiecesStartOnBoardTop != null) {
            this.whitePiecesStartOnBoardTop = whitePiecesStartOnBoardTop
        }
        if (layoutLengthInPx != null){
            this.layoutLengthInPx = layoutLengthInPx
        }

        createTheTilesAndPutThemInLayout()
    }


    override fun buildLayout(tilesPerRow: Int, whitePiecesStartOnBoardTop: Boolean) {
        buildLayout(null, null, tilesPerRow, whitePiecesStartOnBoardTop)
    }

    override fun enableTileSelection(availableTilesForTurn: AvailableTilesForTurn) {
        this.availableTilesForCurrentTurn = availableTilesForTurn
        highlightTilesForFirstClick()
        isTileSelectionEnabled = true
    }

    override fun disableSelectionAndRemoveHighlight() {
        this.availableTilesForCurrentTurn = null
        unhighlightTiles()
        isTileSelectionEnabled = false
    }


    private fun createTheTilesAndPutThemInLayout() {
        val tileLength = layoutLengthInPx.toDouble() / tilesPerRow;

        tileLengthInPx = tileLength.toFloat()

        val layoutLengthCorrectionForX = IntIteratorWithRemainderCorrection(tileLength)
        val layoutLengthCorrectionForY = IntIteratorWithRemainderCorrection(tileLength)

        val tilesRange = if (!whitePiecesStartOnBoardTop) (tilesPerRow - 1 downTo 0) else (0 until tilesPerRow)
        val tilesStoredInLayout = TwoDimenPointsArray<Tile?>(tilesPerRow) { null }
        val maxTileLengthPerRowX = IntArray(tilesPerRow)
        val maxTileLengthPerColumnY = IntArray(tilesPerRow)

        for (y in tilesRange) {
            for (x in tilesRange) {
                val tileLengthInPx = when (tilesPerRow - 1) {
                    x -> layoutLengthCorrectionForX.getInt()
                    y -> layoutLengthCorrectionForY.getInt()
                    else -> tileLength.toInt()
                }
                val tile = createTile(x, y, tileLengthInPx)

                maxTileLengthPerRowX[x] = Math.max(maxTileLengthPerRowX[x], tileLengthInPx)
                maxTileLengthPerColumnY[y] = Math.max(maxTileLengthPerColumnY[y], tileLengthInPx)

                layout.addView(tile.layout)
                tilesStoredInLayout[x, y] = tile
            }
        }
        this.tiles = TwoDimenPointsArray(tilesPerRow) { tilesStoredInLayout[it]!! }
        this.tilesLocationInWindow = calculateTilesLocationInWindow(maxTileLengthPerRowX, maxTileLengthPerColumnY)
    }

    private fun calculateTilesLocationInWindow(maxTileLengthPerRowX: IntArray, maxTileLengthPerColumnY: IntArray): TwoDimenPointsArray<PixelCoordinate> {
        //because the tiles views has yet to be initialized, the coordinates needs to be obtained by rigid calculation
        var tilesLocationInWindow = TwoDimenPointsArray<PixelCoordinate?>(tilesPerRow) {
            tileIndex: Point ->

            var x = 0
            for (tileLength in 0 until tileIndex.x) {
                x += maxTileLengthPerRowX[tileIndex.x]
            }
            var y = 0
            for (tileLength in 0 until tileIndex.y) {
                y += maxTileLengthPerColumnY[tileIndex.y]
            }
            PixelCoordinate(x,y)
        }


        if (!whitePiecesStartOnBoardTop) {
            tilesLocationInWindow = flipTilesLocationInWindow(tilesLocationInWindow)
        }

        return TwoDimenPointsArray(tilesPerRow) { tilesLocationInWindow[it]!! }
    }

    private fun flipTilesLocationInWindow(tilesLocationInWindow: TwoDimenPointsArray<PixelCoordinate?>): TwoDimenPointsArray<PixelCoordinate?> {
        val iterator = tilesLocationInWindow.iterator()
        val range = tilesPerRow - 1 downTo 0

        val flipped = TwoDimenPointsArray<PixelCoordinate?>(tilesPerRow) { null }

        for (y in range) {
            for (x in range) {
                flipped[x,y] = iterator.next()
            }
        }
        return flipped
    }

    private fun createTile(x: Int, y: Int, tileLengthInPx: Int): Tile {
        val constructor = if (x % 2 == y % 2) ::PlayableTile else ::UnplayableTile
        return constructor.invoke(
            layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            tileLengthInPx,
            TileCoordinates(x, y)
        ) { tileWasClicked(
            TileCoordinates(
                x,
                y
            )
        ) }
    }

//    private var currentlyHighlightedTiles = mutableSetOf<Tile>()
//    private var currentlyHighlightedTilesForSelectedTile = mutableSetOf<Tile>()
    private var tileSelected: Tile? = null

    private fun tileWasClicked(tileCoordinates: TileCoordinates) {

        if (!isTileSelectionEnabled) {
            listenersMgr.notifyAll { it.boardWasClickedWhenSelectionIsDisabled() }
            return
        }

        val tile = tiles[tileCoordinates]
        if (tile is PlayableTile) {
            when (tile.state) {
                PlayableTile.States.Highlighted -> { tileSelected = tile; highlightTilesForSelectedTile(tile.tileCoordinates) }
                PlayableTile.States.CanCapture -> return
                PlayableTile.States.Selected, PlayableTile.States.Inactive -> highlightTilesForFirstClick()
                PlayableTile.States.CanLand -> moveSelected(tile.tileCoordinates)
                PlayableTile.States.SelectedAndCanPassTurn -> movePassed()
            }
        }

    }


    private fun movePassed() {
        listenersMgr.notifyAll { it.moveWasSelectedByUserPassedTurn() }
    }

    private fun moveSelected(destinationTile: TileCoordinates) {
        val tileSelected = tileSelected ?: error("internal error")
        listenersMgr.notifyAll { it.moveWasSelectedByUser(tileSelected.tileCoordinates, destinationTile) }
    }

    private fun unhighlightTiles() {
        var a = 0
//        for (y in 0 until tilesPerRow) {
//            for (x in 0 until tilesPerRow) {
//                val tile = tiles[x,y]
//                a++
//                if (tile !is PlayableTile) { continue }
//                if (tile.state != PlayableTile.States.Inactive) {
//                    tile.state = PlayableTile.States.Inactive
//                }
//            }
//        }
        for (tile in tiles) {
            a++
            if (tile !is PlayableTile) { continue }
            if (tile.state != PlayableTile.States.Inactive) {
                tile.state = PlayableTile.States.Inactive
            }
        }
        val b = a
    }

    private fun highlightTilesForFirstClick() {
        val availablePieces = availableTilesForCurrentTurn?.availablePieces ?: error("")
        unhighlightTiles()
        for (tileNumber in availablePieces) {
            val tile = tiles[tileNumber] as PlayableTile
            tile.state = PlayableTile.States.Highlighted
        }
    }

    private fun highlightTilesForSelectedTile(selectedTile: TileCoordinates) {
        val possibleMovesForPiece = availableTilesForCurrentTurn?.possibleMovesForEveryAvailablePlayersPiece?.get(selectedTile) ?: error("")
        unhighlightTiles()
        for (move in possibleMovesForPiece) {
            val tileSelected: PlayableTile = tiles[selectedTile] as PlayableTile
            tileSelected.state = if (availableTilesForCurrentTurn!!.canPassTurn) {
                    PlayableTile.States.SelectedAndCanPassTurn
                } else {
                    PlayableTile.States.Selected
                }

            val destinationTile: PlayableTile = tiles[move.to.x, move.to.y] as PlayableTile
            destinationTile.state = PlayableTile.States.CanLand

            val tileOfTheCapturedPieceIfAny = move.captures?.let{ tiles[it.x, it.y] as PlayableTile }
            tileOfTheCapturedPieceIfAny?.state = PlayableTile.States.CanCapture
        }

    }



}

class PossibleMovesForTurnBuilder {
    companion object {
        fun build(game: Game): AvailableTilesForTurn {

            val _possibleMovesForEveryAvailablePlayersPiece =
                TwoDimenPointsArray<Set<Move>?>(
                    game.boardSize
                ) { null }
            for (availablePiece in game.availablePieces) {
                _possibleMovesForEveryAvailablePlayersPiece[availablePiece.x, availablePiece.y] = game.getAvailableMovesForPiece(availablePiece.x, availablePiece.y)
            }
            val possibleMovesForEveryAvailablePlayersPiece =
                TwoDimenPointsArray(game.boardSize) { _possibleMovesForEveryAvailablePlayersPiece[it] }

            val availablePieces = game.availablePieces.map {
                TileCoordinates(
                    it.x,
                    it.y
                )
            }.toSet()

            return AvailableTilesForTurn(
                game.canPassExtraTurn(),
                availablePieces,
                possibleMovesForEveryAvailablePlayersPiece
            )
        }
    }
}

data class AvailableTilesForTurn(
    val canPassTurn: Boolean,
    val availablePieces: Set<TileCoordinates>,
    val possibleMovesForEveryAvailablePlayersPiece: TwoDimenPointsArray<Set<Move>?>
)