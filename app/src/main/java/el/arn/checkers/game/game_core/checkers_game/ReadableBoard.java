package el.arn.checkers.game.game_core.checkers_game;

import el.arn.checkers.game.game_core.checkers_game.exceptions.PointIsOutOfBoardBoundsException;
import el.arn.checkers.game.game_core.checkers_game.exceptions.TileIsNotPlayableException;
import el.arn.checkers.game.game_core.checkers_game.structs.Piece;
import el.arn.checkers.game.game_core.checkers_game.structs.Player;
import el.arn.checkers.game.game_core.checkers_game.structs.Tile;

import java.util.Set;

public interface ReadableBoard {
    int getBoardSize();
    Integer getStartingRows();
    Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    Set<Tile> getAllPiecesForPlayer(Player player);
    Set<Tile> getAllPiecesInBoard();
}
