/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs;

import el.arn.ultimatecheckers.helpers.points.Point;

public class Move {

    public final Point captures, to;

    public Move(int xCapture, int yCapture, int xTo, int yTo) {
        captures = new Point(xCapture, yCapture);
        to = new Point(xTo, yTo);
    }

    public Move(int xTo, int yTo) {
        captures = null;
        to = new Point(xTo, yTo);
    }
}
