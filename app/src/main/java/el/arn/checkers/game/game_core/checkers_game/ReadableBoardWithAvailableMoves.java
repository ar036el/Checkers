package el.arn.checkers.game.game_core.checkers_game;

import el.arn.checkers.game.game_core.checkers_game.structs.Move;
import el.arn.checkers.game.game_core.checkers_game.structs.Tile;

import java.util.Set;

public interface ReadableBoardWithAvailableMoves extends ReadableBoard {
    Set<Tile> getAvailablePieces();
    Set<Move> getAvailableMovesForPiece(int x, int y);
    void reloadAvailableMoves();
}
