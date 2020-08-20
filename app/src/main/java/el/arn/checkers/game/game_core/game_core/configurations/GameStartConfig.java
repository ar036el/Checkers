package el.arn.checkers.game.game_core.game_core.configurations;

public interface GameStartConfig {

    //startingPlayer
    StartingPlayerOptions getStartingPlayer();
    void setStartingPlayer(StartingPlayerOptions value);
    enum StartingPlayerOptions { White, Black, Random }
    StartingPlayerOptions startingPlayerDefaultValue = StartingPlayerOptions.White;
}
