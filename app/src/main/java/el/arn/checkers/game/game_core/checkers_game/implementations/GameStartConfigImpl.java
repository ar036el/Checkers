package el.arn.checkers.game.game_core.checkers_game.implementations;

import el.arn.checkers.game.game_core.checkers_game.configurations.GameStartConfig;

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
