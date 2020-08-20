package el.arn.checkers.game.game_core.game_core;

import el.arn.checkers.game.game_core.game_core.exceptions.PointIsOutOfBoardBoundsException;
import el.arn.checkers.game.game_core.game_core.exceptions.TileIsNotPlayableException;
import el.arn.checkers.game.game_core.game_core.structs.Piece;
import el.arn.checkers.game.game_core.game_core.structs.Player;
import el.arn.checkers.game.game_core.game_core.structs.Tile;

import java.util.Set;

public interface ReadableBoard {
    int getBoardSize();
    Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    Set<Tile> getAllPiecesForPlayer(Player player);
    Set<Tile> getAllPiecesInBoard();
}
