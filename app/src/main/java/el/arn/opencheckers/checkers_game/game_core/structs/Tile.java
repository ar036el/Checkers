package el.arn.opencheckers.checkers_game.game_core.structs;

public class Tile {
    public final int x, y;
    public final Piece piece;

    public Tile(int x, int y, Piece piece) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }
}
