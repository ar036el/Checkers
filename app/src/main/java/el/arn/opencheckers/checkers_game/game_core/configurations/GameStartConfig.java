package el.arn.opencheckers.checkers_game.game_core.configurations;

public interface GameStartConfig {

    //startingPlayer
    StartingPlayerOptions getStartingPlayer();
    void setStartingPlayer(StartingPlayerOptions value);
    enum StartingPlayerOptions { White, Black, Random }
    StartingPlayerOptions startingPlayerDefaultValue = StartingPlayerOptions.White;
}
