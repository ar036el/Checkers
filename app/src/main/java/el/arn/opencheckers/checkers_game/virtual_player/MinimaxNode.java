package el.arn.opencheckers.checkers_game.virtual_player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class MinimaxNode<M extends GameState.Move> {
    //private final Game game;
    final M move;
    final M rootMove;
    final int heuristicValue;
    final int secondaryHeuristicValue;
    private final Set<MinimaxNode> children;

    public MinimaxNode(M move, int heuristicValue, int secondaryHeuristicValue, M rootMove) {
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
