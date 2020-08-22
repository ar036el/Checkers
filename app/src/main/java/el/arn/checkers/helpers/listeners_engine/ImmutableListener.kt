package el.arn.checkers.helpers.listeners_engine

interface ImmutableListenerHolder<D>

class ImmutableListenerManager<D>(Listener: D) : ImmutableListenerHolder<D> {
    private val handler = ListenersHandlerEngine(Listener)

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}