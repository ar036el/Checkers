package el.arn.opencheckers.game.game_core.game_core.structs;

public enum Player {
    White, Black;

    public Player opponent() {
        return (this == White) ? Black : White;
    }
}
