/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.helpers.listeners_engine

interface NonNullListenerHolder<D> {
    fun setListener(Listener: D)
}

class NonNullListenerManager<D>(Listener: D) : NonNullListenerHolder<D> {
    private val handler = ListenersHandlerEngine(Listener)

    override fun setListener(Listener: D) {
        handler.clear()
        handler.add(Listener)
    }

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}