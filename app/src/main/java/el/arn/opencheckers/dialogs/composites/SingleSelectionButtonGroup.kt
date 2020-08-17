package el.arn.opencheckers.dialogs.composites

import android.view.View
import el.arn.opencheckers.tools.preferences_managers.Pref


class SingleSelectionButtonGroup<T>(
    private val buttonsId: Array<Int>,
    private val applyToSelectedButton: (View) -> Unit,
    private val applyToUnselectedButton: (View) -> Unit,
    private val pref: Pref<T>,
    private val dialogLayout: View
) {

    var value: T
        private set

    private val values = pref.possibleValues!!.toList()
    private var selected: View? = null


    init {
        if (values != pref.possibleValues) { throw InternalError("values must match pref.possibleValues perfectly") }
        if (buttonsId.size != values.size) { throw InternalError("buttons must match pref values") }

        value = pref.value

        for (buttonId in buttonsId) {
            dialogLayout.findViewById<View>(buttonId).setOnClickListener{ select(it) }
        }

        val defaultButtonId = buttonsId[values.indexOf(value)]
        select(dialogLayout.findViewById(defaultButtonId))
    }

    private fun select(button: View) {
        this.selected?.let { applyToUnselectedButton(it) }
        applyToSelectedButton(button)
        this.selected = button
        val index = buttonsId.indexOf(button.id)
        value = values[index]
        saveValue(value)
    }


    private fun saveValue(value: T) {
        pref.value = value
    }
}