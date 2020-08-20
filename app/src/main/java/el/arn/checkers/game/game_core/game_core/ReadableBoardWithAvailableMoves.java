package el.arn.checkers.game.game_core.game_core;

import el.arn.checkers.game.game_core.game_core.structs.Move;
import el.arn.checkers.game.game_core.game_core.structs.Tile;

import java.util.Set;

public interface ReadableBoardWithAvailableMoves extends ReadableBoard {
    Set<Tile> getAvailablePieces();
    Set<Move> getAvailableMovesForPiece(int x, int y);
    void reloadAvailableMoves();
}
