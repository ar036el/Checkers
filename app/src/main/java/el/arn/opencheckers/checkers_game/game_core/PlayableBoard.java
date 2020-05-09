package el.arn.opencheckers.checkers_game.game_core;

import el.arn.opencheckers.checkers_game.game_core.exceptions.*;
import el.arn.opencheckers.checkers_game.game_core.structs.Piece;

public interface PlayableBoard extends ReadableBoard {
    void addPiece(int x, int y, Piece piece) throws TileIsAlreadyOccupiedException, PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    void movePiece(int xFrom, int yFrom, int xTo, int yTo) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, TileIsAlreadyOccupiedException, PieceWasNotSelectedException;
    void changePiece(int x, int y, Piece piece) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException;
    void setDelegate(BoardDelegate delegate);

    /** @return the removed piece type **/
    Piece removePiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException;

}
