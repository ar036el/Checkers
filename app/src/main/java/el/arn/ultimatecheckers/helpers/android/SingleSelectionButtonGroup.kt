/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.helpers.android

import android.view.View
import el.arn.ultimatecheckers.managers.preferences_managers.Preference



/**
 * Turns any group of views into a single selection Group.
 * it attaches an onClickListener to the buttons, and applies the selected and unselected functions accordingly.
 * the data is driven from the preference object. it loads the initial selection, and saves the selected values accordingly.
 * the values are mapped by preference's possibleValues and the order of the buttons.
 *
 *
 * @param [preference] must have possibleValues
 * @param [buttonViews] must match preference possibleValues in size, and will be mapped to possibleValues by order of appearance
 * */
class SingleSelectionButtonGroup<PrefValueType, ButtonView : View>(
    private val preference: Preference<PrefValueType>,
    private val buttonViewsWithPrefValues: Set<Pair<ButtonView, PrefValueType>>,
    private val applyToSelectedButton: (ButtonView) -> Unit,
    private val applyToUnselectedButton: (ButtonView) -> Unit
) {

    private var selectedButtonView: ButtonView? = null
    private val buttonViews = buttonViewsWithPrefValues.map { it.first }
    private val prefValues = buttonViewsWithPrefValues.map { it.second }


    init {
        if (buttonViews.toSet().size != buttonViews.size) { throw InternalError("button views cannot have duplicates") }
        if (prefValues.toSet().size != prefValues.size) { throw InternalError("prefValues views cannot have duplicates") }

        for (button in buttonViews) {
            button.setOnClickListener{ selectButton(button) }
        }

        initButtonsSelections()
    }

    private fun initButtonsSelections() {
        val selectedButton = buttonViews[prefValues.indexOf(preference.value)]
        buttonViews.forEach {
            if (it != selectedButton) {
            applyToUnselectedButton(it)
            }
        }
        selectButton(selectedButton)
    }

    private fun selectButton(button: ButtonView) {
        this.selectedButtonView?.let { applyToUnselectedButton(it) }
        applyToSelectedButton(button)
        this.selectedButtonView = button

        val index = buttonViews.indexOf(button)
        preference.value = prefValues[index]
    }
}