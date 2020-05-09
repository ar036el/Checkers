package el.arn.opencheckers.checkers_game.virtual_player;

public interface MinimaxVirtualPlayer {
    GameState.Move getMove(GameState gameState, int depthLimit);
}

