/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.game.game_core.checkers_game.implementations;

import com.arealapps.ultimatecheckers.game.game_core.checkers_game.configurations.GameStartConfig;

public class GameStartConfigImpl implements GameStartConfig {

    private StartingPlayerOptions startingPlayer = startingPlayerDefaultValue;


    public GameStartConfigImpl() { }
    public GameStartConfigImpl(StartingPlayerOptions startingPlayer) {
        setStartingPlayer(startingPlayer);
    }

    @Override public StartingPlayerOptions getStartingPlayer() {
        return startingPlayer;
    }
    @Override public void setStartingPlayer(StartingPlayerOptions value) {
        startingPlayer = value;
    }
}
