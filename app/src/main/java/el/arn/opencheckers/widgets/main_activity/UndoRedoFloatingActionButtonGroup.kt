package el.arn.opencheckers.widgets.main_activity

import android.app.Activity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import el.arn.opencheckers.R
import el.arn.opencheckers.game.UndoRedoDataBridge
import el.arn.opencheckers.game.UndoRedoDataBridgeGateA

/*

toggling the fab buttons.
sets them in the layout only at start
make the redo button small and to be behind the big undo button.


deps:
*undoRedoMediator
*undo fab component
*redo fab component


*/

class UndoRedoFloatingActionButtonGroup(
    private val undoRedoDataBridge: UndoRedoDataBridgeGateA,
    private val undoFabComponent: FloatingActionButton,
    private val redoFabComponent: FloatingActionButton,
    private val activity: Activity,
    private val isLayoutDirectionRTL: Boolean
) {
    companion object {
        const val REDO_BUTTON_ANIMATION_DURATION = 200
    }

    var isUndoButtonEnabled = undoRedoDataBridge.hasUndo
    var isRedoButtonEnabled = undoRedoDataBridge.hasRedo

    init {

        val cc = undoFabComponent.y + (undoFabComponent.height - redoFabComponent.height) / 2
        redoFabComponent.y = undoFabComponent.y + (undoFabComponent.height - redoFabComponent.height) / 2
        redoFabComponent.elevation = undoFabComponent.elevation - 1


        undoRedoDataBridge.addListener( object: UndoRedoDataBridge.Listener {
            override fun stateHasChanged(isEnabled: Boolean, hasUndo: Boolean, hasRedo: Boolean) {
                setState(hasUndo, hasRedo, true)
            }
        })


        redoFabComponent.setOnClickListener {
            if (isRedoButtonEnabled) {
                undoRedoDataBridge.redo()
            }
        }
        undoFabComponent.setOnClickListener {
            if (isUndoButtonEnabled) {
                undoRedoDataBridge.undo()
            }
        }

        setState(undoRedoDataBridge.hasUndo, undoRedoDataBridge.hasRedo, false)
    }

    fun setState(hasUndo: Boolean, hasRedo: Boolean, withAnimation: Boolean) {
        if (hasUndo) enableUndoButton() else disableUndoButton()
        if (hasRedo) showRedoButton(withAnimation) else hideRedoButton(withAnimation)
    }

    private fun enableUndoButton() {
        isUndoButtonEnabled = true
        //is already enabled when calling undoButton.icon.alpha
    }
    private fun disableUndoButton() {
        isUndoButtonEnabled = false
        //it's already enabled when the call to undoButton.icon.alpha is being called
    }

    private fun showRedoButton(withAnimation: Boolean) {
        if (isRedoButtonEnabled && withAnimation) {
            return
        }
        val destinationX = if (!isLayoutDirectionRTL) {
                undoFabComponent.x + undoFabComponent.width + activity.resources.getDimension(R.dimen.fab_spacing)
            } else {
                undoFabComponent.x - redoFabComponent.width - activity.resources.getDimension(R.dimen.fab_spacing)
            }

        if (withAnimation) {
            redoFabComponent.animate().x(destinationX)
                .setInterpolator(FastOutSlowInInterpolator()).setDuration(REDO_BUTTON_ANIMATION_DURATION.toLong())
        } else {
            redoFabComponent.x = destinationX
        }
        isRedoButtonEnabled = true
    }

    private fun hideRedoButton(withAnimation: Boolean) {
        if (!isRedoButtonEnabled && withAnimation) {
            return
        }
        val destinationX = undoFabComponent.x + (undoFabComponent.width - redoFabComponent.width) / 2

        if (withAnimation) {
        redoFabComponent.animate().x(destinationX)
            .setInterpolator(FastOutSlowInInterpolator()).setDuration(REDO_BUTTON_ANIMATION_DURATION.toLong())
        } else {
            redoFabComponent.x = destinationX
        }

        isRedoButtonEnabled = false
    }

}