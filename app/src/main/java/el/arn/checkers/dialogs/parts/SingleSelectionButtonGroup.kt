package el.arn.checkers.dialogs.parts

import android.view.View
import el.arn.checkers.managers.preferences_managers.Preference


class SingleSelectionButtonGroup<T>(
    private val buttonsId: Array<Int>,
    private val applyToSelectedButton: (View) -> Unit,
    private val applyToUnselectedButton: (View) -> Unit,
    private val preference: Preference<T>,
    private val dialogLayout: View
) {

    var value: T
        private set

    private val values = preference.possibleValues!!.toList()
    private var selected: View? = null


    init {
        if (values != preference.possibleValues) { throw InternalError("values must match pref.possibleValues perfectly") }
        if (buttonsId.size != values.size) { throw InternalError("buttons must match pref values") }

        value = preference.value

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
        preference.value = value
    }
}