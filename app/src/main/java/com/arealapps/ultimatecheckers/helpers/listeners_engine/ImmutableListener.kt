/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.listeners_engine

interface ImmutableListenerHolder<D>

class ImmutableListenerManager<D>(Listener: D) : ImmutableListenerHolder<D> {
    private val handler = ListenersHandlerEngine(Listener)

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}