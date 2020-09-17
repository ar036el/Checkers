/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.helpers.functions.late_invocation_function


class LateInvocationFunction( //Todo make it async? with 'callIfReady' completion function for that
    private var function: (params: Array<out Any?>) -> Unit,
    private var invokesOnlyOnce: Boolean, //TODo not sure if to keep
    var isReady: Boolean,
    private vararg val gates: Gate
) {
    private var invokedOnce = false

    fun trigger(gateIndex: Int, vararg functionParams: Any?) {
        gates[gateIndex].trigger()
        tryToInvoke(functionParams)
    }

    private fun tryToInvoke(vararg functionParams: Any?): Boolean /**@return if successful*/ {
        if (invokedOnce && invokesOnlyOnce || !isReady) {
            return false
        }

        if (areAllGatesOpen()) {
            function.invoke(functionParams)
            invokedOnce = true
            return true
        }

        return false
    }

    fun willInvokeWhenAllGatesAreTriggeredAndOpen() {
        isReady = true
    }

    private fun areAllGatesOpen(): Boolean {
        var allGatesAreOpen = true
        for (gate in gates) {
            if (!gate.isOpen) {
                allGatesAreOpen = false
            }
        }
        return allGatesAreOpen
    }
}

