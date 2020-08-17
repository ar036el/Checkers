package el.arn.opencheckers.game.game_core.virtual_player;

import java.util.*;

public class MinimaxVirtualPlayerImpl<M extends GameState.Move> implements MinimaxVirtualPlayer<M> {

    private boolean isCancelled = false;
    private boolean isComputing = false;

    private MinimaxNode cancelledToken = new MinimaxNode(null, 0, 0, null);

    @Override
    public M getMove(GameState gameState, int depthLimit) { //TODO too much "Game." noise
        Set<GameState.Move> possibleMoves = gameState.getPossibleMoves();
        if (possibleMoves.isEmpty()) { return null; }

        isComputing = true;
        MinimaxNode<M> root = createGameTree(gameState, null, depthLimit, 0, null);
        if (isCancelled) {
            return null; }
        root = getMinimaxNode(root);
        if (isCancelled) {
            return null; }
        isComputing = false;
        return (root != null && !isCancelled) ? root.rootMove : null;
    }

    @Override
    public boolean cancelComputationProcess() {
        if (!isComputing) {
            return false;
        }
        isCancelled = true;
        return true;
    }

    private MinimaxNode getMinimaxNode(MinimaxNode<M> root) {
        return getMinOrMaxNode(root, false);
    }

    private MinimaxNode getMinOrMaxNode(MinimaxNode<M> root, boolean isMin) {
        if (isCancelled) { return null; }
        Set<MinimaxNode> nodes = new HashSet<>();
        for (MinimaxNode node : root.getChildren()) {
            nodes.add(getMinOrMaxNode(node, !isMin));
            if (isCancelled) { return null; }
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
        if (isCancelled) { return null; }
        MinimaxNode node = new MinimaxNode(selectedMove, gameState.getHeuristicValue(), gameState.getSecondaryHeuristicValue(), rootMove);

        if (++level <= maxLevel) {
            for (Object item : gameState.getPossibleMoves()) {
                GameState.Move move = (GameState.Move) item;
                if (level == 1) {
                    rootMove = move;
                }
                MinimaxNode child = createGameTree(gameState.makeAMove(move), move, maxLevel, level, rootMove);
                if (isCancelled) { return null; }
                node.addChild(child);
            }
        }
        return node;
    }


}
