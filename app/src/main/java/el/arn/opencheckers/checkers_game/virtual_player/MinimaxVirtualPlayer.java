package el.arn.opencheckers.checkers_game.virtual_player;

public interface MinimaxVirtualPlayer<M extends GameState.Move> {
    M getMove(GameState gameState, int depthLimit);

    /** @return {@code true} if computation is not in process, {@code false} otherwise **/
    boolean cancelComputationProcess();
}

