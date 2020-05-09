package el.arn.opencheckers.checkers_game.game_core.implementations;

import el.arn.opencheckers.checkers_game.game_core.configurations.ConfigDelegate;
import el.arn.opencheckers.checkers_game.game_core.configurations.GameLogicConfig;

public class GameLogicConfigImpl implements GameLogicConfig {

    private boolean isCapturingMandatory = isCapturingMandatoryDefaultValue;
    private KingBehaviourOptions kingBehaviour = kingBehaviourDefaultValue;
    private CanPawnCaptureBackwardsOptions canPawnCaptureBackwards = canPawnCaptureBackwardsDefaultValue;
    private ConfigDelegate delegate = null;


    public GameLogicConfigImpl() { }
    public GameLogicConfigImpl(boolean isCapturingMandatory, KingBehaviourOptions kingBehaviour, CanPawnCaptureBackwardsOptions canPawnCaptureBackwards) {
        setIsCapturingMandatory(isCapturingMandatory);
        setKingBehaviour(kingBehaviour);
        setCanPawnCaptureBackwards(canPawnCaptureBackwards);
    }

    @Override
    public void setDelegate(ConfigDelegate delegate) {
        this.delegate = delegate;
    }

    @Override public boolean getIsCapturingMandatory() {
        return isCapturingMandatory;
    }
    @Override public void setIsCapturingMandatory(boolean value) {
        this.isCapturingMandatory = value;
        notifyDelegateConfigurationHasChanged();
    }

    @Override public KingBehaviourOptions getKingBehaviour() {
        return kingBehaviour;
    }
    @Override public void setKingBehaviour(KingBehaviourOptions value) {
        kingBehaviour = value;
        notifyDelegateConfigurationHasChanged();
    }

    @Override public CanPawnCaptureBackwardsOptions getCanPawnCaptureBackwards() {
        return canPawnCaptureBackwards;
    }
    @Override public void setCanPawnCaptureBackwards(CanPawnCaptureBackwardsOptions value) {
        canPawnCaptureBackwards = value;
        notifyDelegateConfigurationHasChanged();
    }

    private void notifyDelegateConfigurationHasChanged() {
        if (delegate != null) {
            delegate.configurationHasChanged();
        }
    }
}
