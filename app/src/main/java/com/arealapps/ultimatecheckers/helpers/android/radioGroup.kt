/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.android

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