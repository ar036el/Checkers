package el.arn.opencheckers.checkers_game.virtual_player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class MinimaxNode {
    //private final Game game;
    final GameState.Move move;
    final GameState.Move rootMove;
    final int heuristicValue;
    final int secondaryHeuristicValue;
    private final Set<MinimaxNode> children;

    public MinimaxNode(GameState.Move move, int heuristicValue, int secondaryHeuristicValue, GameState.Move rootMove) {
        this.heuristicValue = heuristicValue;
        this.secondaryHeuristicValue = secondaryHeuristicValue;
        this.move = move;
        this.rootMove = rootMove;
        children = new HashSet<>();
    }

    void addChild(MinimaxNode node) {
        children.add(node);
    }

    Set<MinimaxNode> getChildren() {
        return Collections.unmodifiableSet(children);
    }
}
