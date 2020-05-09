package el.arn.opencheckers.checkers_game.game_core.configurations;

public interface GameLogicConfig {

    //isCapturingMandatory
    boolean getIsCapturingMandatory();
    void setIsCapturingMandatory(boolean value);
    boolean isCapturingMandatoryDefaultValue = false;

    //kingBehaviour
    KingBehaviourOptions getKingBehaviour();
    void setKingBehaviour(KingBehaviourOptions value);
    enum KingBehaviourOptions { FlyingKings, LandsRightAfterCapture, NoFlyingKings }
    KingBehaviourOptions kingBehaviourDefaultValue = KingBehaviourOptions.FlyingKings;

    //canPawnCaptureBackwards
    CanPawnCaptureBackwardsOptions getCanPawnCaptureBackwards();
    void setCanPawnCaptureBackwards(CanPawnCaptureBackwardsOptions value);
    enum CanPawnCaptureBackwardsOptions { Always, OnlyWhenMultiCapture, Never }
    CanPawnCaptureBackwardsOptions canPawnCaptureBackwardsDefaultValue = CanPawnCaptureBackwardsOptions.OnlyWhenMultiCapture;

    void setDelegate(ConfigDelegate delegate);
}
