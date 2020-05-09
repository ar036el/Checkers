package el.arn.opencheckers.checkers_game.virtual_player;

import el.arn.opencheckers.checkers_game.game_core.Game;
import el.arn.opencheckers.checkers_game.game_core.exceptions.GameException;
import el.arn.opencheckers.checkers_game.game_core.implementations.GameImpl;
import el.arn.opencheckers.checkers_game.game_core.structs.*;

import java.util.HashSet;
import java.util.Set;

public class CheckersGameState implements GameState<CheckersMove> {

    private static final int PIECE_IS_PAWN_VALUE = 1;
    private static final int PIECE_IS_KING_VALUE = 3;
    private static final int PIECE_IS_LOCATED_ON_BOARD_EDGE_VALUE = 2;
    private static final int PIECE_IS_LOCATED_NEXT_TO_BOARD_EDGE_VALUE = 1;
    private static final int PIECE_IS_LOCATED_ON_REST_OF_BOARD_VALUE = 0;


    private final Game checkersGame;
    private final int boardSize;
    private final Set<CheckersMove> possibleMoves;
    int heuristicValue;
    int secondaryHeuristicValue;
    Player player;
    Player opponent;

    public CheckersGameState(Game checkersGame, Player player) { //TODO Impl here
        this.checkersGame = checkersGame;
        boardSize = checkersGame.getBoardSize();
        possibleMoves = new HashSet<>();
        this.player = player;
        opponent = (player == Player.White) ? Player.Black : Player.White;
        calculateHeuristicValuesForPlayer(this.player);
        calculateHeuristicValuesForPlayer(opponent);
        createPossibleMoves();
    }

    void createPossibleMoves() {
        for (Tile tile : checkersGame.getAvailablePieces()) {
            for (el.arn.opencheckers.checkers_game.game_core.structs.Move checkersMove : checkersGame.getAvailableMovesForPiece(tile.x, tile.y)) {
                assert(!checkersGame.getAvailableMovesForPiece(tile.x, tile.y).isEmpty());
                possibleMoves.add(new CheckersMove(tile, checkersMove));
            }
        }
    }

    @Override
    public GameState<CheckersMove> makeAMove(CheckersMove move) {
        if (!possibleMoves.contains(move)) {
            throw new InternalError();
        }
        Game gameCopy = new GameImpl((GameImpl) checkersGame);
        try {
            gameCopy.makeAMove(move.tile.x, move.tile.y, move.move.to.x, move.move.to.y);
        } catch (GameException e) {
            throw new InternalError();
        }
        return new CheckersGameState(gameCopy, player);
    }

    @Override
    public Set<CheckersMove> getPossibleMoves() {
        return possibleMoves;
    }

    @Override
    public int getHeuristicValue() {
        return heuristicValue;
    }

    @Override
    public int getSecondaryHeuristicValue() {
        return secondaryHeuristicValue;
    }

    private void calculateHeuristicValuesForPlayer(Player player) {
        int incOrDecFactor = (this.player == player) ? 1 : -1;
        for (Tile tile : checkersGame.getAllPiecesOfPlayer(player)) {
            heuristicValue += incOrDecFactor * getHeuristicValue(tile.piece.getType());
            secondaryHeuristicValue += incOrDecFactor * getSecondaryHeuristicValue(tile.x, tile.y);
        }
    }

    private int getHeuristicValue(Piece.Type pieceType) {
        return (pieceType == Piece.Type.King) ? PIECE_IS_KING_VALUE : PIECE_IS_PAWN_VALUE;
    }

    private int getSecondaryHeuristicValue(int x, int y) {
        if (x == 0 || x == boardSize-1 || y == 0 || y == boardSize-1) {
            return PIECE_IS_LOCATED_ON_BOARD_EDGE_VALUE;
        } else if (x == 1 || x == boardSize-2 || y == 1 || y == boardSize-2) {
            return PIECE_IS_LOCATED_NEXT_TO_BOARD_EDGE_VALUE;
        } else {
            return PIECE_IS_LOCATED_ON_REST_OF_BOARD_VALUE;
        }
    }
}

