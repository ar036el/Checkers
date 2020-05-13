package el.arn.opencheckers.checkers_game.virtual_player;

import java.util.*;

public class MinimaxVirtualPlayerImpl<M extends GameState.Move> implements MinimaxVirtualPlayer<M> {

    @Override
    public M getMove(GameState gameState, int depthLimit) { //TODO too much "Game." noise
        Set<GameState.Move> possibleMoves = gameState.getPossibleMoves();
        if (possibleMoves.isEmpty()) {
            return null;
        }

        MinimaxNode<M> root = createGameTree(gameState, null, depthLimit, 0, null);
        root = getMinimaxNode(root);
        return (root != null) ? root.rootMove : null;
    }

    private MinimaxNode getMinimaxNode(MinimaxNode<M> root) {
        return getMinOrMaxNode(root, false);
    }

    private MinimaxNode getMinOrMaxNode(MinimaxNode<M> root, boolean isMin) {
        Set<MinimaxNode> nodes = new HashSet<>();
        for (MinimaxNode node : root.getChildren()) {
            nodes.add(getMinOrMaxNode(node, !isMin));
        }

        if (nodes.isEmpty()) {
            return root;
        } else if (nodes.size() == 1) {
            return nodes.iterator().next();
        }

        List<MinimaxNode> minOrMaxNodes = getMinOrMaxNodesByHeuristicValue(nodes, isMin);
        assert(!minOrMaxNodes.isEmpty());
        Collections.shuffle(minOrMaxNodes);
        return getMinOrMaxNodeBySecondaryHeuristicValue(minOrMaxNodes, isMin);
    }

    private List<MinimaxNode> getMinOrMaxNodesByHeuristicValue(Set<MinimaxNode> nodes, boolean isMin) {
        int minimaxFactor = isMin ? 1 : -1;
        List<MinimaxNode> minOrMaxNodes = new ArrayList<>();
        for (MinimaxNode node : nodes) {
            if (minOrMaxNodes.isEmpty() || node.heuristicValue * minimaxFactor < minOrMaxNodes.get(0).heuristicValue * minimaxFactor) {
                minOrMaxNodes.clear();
                minOrMaxNodes.add(node);
            } else if (node.heuristicValue == minOrMaxNodes.get(0).heuristicValue) {
                minOrMaxNodes.add(node);
            }
        }
        return minOrMaxNodes;
    }

    private MinimaxNode getMinOrMaxNodeBySecondaryHeuristicValue(List<MinimaxNode> nodes, boolean isMin) {
        int minimaxFactor = isMin ? 1 : -1;
        MinimaxNode minOrMaxNode = null;
        for (MinimaxNode node : nodes) {
            if (minOrMaxNode == null || node.secondaryHeuristicValue * minimaxFactor < minOrMaxNode.secondaryHeuristicValue * minimaxFactor) {
                minOrMaxNode = node;
            }
        }
        return minOrMaxNode;
    }

    private MinimaxNode createGameTree(GameState gameState, GameState.Move selectedMove, int maxLevel, int level, GameState.Move rootMove) {
        //System.out.println("node level " + level + " created: selectedMove= " + ((selectedMove != null) ? selectedMove.getContent() : "no move"));
        MinimaxNode node = new MinimaxNode(selectedMove, gameState.getHeuristicValue(), gameState.getSecondaryHeuristicValue(), rootMove);

        if (++level <= maxLevel) {
            for (Object item : gameState.getPossibleMoves()) {
                GameState.Move move = (GameState.Move) item;
                if (level == 1) {
                    rootMove = move;
                }
                node.addChild(createGameTree(gameState.makeAMove(move), move, maxLevel, level, rootMove));
            }
        }
        return node;
    }


}
