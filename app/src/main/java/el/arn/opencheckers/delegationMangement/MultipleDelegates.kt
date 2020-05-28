package el.arn.opencheckers.delegationMangement

interface DelegatesHolder<D> {
    fun addDelegate(delegate: D)
    fun addDelegates(vararg delegates: D)
    fun removeDelegate(delegate: D)
    fun removeDelegates(vararg delegates: D)
    fun clearDelegates()
    fun hasDelegate(delegate: D): Boolean
}

class Delegates<D>(vararg delegates: D) : DelegatesHolder<D> {
    private val handler = DelegatesHandlerEngine(*delegates)

    override fun addDelegate(delegate: D) = addDelegates(delegate)
    override fun addDelegates(vararg delegates: D) {
        handler.add(*delegates)
    }

    override fun removeDelegate(delegate: D) = removeDelegates(delegate)
    override fun removeDelegates(vararg delegates: D) {
        handler.remove(*delegates)
    }

    override fun clearDelegates() = handler.clear()
    override fun hasDelegate(delegate: D): Boolean = handler.contains(delegate)

    fun notifyAll(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}