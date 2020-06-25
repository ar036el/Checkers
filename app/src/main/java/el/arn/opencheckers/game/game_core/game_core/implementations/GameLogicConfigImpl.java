package el.arn.opencheckers.game.game_core.game_core.implementations;

import java.util.HashSet;
import java.util.Set;

import el.arn.opencheckers.game.game_core.game_core.configurations.ConfigListener;
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig;

public class GameLogicConfigImpl implements GameLogicConfig {

    private boolean isCapturingMandatory = isCapturingMandatoryDefaultValue;
    private KingBehaviourOptions kingBehaviour = kingBehaviourDefaultValue;
    private CanPawnCaptureBackwardsOptions canPawnCaptureBackwards = canPawnCaptureBackwardsDefaultValue;
    private Set<ConfigListener> Listeners = new HashSet<>();


    public GameLogicConfigImpl() { }
    public GameLogicConfigImpl(boolean isCapturingMandatory, KingBehaviourOptions kingBehaviour, CanPawnCaptureBackwardsOptions canPawnCaptureBackwards) {
        setIsCapturingMandatory(isCapturingMandatory);
        setKingBehaviour(kingBehaviour);
        setCanPawnCaptureBackwards(canPawnCaptureBackwards);
    }

    @Override
    public void addListener(ConfigListener Listener) {
        Listeners.add(Listener);
    }

    @Override public boolean getIsCapturingMandatory() {
        return isCapturingMandatory;
    }
    @Override public void setIsCapturingMandatory(boolean value) {
        this.isCapturingMandatory = value;
        notifyListenersConfigurationHasChanged();
    }

    @Override public KingBehaviourOptions getKingBehaviour() {
        return kingBehaviour;
    }
    @Override public void setKingBehaviour(KingBehaviourOptions value) {
        kingBehaviour = value;
        notifyListenersConfigurationHasChanged();
    }

    @Override public CanPawnCaptureBackwardsOptions getCanPawnCaptureBackwards() {
        return canPawnCaptureBackwards;
    }
    @Override public void setCanPawnCaptureBackwards(CanPawnCaptureBackwardsOptions value) {
        canPawnCaptureBackwards = value;
        notifyListenersConfigurationHasChanged();
    }

    private void notifyListenersConfigurationHasChanged() {
        for (ConfigListener Listener : Listeners) {
            Listener.configurationHasChanged();
        }
    }
}
