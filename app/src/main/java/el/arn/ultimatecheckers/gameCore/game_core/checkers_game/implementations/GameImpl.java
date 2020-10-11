/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore.game_core.checkers_game.implementations;

import org.jetbrains.annotations.NotNull;

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.configurations.ConfigListener;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.configurations.GameLogicConfig;
import el.arn.ultimatecheckers.helpers.points.Point;

import java.util.*;

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.BoardListener;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.Game;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.GameLogicListener;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.PlayableBoard;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.ReadableBoard;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.BoardException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.CannotPassTurn;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.GameException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.GameHasAlreadyEndedException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.IllegalMoveException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.NotWalkingDiagonallyException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.PieceDoesNotBelongToCurrentPlayerException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.PieceWasNotSelectedException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.PointIsOutOfBoardBoundsException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.TileIsAlreadyOccupiedException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.exceptions.TileIsNotPlayableException;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Move;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Piece;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Player;
import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.structs.Tile;

public class GameImpl implements Game, BoardListener, ConfigListener {

    private static int MIN_JUMP_DISTANCE = 1;
    private static int MIN_CAPTURE_DISTANCE = 2;

    private final PlayableBoard board;
    private final int boardSize;
    private final GameLogicConfig config;
    private GameLogicListener Listener;

    private Player currentPlayer;
    private Player opponentPlayer;
    private Player winner = null;

    private AvailableMoves availableMovesForCurrentPlayer;
    private Move lastMove = null;

    private boolean flagIsExtraTurn = false;
    private boolean flagCheckingIfEligibleForExtraTurn = false;
    private boolean flagBoardWasChangedUnexpectedly = false;

    @NotNull
    @Override
    public Game clone() {
        return new GameImpl(this);
    }


    private GameImpl(GameImpl source) {
        board = source.board.clone();
        boardSize = source.boardSize;
        config = source.config;
        Listener = null;

        currentPlayer = source.currentPlayer;
        opponentPlayer = source.opponentPlayer;
        winner = source.winner; //TOdo needs to be null?

        availableMovesForCurrentPlayer = source.availableMovesForCurrentPlayer;
        lastMove = source.lastMove;

        flagIsExtraTurn = source.flagIsExtraTurn;
        flagCheckingIfEligibleForExtraTurn = source.flagCheckingIfEligibleForExtraTurn;
        flagBoardWasChangedUnexpectedly = source.flagBoardWasChangedUnexpectedly;
    }

    public GameImpl(GameLogicConfig config, PlayableBoard board, Player startingPlayer, GameLogicListener Listener) {
        this.config = config;
        config.addListener(this);
        this.board = board;
        board.setListener(this);
        this.Listener = Listener;
        boardSize = board.getBoardSize();
        currentPlayer = startingPlayer;
        opponentPlayer = (startingPlayer == Player.White) ? Player.Black : Player.White;

        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        tryToEndGame();
    }

    @Override
    public Tile makeAMove(int xFrom, int yFrom, int xTo, int yTo) throws GameException {
        if (flagBoardWasChangedUnexpectedly) {
            refreshGame();
        }
        checkForExplicitIllegalMoves(xFrom, yFrom, xTo, yTo);
        Tile captured = tryToMakeAMove(xFrom, yFrom, xTo, yTo);

        if (!tryToGetAnExtraTurn()) {
            endTurn();
        }
        tryToEndGame();
        flagBoardWasChangedUnexpectedly = false;
        return captured;
    }

    @Override
    public void passTurn() throws CannotPassTurn {
        if (isExtraTurn() && !config.getIsCapturingMandatory()) {
            endTurn();
        } else {
            throw new CannotPassTurn();
        }
    }

    @Override
    public boolean canPassExtraTurn() {
        return !config.getIsCapturingMandatory();
    }

    @Override
    public boolean isExtraTurn() {
        return flagIsExtraTurn;
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public GameLogicConfig getConfig() {
        return config;
    }

    @Override
    public Player getWinner() {
        return winner;
    }

    @Override
    public void setListener(GameLogicListener Listener) {
        this.Listener = Listener;
    }

    @Override
    public int getBoardSize() {
        return boardSize;
    }

    @Override
    public Integer getStartingRows() {
        return board.getStartingRows();
    }

    @Override
    public Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException {
        return board.getPiece(x, y);
    }

    @Override
    public Set<Tile> getAllPiecesForPlayer(Player player) {
        return board.getAllPiecesForPlayer(player);
    }

    @Override
    public Set<Tile> getAllPiecesInBoard() {
        return board.getAllPiecesInBoard();
    }

    @Override
    public Set<Tile> getAvailablePieces() {
        return availableMovesForCurrentPlayer.getPieces();
    }

    @Override
    public Set<Move> getAvailableMovesForPiece(int x, int y) {
        return availableMovesForCurrentPlayer.getMoves(x, y);
    }

    @Override
    public void reloadAvailableMoves() {
        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer); //TODO not tested!!
    }

    @Override
    public void boardHasChanged() {
        flagBoardWasChangedUnexpectedly = true;
    }

    @Override
    public void configurationHasChanged() {
        refreshGame();
    }

    @Override
    public void refreshGame() {
        //TODO if this happened while on extra turn, it loses the turn. can you do something with this?
        turnAnyPawnThatIsUnexpectedlyOnKingsRowIntoKing();
        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        tryToEndGame();
    }

    private boolean tryToGetAnExtraTurn() {
        AvailableMoves availableMovesIfExtraTurn = getAvailableMovesIfExtraTurn();
        if (availableMovesIfExtraTurn != null) {
            availableMovesForCurrentPlayer = availableMovesIfExtraTurn;
            flagIsExtraTurn = true;
            return true;
        }
        return false;
    }

    private void turnAnyPawnThatIsUnexpectedlyOnKingsRowIntoKing() {
        for (int x = 0; x < boardSize; x++) {
            if (x % 2 == 0) {
                turnPawnIntoKingIfReachedTheKingsRow(x, 0);
            } else {
                turnPawnIntoKingIfReachedTheKingsRow(x, boardSize - 1);
            }
        }
    }

    private void tryToEndGame() {
        if (winner != null) {
            return;
        }
        int whitePieces = board.getAllPiecesForPlayer(Player.White).size();
        int blackPieces = board.getAllPiecesForPlayer(Player.Black).size();

        if (whitePieces + blackPieces <= 0) {
            throw new InternalError();
        } else if (availableMovesForCurrentPlayer.isEmpty()) {
            setWinnerAndNotifyListener(opponentPlayer);
        } else if (whitePieces == 0) {
            setWinnerAndNotifyListener(Player.Black);
        } else if (blackPieces == 0) {
            setWinnerAndNotifyListener(Player.White);
        }
    }

    private void endTurn() {
        turnPawnIntoKingIfReachedTheKingsRow(lastMove.to.x, lastMove.to.y);
        switchPlayer();
        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        flagIsExtraTurn = false;
    }

    private void setWinnerAndNotifyListener(Player winner) {
        this.winner = winner;
        if (Listener != null) {
            Listener.gameHasEnded(winner);
        }
    }

    private void turnPawnIntoKingIfReachedTheKingsRow(int x, int y) {
        try {
            Piece piece = board.getPiece(x, y);
            if (y == boardSize - 1 && piece == Piece.WhitePawn) {
                board.changePiece(x, y, Piece.WhiteKing);
            } else if (y == 0 && piece == Piece.BlackPawn) {
                board.changePiece(x, y, Piece.BlackKing);
            }
        } catch (BoardException e) {
            throw new InternalError();
        }
    }

    private void checkForExplicitIllegalMoves(int xFrom, int yFrom, int xTo, int yTo) throws GameException {
        if (getWinner() != null) {
            throw new GameHasAlreadyEndedException();
        }

        Piece piece = board.getPiece(xFrom, yFrom);
        if (piece == null) {
            throw new PieceWasNotSelectedException();
        } else if (board.getPiece(xTo, yTo) != null) {
            throw new TileIsAlreadyOccupiedException();
        } else if (piece.getTeam() != currentPlayer) {
            throw new PieceDoesNotBelongToCurrentPlayerException();
        } else if (Math.abs(xTo - xFrom) != Math.abs(yTo - yFrom)) {
            throw new NotWalkingDiagonallyException();
        }
    }

    private boolean canPawnCaptureBackwards() {
        switch (config.getCanPawnCaptureBackwards()) {
            case Always:
                return true;
            case OnlyWhenMultiCapture:
                return flagCheckingIfEligibleForExtraTurn;
            case Never:
                return false;
            default:
                throw new InternalError();
        }
    }

    private Move getMoveIfAvailable(int xFrom, int yFrom, int xTo, int yTo) {
        if (availableMovesForCurrentPlayer.getMoves(xFrom,yFrom) != null) {
            for (Move move : availableMovesForCurrentPlayer.getMoves(xFrom,yFrom)) {
                if (move.to.x == xTo && move.to.y == yTo) {
                    return move;
                }
            }
        }
        return null;
    }

    private Tile tryToMakeAMove(int xFrom, int yFrom, int xTo, int yTo) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException, TileIsAlreadyOccupiedException, IllegalMoveException, CannotPassTurn {
        Move move = getMoveIfAvailable(xFrom, yFrom, xTo, yTo);
        if (move == null) {
            throw new IllegalMoveException();
        }
        Tile captured = null;
        if (move.captures != null) {
            captured = new Tile(move.captures.x, move.captures.y, board.getPiece(move.captures.x, move.captures.y));
            capture(xFrom, yFrom, move.captures.x, move.captures.y, xTo, yTo);
        } else {
            jump(xFrom, yFrom, xTo, yTo);
        }
        lastMove = move;
        return captured;
    }


    private void jump(int xFrom, int yFrom, int xTo, int yTo) throws TileIsAlreadyOccupiedException, PieceWasNotSelectedException, PointIsOutOfBoardBoundsException, TileIsNotPlayableException {
        board.movePiece(xFrom, yFrom, xTo, yTo);
    }

    private void capture(int xFrom, int yFrom, int xCapture, int yCapture, int xTo, int yTo) throws TileIsAlreadyOccupiedException, PieceWasNotSelectedException, PointIsOutOfBoardBoundsException, TileIsNotPlayableException {
        if (getMoveIfAvailable(xFrom, yFrom, xTo, yTo) == null)
            return;
        board.movePiece(xFrom, yFrom, xTo, yTo);
        board.removePiece(xCapture, yCapture);
    }

    private void switchPlayer() {
        if (currentPlayer == Player.White) {
            currentPlayer = Player.Black;
            opponentPlayer = Player.White;
        } else {
            currentPlayer = Player.White;
            opponentPlayer = Player.Black;
        }
    }

    private AvailableMoves getAvailableMovesIfExtraTurn() {
        if (lastMove.captures != null) {
            flagCheckingIfEligibleForExtraTurn = true;
            Set<Move> availableMovesForLastPlayedPiece = getAvailableMovesForPieceWithoutMandatoryCapturingFilter(lastMove.to.x, lastMove.to.y);
            flagCheckingIfEligibleForExtraTurn = false;

            if (availableMovesForLastPlayedPiece != null) {
                boolean eligibleForExtraTurn = containsACaptureMove(availableMovesForLastPlayedPiece);
                if (eligibleForExtraTurn) {
                    filterNonCaptureMoves(availableMovesForLastPlayedPiece);
                    AvailableMoves availableMoves = new AvailableMoves(boardSize);

                    availableMoves.put(lastMove.to.x, lastMove.to.y, availableMovesForLastPlayedPiece);
                    return availableMoves;
                }
            }
        }
        return null;
    }


    private void filterNonCaptureMoves(Set<Move> moves) {
        Set<Move> toRemove= new HashSet<>();
        for (Move move : moves) {
            if (move.captures == null) {
                toRemove.add(move);
            }
        }
        moves.removeAll(toRemove);

    }

    private boolean containsACaptureMove(Set<Move> moves) {
        for (Move move : moves) {
            if (move.captures != null) {
                return true;
            }
        }
        return false;
    }

    private AvailableMoves getAvailableMovesForPlayer(Player player) {
        AvailableMoves availableMoves = new AvailableMoves(boardSize);
        Set<Tile> tiles = board.getAllPiecesForPlayer(player);
        boolean onlyCaptureMovesAllowed = false;

        for (Tile tile : tiles) {
            Set<Move> availableMovesPerPiece = getAvailableMovesForPieceWithoutMandatoryCapturingFilter(tile.x, tile.y);
            if (availableMovesPerPiece == null) {
                continue;
            }
            availableMoves.put(tile.x, tile.y, availableMovesPerPiece);
            if (config.getIsCapturingMandatory() && containsACaptureMove(availableMovesPerPiece)) {
                onlyCaptureMovesAllowed = true;
            }
        }
        if (onlyCaptureMovesAllowed) {
            filterNonCaptureMoves(availableMoves);
        }
        return availableMoves;
    }

    private void filterNonCaptureMoves(AvailableMoves availableMoves) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Set<Move> availableMovesPerPiece = availableMoves.getMoves(i, j);
                if (availableMovesPerPiece == null) {
                    continue;
                }
                filterNonCaptureMoves(availableMovesPerPiece);
                if (availableMovesPerPiece.isEmpty()) {
                    availableMoves.remove(i, j);
                }
            }
        }
    }

    private Set<Move> getAvailableMovesForPieceWithoutMandatoryCapturingFilter(int x, int y) {
        Piece piece;
        try {
            piece = board.getPiece(x, y);
        } catch (GameException e) {
            throw new InternalError();
        }
        if (piece.getType() == Piece.Type.Pawn) {
            return getAvailableMovesForPiece(x, y, MIN_JUMP_DISTANCE, MIN_CAPTURE_DISTANCE, true, false, canPawnCaptureBackwards(), piece.getTeam());
        } else if (config.getKingBehaviour() == GameLogicConfig.KingBehaviourOptions.NoFlyingKings) {
            return getAvailableMovesForPiece(x, y, MIN_JUMP_DISTANCE, MIN_CAPTURE_DISTANCE, true, true, true, piece.getTeam());
        } else if (config.getKingBehaviour() == GameLogicConfig.KingBehaviourOptions.FlyingKings) {
            return getAvailableMovesForPiece(x, y, boardSize, boardSize, false, true, true, piece.getTeam());
        } else if (config.getKingBehaviour() == GameLogicConfig.KingBehaviourOptions.LandsRightAfterCapture) {
            return getAvailableMovesForPiece(x, y, boardSize, boardSize, true, true, true, piece.getTeam());
        } else {
            throw new InternalError();
        }
    }

    private Set<Move> getAvailableMovesForPiece(int xFrom,
                                                int yFrom,
                                                int maxJumpDistance,
                                                int maxCaptureDistance,
                                                boolean mustLandAfterCapture,
                                                boolean canJumpBackwards,
                                                boolean canCaptureBackwards,
                                                Player team) {
        Set<Move> availableMovesForPiece = new HashSet<>();

        for (DiagonalDirection direction : DiagonalDirection.values()) {
            Point captured = null;
            int distance;
            for (distance = 1; distance < boardSize; distance++) {
                try {
                    int xTo = xFrom + distance * direction.xFactor;
                    int yTo = yFrom + distance * direction.yFactor;
                    Piece possibleCapture = board.getPiece(xTo, yTo);

                    if (possibleCapture != null) {
                        if (captured == null && possibleCapture.getTeam() != team) {
                            captured = new Point(xTo, yTo);
                        } else {
                            break;
                        }
                    } else {
                        if (captured == null && distance <= maxJumpDistance
                                && !(!canJumpBackwards && direction.isBackwardsFor(team)) ) {
                            availableMovesForPiece.add(new Move(xTo, yTo));
                        }
                        else if (captured != null && distance <= maxCaptureDistance
                                && !((mustLandAfterCapture && Math.abs(xTo - captured.x) > 1)
                                || (!canCaptureBackwards && direction.isBackwardsFor(team))) ) {
                            availableMovesForPiece.add(new Move(captured.x, captured.y, xTo, yTo));
                        }
                    }
                } catch (TileIsNotPlayableException e) {
                    throw new InternalError();
                } catch (PointIsOutOfBoardBoundsException e) {
                    break;
                }
            }
        }

        return !availableMovesForPiece.isEmpty() ? availableMovesForPiece : null;
    }

    @Override
    public ReadableBoard getBoard() {
        return board;
    }


    private class AvailableMoves {
        private class Item {
            final Tile piece;
            final Set<Move> moves;
            Item(Tile piece, Set<Move> moves) {
                this.piece = piece; this.moves = moves;
            }
        }
        private Item[][] _array;
        private Set<Tile> _set;

        AvailableMoves(int size) {
            _array = new Item[size][size];
            _set = new HashSet<>();
        }
        void put(int x, int y, Set<Move> availableMovesForPiece) {
            try {
                if (availableMovesForPiece == null) { throw new InternalError(); }
                Tile tile = null;
                tile = new Tile(x, y, board.getPiece(x,y));
                Item item = new Item(tile, availableMovesForPiece);
                remove(x, y);
                _set.add(tile);
                _array[x][y] = item;
            } catch (BoardException e) {
                throw new InternalError();
            }
        }
        void remove(int x, int y) {
            if (_array[x][y] == null) {
                return;
            }
            _set.remove(_array[x][y].piece);
            _array[x][y] = null;
        }
        boolean isEmpty() {
            return _set.isEmpty();
        }
        Set<Tile> getPieces() {
            return Collections.unmodifiableSet(_set);
        }
        Set<Move> getMoves(int x, int y) {
            return (_array[x][y] != null) ? _array[x][y].moves : null;
        }
    }

    private enum DiagonalDirection {
        TopRight(-1, 1, false),
        TopLeft(1, 1, false),
        BottomRight(-1, -1, true),
        BottomLeft(1, -1, true);
        final int xFactor, yFactor;
        private final boolean _isBackwardsForWhite;
        DiagonalDirection(int xFactor, int yFactor, boolean isBackwardsForWhite) {
            this.xFactor = xFactor;
            this.yFactor = yFactor;
            this._isBackwardsForWhite = isBackwardsForWhite;
        }
        boolean isBackwardsFor(Player player) {
            return (player == Player.White) == _isBackwardsForWhite;
        }
    }

}
