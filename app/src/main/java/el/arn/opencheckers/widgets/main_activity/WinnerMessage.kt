package el.arn.opencheckers.widgets.main_activity

import android.animation.Animator
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.android.AnimatorListener

class WinnerMessageDrawableStyleDecorator {
    @DrawableRes fun get(winningMessageOption: WinnerMessage.WinningMessageOptions): Int {
        return when (winningMessageOption) {
            WinnerMessage.WinningMessageOptions.YouWin -> R.drawable.winner_message_win
            WinnerMessage.WinningMessageOptions.YouLose -> R.drawable.winner_message_lose
            WinnerMessage.WinningMessageOptions.Player1Wins -> R.drawable.winner_message_player1
            WinnerMessage.WinningMessageOptions.Player2Wins -> R.drawable.winner_message_player2
        }
    }
}

class WinnerMessage(
    private val layout: ImageView,
    private val winnerMessageDrawableStyleDecorator: WinnerMessageDrawableStyleDecorator
) {
    enum class VisibilityStates { Hidden, OnAnimation, Shown }
    enum class WinningMessageOptions { YouWin, YouLose, Player1Wins, Player2Wins}

    lateinit var state: VisibilityStates
        private set

    private var currentAnimationHandler: ViewPropertyAnimator? = null

    fun show(applyAnimation: Boolean, onComplete: (() -> Unit)?, winningMessage: WinningMessageOptions) {
        if (state != VisibilityStates.Hidden) {
            return
        }
        state = VisibilityStates.OnAnimation
        layout.setImageResource(winnerMessageDrawableStyleDecorator.get(winningMessage))
        currentAnimationHandler = layout.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(object :
                AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                state = VisibilityStates.Shown
                onComplete?.invoke()
            }
        })
    }

    fun hide() {
        if (state == VisibilityStates.OnAnimation) {
            currentAnimationHandler?.cancel()
        }
        layout.alpha = 0f
        state = VisibilityStates.Hidden
    }

    init {
        hide()
    }
}