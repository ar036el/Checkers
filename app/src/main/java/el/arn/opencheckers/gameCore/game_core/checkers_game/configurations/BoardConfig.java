/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.gameCore.game_core.checkers_game.configurations;

import el.arn.opencheckers.gameCore.game_core.checkers_game.exceptions.ConfigurationException;

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
