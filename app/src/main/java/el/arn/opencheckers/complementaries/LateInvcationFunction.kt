package el.arn.opencheckers.complementaries

class LateInvcationFunction( //Todo make it async? with 'callIfReady' completion function for that
    private var function: (params: Array<out Any?>) -> Unit,
    private var callOnlyOnce: Boolean, //TODo not sure if to keep
    private vararg val gates: Gate
) {
    private var invokedOnce = false
    fun triggerAndTryToInvoke(vararg params: Any?): Boolean /**@return if successful*/ {
        if (!invokedOnce || !callOnlyOnce) {
            invokedOnce = true

            var ifAllAreTrue = true
            for (gate in gates) {
                if (!gate.triggerAndCheck()) {
                    ifAllAreTrue = false
                }
            }
            if (ifAllAreTrue) {
                function.invoke(params)
                return true
            }
        }
        return false
    }
}


interface Gate {
    fun triggerAndCheck(): Boolean
}

class LogicGate(private val condition: () -> Boolean) : Gate {
    override fun triggerAndCheck(): Boolean = condition.invoke()
}

class GateByCountOfCalls(private var numberOfCalls: Int) : Gate {
    override fun triggerAndCheck(): Boolean {
        numberOfCalls--
        return (numberOfCalls <= 0)
    }
}