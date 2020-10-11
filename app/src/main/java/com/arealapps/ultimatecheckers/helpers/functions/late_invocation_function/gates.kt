/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.functions.late_invocation_function


interface Gate {
    fun trigger(): Boolean
    val isOpen: Boolean
}

class LogicGate(private val condition: () -> Boolean) : Gate {
    override fun trigger(): Boolean {
        if (condition.invoke()) {
            isOpen = true
        }
        return isOpen
    }
    override var isOpen: Boolean = false
        private set
}

class GateByNumberOfCalls(private var numberOfCalls: Int) : Gate {
    override fun trigger(): Boolean {
        numberOfCalls--
        if (numberOfCalls <= 0) {
            isOpen = true
        }
        return isOpen
    }
    override var isOpen: Boolean = false
        private set
}