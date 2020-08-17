package el.arn.opencheckers.complementaries


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