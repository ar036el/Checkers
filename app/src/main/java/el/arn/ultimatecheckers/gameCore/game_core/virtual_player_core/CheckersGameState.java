/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore.game_core.virtual_player_core;

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.Game;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.GameException;

import java.util.HashSet;
import java.util.Set;

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Piece;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Player;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile;

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

    private void createPossibleMoves() {
        for (Tile tile : checkersGame.getAvailablePieces()) {
            for (el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Move checkersMove : checkersGame.getAvailableMovesForPiece(tile.x, tile.y)) {
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
        Game gameClone = checkersGame.clone();
        try {
            gameClone.makeAMove(move.fromTile.x, move.fromTile.y, move.move.to.x, move.move.to.y);
        } catch (GameException e) {
            throw new InternalError();
        }
        return new CheckersGameState(gameClone, player);
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
        for (Tile tile : checkersGame.getAllPiecesForPlayer(player)) {
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

