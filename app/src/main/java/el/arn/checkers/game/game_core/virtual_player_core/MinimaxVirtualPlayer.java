package el.arn.checkers.game.game_core.virtual_player_core;

public interface MinimaxVirtualPlayer<M extends GameState.Move> {
    M getMove(GameState gameState, int depthLimit);

    /** @return {@code true} if computation is not in process, {@code false} otherwise **/
    boolean cancelComputationProcess();
}
