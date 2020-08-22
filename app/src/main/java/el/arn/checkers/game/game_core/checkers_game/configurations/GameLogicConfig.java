package el.arn.checkers.game.game_core.checkers_game.configurations;

import el.arn.checkers.helpers.EnumWithId;

public interface GameLogicConfig {

    //isCapturingMandatory
    boolean getIsCapturingMandatory();
    void setIsCapturingMandatory(boolean value);
    boolean isCapturingMandatoryDefaultValue = false;

    //kingBehaviour
    KingBehaviourOptions getKingBehaviour();
    void setKingBehaviour(KingBehaviourOptions value);
    enum KingBehaviourOptions implements EnumWithId { FlyingKings("flyingKings"), LandsRightAfterCapture("landsRightAfterCapture"), NoFlyingKings("noFlyingKings"); private String id; KingBehaviourOptions(String id) {this.id = id;} public String getId() {return id;} }
    KingBehaviourOptions kingBehaviourDefaultValue = KingBehaviourOptions.FlyingKings;

    //canPawnCaptureBackwards
    CanPawnCaptureBackwardsOptions getCanPawnCaptureBackwards();
    void setCanPawnCaptureBackwards(CanPawnCaptureBackwardsOptions value);
    enum CanPawnCaptureBackwardsOptions implements EnumWithId { Always("always"), OnlyWhenMultiCapture("onlyWhenMultiCapture"), Never("never"); private String id; CanPawnCaptureBackwardsOptions(String id) {this.id = id;} public String getId() {return id;} }
    CanPawnCaptureBackwardsOptions canPawnCaptureBackwardsDefaultValue = CanPawnCaptureBackwardsOptions.OnlyWhenMultiCapture;

    void addListener(ConfigListener Listener);
}