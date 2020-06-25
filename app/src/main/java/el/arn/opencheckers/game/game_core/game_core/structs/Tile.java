package el.arn.opencheckers.game.game_core.game_core.structs;

public class Tile {
    public final int x, y;
    public final Piece piece;

    public Tile(int x, int y, Piece piece) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }
}
