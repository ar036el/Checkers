/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.gameCore.game_core.checkers_game.configurations;

public interface GameStartConfig {

    //startingPlayer
    StartingPlayerOptions getStartingPlayer();
    void setStartingPlayer(StartingPlayerOptions value);
    enum StartingPlayerOptions { White, Black, Random }
    StartingPlayerOptions startingPlayerDefaultValue = StartingPlayerOptions.White;
}
