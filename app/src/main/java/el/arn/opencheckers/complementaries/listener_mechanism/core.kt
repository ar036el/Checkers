package el.arn.opencheckers.complementaries.listener_mechanism


class ListenersHandlerEngine<D>(vararg Listeners: D) {
    private val list = mutableListOf(*Listeners)

    fun add(vararg Listeners: D) = list.addAll(Listeners)
    fun remove(vararg Listeners: D) = list.removeAll(Listeners)
    fun clear () = list.clear()
    fun contains(Listener: D) = list.contains(Listener)

    fun notifyAll(action: (D) -> Unit) {
        val ListenersToRemove = mutableSetOf<D>()

        fun remove(Listener: LimitedListenerImpl) {
            ListenersToRemove.add(Listener as D)
            Listener.destroy()
        }

        val list = list.toList() //to prevent async related problems
        for (Listener in list) {

            if (Listener is LimitedListenerImpl) {
                if (Listener.destroyIf?.invoke() == true
                    || Listener.destroyed) {
                    remove(Listener)
                    continue
                }
            }

            if (Listener in this.list) { //maybe it was removed during this call
                action.invoke(Listener)
            }

            if (Listener is LimitedListenerImpl) {
                if (Listener.destroyAfterTotalCallsOf != null) {
                    Listener.destroyAfterTotalCallsOf = Listener.destroyAfterTotalCallsOf?.let { it - 1 }
                }
                if (Listener.destroyAfterIf?.invoke() == true
                    || Listener.destroyAfterTotalCallsOf ?: 100 <= 0
                    || Listener.destroyAfterCall) {
                    remove(Listener)
                }
            }

        }

        this.list -= ListenersToRemove
    }

}

object DelegationManagerFactory {
    fun <D>listenerManager() = ListenerManager<D>()
    fun <D>listenersManager() = ListenersManager<D>()
}