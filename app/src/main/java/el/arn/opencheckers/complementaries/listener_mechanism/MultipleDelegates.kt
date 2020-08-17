package el.arn.opencheckers.complementaries.listener_mechanism

interface HoldsListeners<D> {
    fun addListener(Listener: D)
    fun addListeners(vararg Listeners: D)
    fun removeListener(Listener: D): Boolean
    fun removeListeners(vararg Listeners: D): Boolean
    fun clearListeners()
    fun hasListener(Listener: D): Boolean
}


class ListenersManager<D>(vararg Listeners: D) : HoldsListeners<D> {
    private val handler = ListenersHandlerEngine(*Listeners)

    override fun addListener(Listener: D) = addListeners(Listener)
    override fun addListeners(vararg Listeners: D) {
        handler.add(*Listeners)
    }

    override fun removeListener(Listener: D) = removeListeners(Listener)
    override fun removeListeners(vararg Listeners: D): Boolean {
        return handler.remove(*Listeners)
    }

    override fun clearListeners() = handler.clear()
    override fun hasListener(Listener: D): Boolean = handler.contains(Listener)

    fun notifyAll(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}