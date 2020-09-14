package el.arn.checkers.helpers.android

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup

val RadioGroup.radioButtons: Set<RadioButton>
    get() {
        val count: Int = this.childCount
        val setOfRadioButtons = mutableSetOf<RadioButton>()
        for (i in 0 until count) {
            val view = this.getChildAt(i)
            if (view is RadioButton) {
                setOfRadioButtons.add(view)
            }
        }
        return setOfRadioButtons.toSet()
    }