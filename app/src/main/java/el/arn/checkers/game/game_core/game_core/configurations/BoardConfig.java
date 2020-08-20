package el.arn.checkers.game.game_core.game_core.configurations;

import el.arn.checkers.game.game_core.game_core.exceptions.ConfigurationException;

public interface BoardConfig {

    //boardSize
    int getBoardSize();
    void setBoardSize(int value) throws ConfigurationException;
    int boardSizeDefaultValue = 8;
    int minBoardSize = 4;
    int maxBoardSize = 26;

    //startingRowsForEachPlayer
    int getStartingRowsForEachPlayer();
    void setStartingRowsForEachPlayer(int value) throws ConfigurationException;
    int startingRowsForEachPlayerDefaultValue = 2;
    int minStartingRowsForEachPlayer = 1;
    int maxStartingRowsForEachPlayer(int boardSize) throws ConfigurationException;

}
