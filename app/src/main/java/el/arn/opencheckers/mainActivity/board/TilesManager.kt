package el.arn.opencheckers.mainActivity.board

import android.content.Context
import android.view.LayoutInflater
import android.widget.GridLayout
import el.arn.opencheckers.DoubleToIntCorrector
import el.arn.opencheckers.delegationMangement.DelegatesManager
import el.arn.opencheckers.delegationMangement.HoldsDelegates

class TilesManager(
    private val layout: GridLayout,
    private val boardSize: Int,
    private val layoutLengthInPx: Int,
    private val boardDirection: BoardDirection,
    private val delegationMgr: DelegatesManager<Delegate> = DelegatesManager()
) : HoldsDelegates<TilesManager.Delegate> by delegationMgr {
    enum class BoardDirection { WhitePiecesOnTop, WhitePiecesOnBottom }

    private val tiles = PointArray<Tile?>(boardSize) {null}

    init {
        layout.removeAllViews()
        layout.columnCount = boardSize
        layout.rowCount = boardSize

        createTiles()
    }

    private fun createTiles() {
        val tileSize = layoutLengthInPx.toDouble() / boardSize;
        val tileSizeCorrectorForX = DoubleToIntCorrector(tileSize)
        val tileSizeCorrectorForY = DoubleToIntCorrector(tileSize)

        val range = if (boardDirection == BoardDirection.WhitePiecesOnBottom) (boardSize - 1 downTo 0) else (0 until boardSize)
        for (y in range) {
            for (x in range) {
                val tileLengthInPx =
                    if (x == boardSize - 1)
                        tileSizeCorrectorForX.getInt()
                    else if (y == boardSize - 1)
                        tileSizeCorrectorForY.getInt()
                    else tileSize.toInt()

                val createNewTile = if (x % 2 == y % 2) ::PlayableTile else ::UnplayableTile
                val tile = createNewTile(
                    layout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                    tileLengthInPx, x, y,
                    { delegationMgr.notifyAll { it.tileWasClicked(x, y) }}
                )
                layout.addView(tile.layout)
                tiles[x, y] = tile
            }
        }
    }

    interface Delegate {
        fun tileWasClicked(boardX: Int, boardY: Int)
    }

}