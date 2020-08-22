package el.arn.checkers.game.game_core.checkers_game.structs;

import el.arn.checkers.helpers.points.Point;

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
