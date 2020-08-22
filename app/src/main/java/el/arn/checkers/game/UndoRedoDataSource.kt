package el.arn.checkers.game

import el.arn.checkers.helpers.listeners_engine.ListenersManager
import el.arn.checkers.helpers.listeners_engine.HoldsListeners

interface UndoRedoDataBridge : HoldsListeners<UndoRedoDataBridge.Listener>{
    val isEnabled: Boolean
    val canUndo: Boolean
    val canRedo: Boolean
    fun reloadState()

    interface Listener {
        fun stateWasChangedOrReloaded(isEnabled: Boolean, canUndo: Boolean, canRedo: Boolean) {}
        fun undoWasInvoked() {}
        fun redoWasInvoked() {}
    }
}


interface UndoRedoDataBridgeSideA : UndoRedoDataBridge {
    override var isEnabled: Boolean
    fun undo(): Boolean /**@return true if successful*/
    fun redo(): Boolean /**@return true if successful*/
}

interface UndoRedoDataBridgeSideB : UndoRedoDataBridge {
    override var canUndo: Boolean
    override var canRedo: Boolean
}


class UndoRedoDataBridgeImpl(
    private val listenersMgr: ListenersManager<UndoRedoDataBridge.Listener> = ListenersManager()
) : UndoRedoDataBridgeSideA, UndoRedoDataBridgeSideB,
    HoldsListeners<UndoRedoDataBridge.Listener> by listenersMgr {
    override fun undo(): Boolean {
        if (!canUndo) return false
        listenersMgr.notifyAll { it.undoWasInvoked() }
        return true
    }

    override fun redo(): Boolean {
        if (!canRedo) return false
        listenersMgr.notifyAll { it.redoWasInvoked() }
        return true
    }

    override var isEnabled: Boolean = false
        set(value) {
            field = value
            listenersMgr.notifyAll { it.stateWasChangedOrReloaded(value, canUndo, canRedo) }
        }

    override fun reloadState() {
        listenersMgr.notifyAll { it.stateWasChangedOrReloaded(isEnabled, canUndo, canRedo) }
    }

    override var canUndo = false
        set(value) {
            field = value
            listenersMgr.notifyAll { it.stateWasChangedOrReloaded(isEnabled, value, canRedo) }
        }
    override var canRedo: Boolean = false
        set(value) {
            field = value
            listenersMgr.notifyAll { it.stateWasChangedOrReloaded(isEnabled, canUndo, value) }
        }

}