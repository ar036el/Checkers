package el.arn.checkers.dialogs.composites

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageButton
import el.arn.checkers.R
import el.arn.checkers.complementaries.listener_mechanism.ListenersManager
import el.arn.checkers.complementaries.listener_mechanism.HoldsListeners

//TODo renen: should I put the tringRes in ctor or inside?
class FiveStarsLayout(
    layout: ViewGroup,
    context: Context,
    listener: Listener,
    initialStars: Int = 0,
    private val listenersMgr: ListenersManager<Listener> = ListenersManager()
): HoldsListeners<FiveStarsLayout.Listener> by listenersMgr {


    var rating: Int = 0
        set(value) {
            if (field != value) {
                field = value
                updateStarButtons(value)
            }
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

        listenersMgr.notifyAll { it.onRatingChanged(stars) }
    }


    interface Listener {
        fun onRatingChanged(rating: Int)
    }

    init {
        rating = initialStars
        listenersMgr.addListener(listener)

        addClickListenersToStarsImageViews()
    }

    private fun addClickListenersToStarsImageViews() {
        oneStarButton.setOnClickListener { rating = 1 }
        twoStarButton.setOnClickListener { rating = 2 }
        threeStarButton.setOnClickListener { rating = 3 }
        fourStarButton.setOnClickListener { rating = 4 }
        fiveStarButton.setOnClickListener { rating = 5 }
    }

}