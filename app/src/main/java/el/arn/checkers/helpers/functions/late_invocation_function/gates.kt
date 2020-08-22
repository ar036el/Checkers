package el.arn.checkers.helpers.functions.late_invocation_function


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