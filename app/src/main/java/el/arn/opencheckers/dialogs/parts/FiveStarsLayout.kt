package el.arn.opencheckers.dialogs.parts

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageButton
import el.arn.opencheckers.R
import el.arn.opencheckers.complementaries.listener_mechanism.ListenersManager
import el.arn.opencheckers.complementaries.listener_mechanism.HoldsListeners

//TODo renen: should I put the tringRes in ctor or inside?
class FiveStarsLayout(
    layout: ViewGroup,
    context: Context,
    delegate: Listener,
    initialStars: Int = 0,
    private val delegationMgr: ListenersManager<Listener> = ListenersManager()
): HoldsListeners<FiveStarsLayout.Listener> by delegationMgr {


    var rating: Int = 0
        set(value) {
            updateStarButtons(value)
            field = value
        }



    private val oneStarButton = layout.findViewById<ImageButton>(R.id.fiveStarsLayout_1star)
    private val twoStarButton = layout.findViewById<ImageButton>(R.id.fiveStarsLayout_2stars)
    private val threeStarButton = layout.findViewById<ImageButton>(R.id.fiveStarsLayout_3stars)
    private val fourStarButton = layout.findViewById<ImageButton>(R.id.fiveStarsLayout_4stars)
    private val fiveStarButton = layout.findViewById<ImageButton>(R.id.fiveStarsLayout_5stars)

    private val starButtons = listOf(oneStarButton, twoStarButton, threeStarButton, fourStarButton, fiveStarButton)


    private fun updateStarButtons(stars: Int) /**@param int 0 - 5 stars*/ {
        val filled = 0 until Math.min(stars, 5)
        val empty = stars until 5

        filled.forEach { i -> starButtons[i].setImageResource(R.drawable.ic_star_filled_32dp) }
        empty.forEach { i -> starButtons[i].setImageResource(R.drawable.ic_star_empty_32dpcrap) }

        delegationMgr.notifyAll { it.onRatingChanged(stars) }
    }


    interface Listener {
        fun onRatingChanged(rating: Int)
    }

    init {
        rating = initialStars
        delegationMgr.addListener(delegate)
    }


}