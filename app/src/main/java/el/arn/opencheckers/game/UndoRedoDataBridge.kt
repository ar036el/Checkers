package el.arn.opencheckers.game

import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners

interface UndoRedoDataBridge : HoldsListeners<UndoRedoDataBridge.Listener>{
    val isEnabled: Boolean
    val hasUndo: Boolean
    val hasRedo: Boolean

    interface Listener {
        fun stateHasChanged(isEnabled: Boolean, hasUndo: Boolean, hasRedo: Boolean) {}
        fun undoWasInvoked() {}
        fun redoWasInvoked() {}
    }
}


interface UndoRedoDataBridgeGateA : UndoRedoDataBridge {
    override var isEnabled: Boolean
    fun undo(): Boolean /**@return true if successful*/
    fun redo(): Boolean /**@return true if successful*/
}

interface UndoRedoDataBridgeGateB : UndoRedoDataBridge {
    override var hasUndo: Boolean
    override var hasRedo: Boolean
}


class UndoRedoDataBridgeImpl(
    private val delegationMgr: ListenersManager<UndoRedoDataBridge.Listener> = ListenersManager()
) : UndoRedoDataBridgeGateA, UndoRedoDataBridgeGateB,
    HoldsListeners<UndoRedoDataBridge.Listener> by delegationMgr {
    override fun undo(): Boolean {
        if (!hasUndo) return false
        delegationMgr.notifyAll { it.undoWasInvoked() }
        return true
    }

    override fun redo(): Boolean {
        if (!hasRedo) return false
        delegationMgr.notifyAll { it.redoWasInvoked() }
        return true
    }

    override var isEnabled: Boolean = false
        set(value) {
            field = value
            delegationMgr.notifyAll { it.stateHasChanged(value, hasUndo, hasRedo) }
        }
    override var hasUndo = false
        set(value) {
            field = value
            delegationMgr.notifyAll { it.stateHasChanged(isEnabled, value, hasRedo) }
        }
    override var hasRedo: Boolean = false
        set(value) {
            field = value
            delegationMgr.notifyAll { it.stateHasChanged(isEnabled, hasUndo, value) }
        }

}