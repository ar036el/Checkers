/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.helpers.listeners_engine

interface HoldsListener<D> {
    fun setListener(listener: D?)
    fun removeListener()
}

class ListenerManager<D>(Listener: D? = null) : HoldsListener<D> {
    private val handler: ListenersHandlerEngine<D> = if (Listener != null) ListenersHandlerEngine(Listener) else ListenersHandlerEngine()

    override fun setListener(listener: D?) {
        handler.clear()
        listener?.let { handler.add(it) }
    }
    override fun removeListener() {
        handler.clear()
    }
    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}