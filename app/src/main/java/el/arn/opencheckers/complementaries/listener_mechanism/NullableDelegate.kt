package el.arn.opencheckers.complementaries.listener_mechanism

interface HoldsListener<D> {
    fun setListener(Listener: D?)
    fun removeListener()
}

class ListenerManager<D>(Listener: D? = null) : HoldsListener<D> {
    private val handler = if (Listener != null) ListenersHandlerEngine(Listener) else ListenersHandlerEngine()

    override fun setListener(Listener: D?) {
        handler.clear()
        Listener?.let { handler.add(it) }
    }
    override fun removeListener() {
        handler.clear()
    }
    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}