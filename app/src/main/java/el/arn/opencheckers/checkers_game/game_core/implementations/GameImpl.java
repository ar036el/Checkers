package el.arn.opencheckers.checkers_game.game_core.implementations;

import el.arn.opencheckers.checkers_game.game_core.*;
import el.arn.opencheckers.checkers_game.game_core.configurations.ConfigDelegate;
import el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig;
import el.arn.opencheckers.checkers_game.game_core.exceptions.*;
import el.arn.opencheckers.checkers_game.game_core.structs.*;
import java.util.*;
import static el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig.KingBehaviourOptions.*;

public class GameImpl implements Game, BoardDelegate, ConfigDelegate {

    private static int MIN_JUMP_DISTANCE = 1;
    private static int MIN_CAPTURE_DISTANCE = 2;

    private final PlayableBoard board;
    private final int boardSize;
    private final GameLogicConfig config;
    private GameLogicDelegate delegate;

    private Player currentPlayer;
    private Player opponentPlayer;
    private Player winner = null;

    private AvailableMoves availableMovesForCurrentPlayer;
    private Move lastMove = null;

    private boolean flagIsExtraTurn = false;
    private boolean flagCheckingIfEligibleForExtraTurn = false;
    private boolean flagBoardWasChangedUnexpectedly = false;

    public GameImpl(GameImpl source) {
        board = new BoardImpl((BoardImpl) source.board);
        boardSize = source.boardSize;
        config = source.config;
        delegate = null;

        currentPlayer = source.currentPlayer;
        opponentPlayer = source.opponentPlayer;
        winner = source.winner; //TOdo needs to be null?

        availableMovesForCurrentPlayer = source.availableMovesForCurrentPlayer;
        lastMove = source.lastMove;

        flagIsExtraTurn = source.flagIsExtraTurn;
        flagCheckingIfEligibleForExtraTurn = source.flagCheckingIfEligibleForExtraTurn;
        flagBoardWasChangedUnexpectedly = source.flagBoardWasChangedUnexpectedly;
    }

    public GameImpl(GameLogicConfig config, PlayableBoard board, Player startingPlayer, GameLogicDelegate delegate) {
        this.config = config;
        config.setDelegate(this);
        this.board = board;
        board.setDelegate(this);
        this.delegate = delegate;
        boardSize = board.getBoardSize();
        currentPlayer = startingPlayer;
        opponentPlayer = (startingPlayer == Player.White) ? Player.Black : Player.White;

        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        tryToEndGame();
    }

    @Override
    public Tile makeAMove(int xFrom, int yFrom, int xTo, int yTo) throws GameException {
        if (flagBoardWasChangedUnexpectedly) {
            refreshGameBecauseSomethingChangedWhileWaitingForNextMove();
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
    public Player getWinner() {
        return winner;
    }

    @Override
    public void setDelegate(GameLogicDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getBoardSize() {
        return boardSize;
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
        refreshGameBecauseSomethingChangedWhileWaitingForNextMove();
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

    private void refreshGameBecauseSomethingChangedWhileWaitingForNextMove() {
        //TODO if this happened while on extra turn, it loses the turn. can you do something with this?
        turnAnyPawnThatIsUnexpectedlyOnKingsRowIntoKing();
        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        tryToEndGame();
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
            setWinnerAndNotifyDelegate(opponentPlayer);
        } else if (whitePieces == 0) {
            setWinnerAndNotifyDelegate(Player.Black);
        } else if (blackPieces == 0) {
            setWinnerAndNotifyDelegate(Player.White);
        }
    }

    private void endTurn() {
        turnPawnIntoKingIfReachedTheKingsRow(lastMove.to.x, lastMove.to.y);
        switchPlayer();
        availableMovesForCurrentPlayer = getAvailableMovesForPlayer(currentPlayer);
        flagIsExtraTurn = false;
    }

    private void setWinnerAndNotifyDelegate(Player winner) {
        this.winner = winner;
        if (delegate != null) {
            delegate.gameHasEnded(winner);
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
        if (move.capture != null) {
            captured = new Tile(move.capture.x, move.capture.y, board.getPiece(move.capture.x, move.capture.y));
            capture(xFrom, yFrom, move.capture.x, move.capture.y, xTo, yTo);
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
        if (lastMove.capture != null) {
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
            if (move.capture == null) {
                toRemove.add(move);
            }
        }
        moves.removeAll(toRemove);

    }

    private boolean containsACaptureMove(Set<Move> moves) {
        for (Move move : moves) {
            if (move.capture != null) {
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
        } else if (config.getKingBehaviour() == NoFlyingKings) {
            return getAvailableMovesForPiece(x, y, MIN_JUMP_DISTANCE, MIN_CAPTURE_DISTANCE, true, true, true, piece.getTeam());
        } else if (config.getKingBehaviour() == FlyingKings) {
            return getAvailableMovesForPiece(x, y, boardSize, boardSize, false, true, true, piece.getTeam());
        } else if (config.getKingBehaviour() == LandsRightAfterCapture) {
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
