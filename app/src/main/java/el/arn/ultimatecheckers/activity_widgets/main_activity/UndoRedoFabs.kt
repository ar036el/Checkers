/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.activity_widgets.main_activity

import android.app.Activity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.helpers.consts.ALPHA_ICON_ENABLED
import el.arn.ultimatecheckers.helpers.consts.ALPHA_ICON_DISABLED
import el.arn.ultimatecheckers.helpers.listeners_engine.HoldsListeners
import el.arn.ultimatecheckers.helpers.listeners_engine.ListenersManager
import el.arn.ultimatecheckers.game.UndoRedoDataBridgeSideA

/*

toggles the fab buttons.
sets them in the layout only at start
make the redo button small and to be behind the big undo button.


deps:
*undoRedoMediator
*undo fab component
*redo fab component


*/

interface UndoRedoFabs: HoldsListeners<UndoRedoFabs.Listener> {
    fun enableUndoButton()
    fun disableUndoButton()
    fun showRedoButton(withAnimation: Boolean)
    fun hideRedoButton(withAnimation: Boolean)
    val isUndoButtonEnabled: Boolean
    val isRedoButtonEnabled: Boolean

    interface Listener {
        fun undoFabWasClicked()
        fun redoFabsWasClicked()
    }
}

class UndoRedoFabsImpl(
    private val undoRedoDataBridgeSideA: UndoRedoDataBridgeSideA,
    private val undoFabComponent: FloatingActionButton,
    private val redoFabComponent: FloatingActionButton,
    private val activity: Activity,
    private val isLayoutDirectionRTL: Boolean,
    private val listenersMgr: ListenersManager<UndoRedoFabs.Listener> = ListenersManager()
) : UndoRedoFabs, HoldsListeners<UndoRedoFabs.Listener> by listenersMgr {

    companion object {
        const val REDO_BUTTON_ANIMATION_DURATION = 200
    }

    override var isUndoButtonEnabled = undoRedoDataBridgeSideA.canUndo
    override var isRedoButtonEnabled = undoRedoDataBridgeSideA.canRedo

    init {
        initRedoFabPosition()

        undoFabComponent.setOnClickListener {
            keepRedoButtonBehindUndoButton()
            if (isUndoButtonEnabled && undoRedoDataBridgeSideA.isEnabled) {
                listenersMgr.notifyAll { it.undoFabWasClicked() }
            }
        }

        redoFabComponent.setOnClickListener {
            keepRedoButtonBehindUndoButton()
            if (isRedoButtonEnabled && undoRedoDataBridgeSideA.isEnabled) {
                listenersMgr.notifyAll { it.redoFabsWasClicked() }
            }
        }

        if (undoRedoDataBridgeSideA.canUndo) enableUndoButton() else disableUndoButton()
        if (undoRedoDataBridgeSideA.canRedo) showRedoButton(false) else hideRedoButton(false)
    }

    private fun keepRedoButtonBehindUndoButton() {
        redoFabComponent.elevation = -100f
        undoFabComponent.elevation = 200f
    }

    private fun initRedoFabPosition() {
        redoFabComponent.y = undoFabComponent.y + (undoFabComponent.height - redoFabComponent.height) / 2
        keepRedoButtonBehindUndoButton()
    }

    override fun enableUndoButton() {
        undoFabComponent.imageAlpha = ALPHA_ICON_ENABLED
        isUndoButtonEnabled = true
    }
    override fun disableUndoButton() {
        undoFabComponent.imageAlpha = ALPHA_ICON_DISABLED
        isUndoButtonEnabled = false
    }

    override fun showRedoButton(withAnimation: Boolean) {
        if (isRedoButtonEnabled && withAnimation) {
            return
        }
        keepRedoButtonBehindUndoButton()

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

    override fun hideRedoButton(withAnimation: Boolean) {
        if (!isRedoButtonEnabled && withAnimation) {
            return
        }
        keepRedoButtonBehindUndoButton()

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