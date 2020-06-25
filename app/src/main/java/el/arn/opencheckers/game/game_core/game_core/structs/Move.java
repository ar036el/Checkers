package el.arn.opencheckers.game.game_core.game_core.structs;

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
