/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore.game_core.checkers_game;

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.PointIsOutOfBoardBoundsException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.TileIsNotPlayableException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Piece;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Player;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile;

import java.util.Set;

public interface ReadableBoard {
    int getBoardSize();
    Integer getStartingRows();
    Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    Set<Tile> getAllPiecesForPlayer(Player player);
    Set<Tile> getAllPiecesInBoard();
}
