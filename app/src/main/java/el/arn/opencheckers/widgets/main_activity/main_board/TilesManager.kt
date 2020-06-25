package el.arn.opencheckers.widgets.main_activity.main_board

import android.content.Context
import android.view.LayoutInflater
import android.widget.GridLayout
import el.arn.opencheckers.complementaries.IntIteratorWithRemainderCorrection
import el.arn.opencheckers.complementaries.TwoDimenPointsArray
import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.structs.Move
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.complementaries.game.TileCoordinate
import el.arn.opencheckers.widgets.main_activity.main_board.parts.PlayableTile
import el.arn.opencheckers.widgets.main_activity.main_board.parts.Tile
import el.arn.opencheckers.widgets.main_activity.main_board.parts.UnplayableTile


interface TilesManager : HoldsListeners<TilesManager.Listener>{

    fun setLayout(layout: GridLayout, tilesPerRow: Int?, tilesStartFromTop: Boolean?, layoutLengthInPx: Float?)
    fun enableTileSelection(availableTilesForTurn: AvailableTilesForTurn) /**get it from [PossibleMovesForTurnBuilder]*/;
    fun disableSelectionAndRemoveHighlight()

    val tileLengthInPx: Float
    val tilesStoredInLayout: TwoDimenPointsArray<Tile>
    
    interface Listener {
        fun moveWasSelected(from: TileCoordinate, to: TileCoordinate) {}
        fun moveWasSelectedPassedTurn() {}
        fun boardWasClickedWhenSelectionIsDisabled() {}
    }
}

class TilesManager_impl(
    private var layout: GridLayout,
    private var layoutLengthInPx: Float,
    private var tilesPerRow: Int,
    private var whitePiecesOnTop: Boolean,
    private val delegationMgr: ListenersManager<TilesManager.Listener> = ListenersManager()
) : TilesManager,
    HoldsListeners<TilesManager.Listener> by delegationMgr {

    var isTileSelectionEnabled = false; private set
    override lateinit var tilesStoredInLayout : TwoDimenPointsArray<Tile>

    private var tileSelected: TileCoordinate? = null
    private var availableTilesData: AvailableTilesForTurn? = null

    init {
        setLayout(layout, tilesPerRow, whitePiecesOnTop, layoutLengthInPx)
    }

    override fun setLayout(layout: GridLayout, tilesPerRow: Int?, tilesStartFromTop: Boolean?, layoutLengthInPx: Float?) {
        this.layout = layout
        layout.removeAllViews()

        if (tilesPerRow != null) {
            this.tilesPerRow = tilesPerRow
            layout.columnCount = tilesPerRow
            layout.rowCount = tilesPerRow
        }
        if (tilesStartFromTop != null) {
            this.whitePiecesOnTop = tilesStartFromTop
        }
        if (layoutLengthInPx != null){
            this.layoutLengthInPx = layoutLengthInPx
        }

        createTheTilesAndPutThemInLayout()
    }

    override fun enableTileSelection(availableTilesForTurn: AvailableTilesForTurn) {
        this.availableTilesData = availableTilesForTurn
        highlightTiles()
        isTileSelectionEnabled = true
    }

    override fun disableSelectionAndRemoveHighlight() {
        this.availableTilesData = null
        unhighlightTiles()
        isTileSelectionEnabled = false
    }

    override var tileLengthInPx = 0f


    private fun createTheTilesAndPutThemInLayout() {
        val tileLength = layoutLengthInPx.toDouble() / tilesPerRow;

        tileLengthInPx = tileLength.toFloat()

        val layoutLengthCorrectionForX =
            IntIteratorWithRemainderCorrection(
                tileLength
            )
        val layoutLengthCorrectionForY =
            IntIteratorWithRemainderCorrection(
                tileLength
            )

        val range = if (whitePiecesOnTop) (tilesPerRow - 1 downTo 0) else (0 until tilesPerRow)
        val tilesStoredInLayout =
            TwoDimenPointsArray<Tile?>(
                tilesPerRow
            ) { null }
        for (y in range) {
            for (x in range) {
                val tileLengthInPx = when (tilesPerRow - 1) {
                    x -> layoutLengthCorrectionForX.getInt()
                    y -> layoutLengthCorrectionForY.getInt()
                    else -> tileLength.toInt()
                }
                val tile = createTile(x, y, tileLengthInPx)

                layout.addView(tile.layout)
                tilesStoredInLayout[x, y] = tile
            }
        }
        this.tilesStoredInLayout =
            TwoDimenPointsArray(tilesPerRow) { tilesStoredInLayout[it]!! }
    }

    private fun createTile(x: Int, y: Int, tileLengthInPx: Int): Tile {
        val constructor = if (x % 2 == y % 2) ::PlayableTile else ::UnplayableTile
        return constructor.invoke(
            layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            tileLengthInPx,
            TileCoordinate(x, y)
        ) { tileWasClicked(
            TileCoordinate(
                x,
                y
            )
        ) }
    }

    private fun tileWasClicked(tileCoordinate: TileCoordinate) {

        if (!isTileSelectionEnabled) {
            delegationMgr.notifyAll { it.boardWasClickedWhenSelectionIsDisabled() }
            return
        }

        val tile = tilesStoredInLayout[tileCoordinate]
        if (tile is PlayableTile) {
            when (tile.state) {
                PlayableTile.States.Highlighted -> highlightTilesForSelectedTile(tile.tileCoordinate)
                PlayableTile.States.CanCapture -> return
                PlayableTile.States.Selected, PlayableTile.States.Inactive -> highlightTiles()
                PlayableTile.States.CanLand -> moveSelected(tile.tileCoordinate)
                PlayableTile.States.SelectedAndCanPassTurn -> movePassed()
            }
        }

    }


    private fun movePassed() {
        delegationMgr.notifyAll { it.moveWasSelectedPassedTurn() }
    }

    private fun moveSelected(destinationTile: TileCoordinate) {
        val tileSelected = tileSelected ?: error("internal")
        delegationMgr.notifyAll { it.moveWasSelected(tileSelected, destinationTile) }
    }

    private fun unhighlightTiles() {
        for (tile in tilesStoredInLayout) {
            if (tile !is PlayableTile) return
            if (tile.state != PlayableTile.States.Inactive) {
                tile.state = PlayableTile.States.Inactive
            }
        }
    }

    private fun highlightTiles() {
        unhighlightTiles()

        val availablePieces = availableTilesData?.availablePieces ?: error("")

        for (tileNumber in availablePieces) {
            val tile = tilesStoredInLayout[tileNumber] as PlayableTile
            tile.state = PlayableTile.States.Highlighted
        }
    }

    private fun highlightTilesForSelectedTile(selectedTile: TileCoordinate) {
        val possibleMovesForPiece = availableTilesData?.possibleMovesForEveryAvailablePlayersPiece?.get(selectedTile) ?: error("")

        unhighlightTiles()

        for (move in possibleMovesForPiece) {
            val tileSelected: PlayableTile = tilesStoredInLayout[selectedTile] as PlayableTile
            tileSelected.state = if (availableTilesData!!.canPassTurn) {
                    PlayableTile.States.SelectedAndCanPassTurn
                } else {
                    PlayableTile.States.Selected
                }

            val destinationTile: PlayableTile = tilesStoredInLayout[move.to.x, move.to.y] as PlayableTile
            destinationTile.state = PlayableTile.States.CanLand

            val tileOfTheCapturedPieceIfAny = move.captures?.let{ tilesStoredInLayout[it.x, it.y] as PlayableTile }
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
                TwoDimenPointsArray(game.boardSize) { _possibleMovesForEveryAvailablePlayersPiece[it]!! }

            val availablePieces = game.availablePieces.map {
                TileCoordinate(
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
    val availablePieces: Set<TileCoordinate>,
    val possibleMovesForEveryAvailablePlayersPiece: TwoDimenPointsArray<Set<Move>>
)