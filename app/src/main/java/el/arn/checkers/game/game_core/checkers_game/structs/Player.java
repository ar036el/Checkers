package el.arn.checkers.game.game_core.checkers_game.structs;

public enum Player {
    White, Black;

    public Player opponent() {
        return (this == White) ? Black : White;
    }
}