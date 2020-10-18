/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.activityWidgets.mainActivity

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.timecalc.helpers.android.AnimatorListener
import el.arn.opencheckers.R
import el.arn.opencheckers.helpers.game_enums.WinningTypeOptions
import el.arn.opencheckers.helpers.listeners_engine.HoldsListeners
import el.arn.opencheckers.helpers.listeners_engine.ListenersManager

interface WinnerMessage : HoldsListeners<WinnerMessage.Listener> {

    fun show(applyAnimation: Boolean, winningType: WinningTypeOptions)
    fun hide()
    val state: States

    enum class States { Hidden, OnAnimation, Shown }


    interface Listener {
        fun messageAnimationWasFinished()
        fun messageWasClickedWhenMessageIsShown()
        fun stateWasChanged(state: States)
    }
}

class WinnerMessageImpl(
    private val layout: ImageView,
    private val listenersMgr: ListenersManager<WinnerMessage.Listener> = ListenersManager()
) : WinnerMessage, HoldsListeners<WinnerMessage.Listener> by listenersMgr {


    private val winnerMessageDrawableStyleDecorator: WinnerMessageDrawableStyleDecorator = WinnerMessageDrawableStyleDecorator()
    private var currentAnimationHandler: ViewPropertyAnimator? = null

    override var state: WinnerMessage.States = WinnerMessage.States.Hidden
        set(value) {
            if (field != value) {
                field = value
                listenersMgr.notifyAll { it.stateWasChanged(value) }
            }
        }

    override fun show(applyAnimation: Boolean, winningType: WinningTypeOptions) {
        if (state != WinnerMessage.States.Hidden) {
            return
        }
        state = WinnerMessage.States.OnAnimation

        layout.setImageResource(winnerMessageDrawableStyleDecorator.get(winningType))
        layout.visibility = View.VISIBLE
        if (applyAnimation) {
            currentAnimationHandler = layout.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(object :
                    AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        state = WinnerMessage.States.Shown
                        listenersMgr.notifyAll { it.messageAnimationWasFinished() }
                    }
                    override fun onAnimationCancel(animation: Animator?) {
                        animation?.removeAllListeners()
                    }
                })
        } else {
            layout.alpha = 1f
        }
    }

    override fun hide() {
        if (state == WinnerMessage.States.OnAnimation) {
            currentAnimationHandler?.cancel()
        }
        state = WinnerMessage.States.Hidden

        layout.visibility = View.GONE
        layout.alpha = 0f
    }

    init {
        hide()
        layout.setOnClickListener {
            if (state == WinnerMessage.States.Shown) {
                listenersMgr.notifyAll { it.messageWasClickedWhenMessageIsShown() }
            }
        }
    }

    class WinnerMessageDrawableStyleDecorator {
        @DrawableRes fun get(winningType: WinningTypeOptions): Int {
            return when (winningType) {
                WinningTypeOptions.Win -> R.drawable.winner_message_win
                WinningTypeOptions.Lose -> R.drawable.winner_message_lose
                WinningTypeOptions.Player1Wins -> R.drawable.winner_message_player1
                WinningTypeOptions.Player2Wins -> R.drawable.winner_message_player2
            }
        }
    }
}