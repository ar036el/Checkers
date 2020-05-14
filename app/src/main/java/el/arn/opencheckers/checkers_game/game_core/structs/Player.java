package el.arn.opencheckers.checkers_game.game_core.structs;

public enum Player {
    White, Black;

    public Player opponent() {
        return (this == White) ? Black : White;
    }
}
