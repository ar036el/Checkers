package el.arn.checkers.game.game_core.game_core.implementations;

import org.jetbrains.annotations.NotNull;

import el.arn.checkers.game.game_core.game_core.*;
import el.arn.checkers.game.game_core.game_core.exceptions.*;
import el.arn.checkers.game.game_core.game_core.structs.Piece;
import el.arn.checkers.game.game_core.game_core.structs.Player;
import el.arn.checkers.game.game_core.game_core.structs.Tile;

import java.util.*;

public class BoardImpl implements PlayableBoard {

    private final int boardSize;
    private final Tile[][] tiles;
    private final Set<Tile> whitePlayers = new HashSet<>();
    private final Set<Tile> blackPlayers = new HashSet<>();
    private BoardListener Listener = null;

    @NotNull
    @Override
    public BoardImpl clone() {
        return new BoardImpl(this);
    }

    private BoardImpl(BoardImpl source) {
        boardSize = source.boardSize;
        tiles = new Tile[boardSize][boardSize];
        for (int i = 0; i < source.tiles.length; i++) {
            System.arraycopy(source.tiles[i], 0, tiles[i], 0, source.tiles[i].length);
        }
        whitePlayers.addAll(source.whitePlayers);
        blackPlayers.addAll(source.blackPlayers);
        Listener = null;
    }

    public BoardImpl(int boardSize) {
        this.boardSize = boardSize;
        if (!isEven(boardSize)) {
            throw new InternalError();
        }
        tiles = new Tile[boardSize][boardSize];
    }

    public BoardImpl(int boardSize, int startingRowsForEachPlayer) {
        this.boardSize = boardSize;
        if (!isEven(boardSize)) {
            throw new InternalError();
        }
        tiles = new Tile[boardSize][boardSize];

        createStartingRows(startingRowsForEachPlayer);
    }

    @Override
    public void addPiece(int x, int y, Piece piece) throws TileIsAlreadyOccupiedException, PointIsOutOfBoardBoundsException, TileIsNotPlayableException {
        if (getPiece(x, y) != null) {
            throw new TileIsAlreadyOccupiedException();
        }
        Tile tile = new Tile(x, y, piece);
        tiles[x][y] = tile;
        getTilesFor(piece.getTeam()).add(tile);
        notifyListenerBoardHasChanged();
    }

    @Override
    public Piece removePiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException {
        Piece piece = getPiece(x, y);
        if (piece == null) {
            throw new PieceWasNotSelectedException();
        }
        boolean successful = getTilesFor(piece.getTeam()).remove(tiles[x][y]);
        if (!successful) {
            throw new InternalError();
        }
        tiles[x][y] = null;
        notifyListenerBoardHasChanged();

        return piece;
    }

    @Override
    public void movePiece(int xFrom, int yFrom, int xTo, int yTo) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, TileIsAlreadyOccupiedException, PieceWasNotSelectedException {
        Piece piece = getPiece(xFrom, yFrom);
        if (getPiece(xTo, yTo) != null) {
            throw new TileIsAlreadyOccupiedException();
        }
        removePiece(xFrom, yFrom);
        addPiece(xTo, yTo, piece);
    }

    @Override
    public void changePiece(int x, int y, Piece piece) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException, PieceWasNotSelectedException {
        removePiece(x, y);
        try {
            addPiece(x, y, piece);
        } catch (TileIsAlreadyOccupiedException e) {
            throw new InternalError();
        }

    }

    @Override
    public Piece getPiece(int x, int y) throws PointIsOutOfBoardBoundsException, TileIsNotPlayableException {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize)
            throw new PointIsOutOfBoardBoundsException();
        if ((!isEven(y) && isEven(x)) || (isEven(y) && !isEven(x))) {
            throw new TileIsNotPlayableException();
        }
        return (tiles[x][y] != null) ? tiles[x][y].piece : null;
    }

    @Override
    public int getBoardSize() {
        return boardSize;
    }

    @Override
    public void setListener(BoardListener Listener) {
        this.Listener = Listener;
    }

    @Override
    public Set<Tile> getAllPiecesForPlayer(Player player) {
        return getTilesFor(player);
    }

    @Override
    public Set<Tile> getAllPiecesInBoard() {
        Set<Tile> allPieces = new HashSet<>(whitePlayers);
        allPieces.addAll(blackPlayers);
        return allPieces;
    }

    private Set<Tile> getTilesFor(Player team) {
        return (team == Player.White) ? whitePlayers : blackPlayers;
    }

    private void createStartingRows(int rows) {
        createStartingRows(0, rows, Player.White);
        createStartingRows(boardSize - rows, boardSize, Player.Black);
    }

    private void createStartingRows(int yStart, int yEnd, Player player) {
        Piece pawn = (player == Player.White) ? Piece.WhitePawn : Piece.BlackPawn;
        for (int y = yStart; y < yEnd; y++) {
            for (int x = 0; x < boardSize; x++) {
                if ((isEven(y) && isEven(x)) || (!isEven(y) && !isEven(x))) {
                    try {
                        addPiece(x, y, pawn);
                    } catch (BoardException e) {
                        throw new InternalError();
                    }
                }
            }
        }
    }

    private void notifyListenerBoardHasChanged() {
        if (Listener != null) {
            Listener.boardHasChanged();
        }
    }

    private static boolean isEven(int number) {
        return number % 2 == 0;
    }
}
