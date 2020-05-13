package el.arn.opencheckers.checkers_game.game_core.structs;

public class Move {

    public final Point capture, to;

    public Move(int xCapture, int yCapture, int xTo, int yTo) {
        capture = new Point(xCapture, yCapture);
        to = new Point(xTo, yTo);
    }

    public Move(int xTo, int yTo) {
        capture = null;
        to = new Point(xTo, yTo);
    }
}
