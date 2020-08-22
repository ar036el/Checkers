package el.arn.checkers.helpers.listeners_engine

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