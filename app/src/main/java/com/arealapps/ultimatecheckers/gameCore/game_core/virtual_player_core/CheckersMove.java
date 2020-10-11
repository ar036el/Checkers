/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.gameCore.game_core.virtual_player_core;

import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Move;
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile;

public class CheckersMove implements GameState.Move {

    public final Tile fromTile;
    public final Move move;

    CheckersMove(Tile fromTile, Move move) {
        this.fromTile = fromTile;
        this.move = move;
    }
}
