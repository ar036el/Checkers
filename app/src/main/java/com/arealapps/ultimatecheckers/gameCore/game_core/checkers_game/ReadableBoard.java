/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game;

import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.PointIsOutOfBoardBoundsException;
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.TileIsNotPlayableException;
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Piece;
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Player;
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile;

import java.util.Set;

public interface ReadableBoard {
    int getBoardSize();
    Integer getStartingRows();
    Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException;
    Set<Tile> getAllPiecesForPlayer(Player player);
    Set<Tile> getAllPiecesInBoard();
}
