/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers.themed_resources

import el.arn.ultimatecheckers.helpers.listeners_engine.LimitedListener
import el.arn.ultimatecheckers.helpers.listeners_engine.LimitedListenerImpl
import el.arn.ultimatecheckers.managers.preferences_managers.Preference

interface ChangesStyleByTheme {
    val themedResource: ThemedResource<*>
    fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean)
    fun disableAutoRefresh()
}

class ChangesStyleByTheme_implByDelegation<T>(
    override val themedResource: ThemedResource<T>
) : ChangesStyleByTheme {

    private var preferenceListener: Preference.Listener<T>? = null

    override fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean) {

        val _prefListener = object : Preference.Listener<T> , LimitedListener by LimitedListenerImpl(destroyIf = stopWhen) {
            override fun prefHasChanged(preference: Preference<T>, value: T) {
                updateStyleFunc(themedResource)
            }
        }
        preferenceListener = _prefListener
        themedResource.preference.addListener(_prefListener)
    }
    override fun disableAutoRefresh() {
        preferenceListener?.let { themedResource.preference.removeListener(it) }
    }
}