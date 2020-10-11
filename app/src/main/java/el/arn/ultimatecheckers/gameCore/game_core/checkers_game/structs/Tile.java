/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs;

public class Tile {
    public final int x, y;
    public final Piece piece;

    public Tile(int x, int y, Piece piece) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }
}
