package el.arn.opencheckers.mainActivity

import android.animation.Animator
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import el.arn.opencheckers.kol_minei.AnimatorListener

class WinnerMessage(
    private val layout: ViewGroup
) {
    enum class States { Hidden, OnAnimation, Shown }

    lateinit var state: States
        private set

    private var currentAnimationHandler: ViewPropertyAnimator? = null

    fun show(applyAnimation: Boolean, onComplete: (() -> Unit)?) {
        if (state != States.Hidden) {
            return
        }
        state = States.OnAnimation
        currentAnimationHandler = layout.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(object : AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                state = States.Shown
                onComplete?.invoke()
            }
        })
    }

    fun hide() {
        if (state == States.OnAnimation) {
            currentAnimationHandler?.cancel()
        }
        layout.alpha = 0f
        state = States.Hidden
    }

    init {
        hide()
    }
}