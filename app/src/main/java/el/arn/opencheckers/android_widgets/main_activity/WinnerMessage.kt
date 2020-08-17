package el.arn.opencheckers.android_widgets.main_activity

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.android.AnimatorListener
import el.arn.opencheckers.complementaries.game.WinningTypes
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager

interface WinnerMessage : HoldsListeners<WinnerMessage.Listener> {

    fun show(applyAnimation: Boolean, winningType: WinningTypes)
    fun hide()
    val state: States

    enum class States { Hidden, OnAnimation, Shown }


    interface Listener {
        fun messageAnimationWasFinished()
        fun messageWasClickedWhenStateIsShown()
    }
}

class WinnerMessageImpl(
    private val layout: ImageView,
    private val listenersMgr: ListenersManager<WinnerMessage.Listener> = ListenersManager()
) : WinnerMessage, HoldsListeners<WinnerMessage.Listener> by listenersMgr {


    private val winnerMessageDrawableStyleDecorator: WinnerMessageDrawableStyleDecorator = WinnerMessageDrawableStyleDecorator()
    private var currentAnimationHandler: ViewPropertyAnimator? = null

    override var state: WinnerMessage.States = WinnerMessage.States.Hidden

    override fun show(applyAnimation: Boolean, winningType: WinningTypes) {
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
                listenersMgr.notifyAll { it.messageWasClickedWhenStateIsShown() }
            }
        }
    }

    class WinnerMessageDrawableStyleDecorator {
        @DrawableRes fun get(winningType: WinningTypes): Int {
            return when (winningType) {
                WinningTypes.Win -> R.drawable.winner_message_win
                WinningTypes.Lose -> R.drawable.winner_message_lose
                WinningTypes.Player1Wins -> R.drawable.winner_message_player1
                WinningTypes.Player2Wins -> R.drawable.winner_message_player2
            }
        }
    }
}