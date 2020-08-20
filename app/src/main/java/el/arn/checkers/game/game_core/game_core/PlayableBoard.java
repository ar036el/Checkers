package el.arn.checkers.game.game_core.game_core;

import org.jetbrains.annotations.NotNull;

import el.arn.checkers.game.game_core.game_core.exceptions.*;
import el.arn.checkers.game.game_core.game_core.structs.Piece;

public interface PlayableBoard extends ReadableBoard, Cloneable {
    void addPiece(int x, int y, Piece piece) throws TileIsAlreadyOccupiedException, PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    void movePiece(int xFrom, int yFrom, int xTo, int yTo) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, TileIsAlreadyOccupiedException, PieceWasNotSelectedException;
    void changePiece(int x, int y, Piece piece) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException;
    void setListener(BoardListener Listener);

    @NotNull
    PlayableBoard clone();

    /** @return the removed piece type **/
    Piece removePiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException;

}
