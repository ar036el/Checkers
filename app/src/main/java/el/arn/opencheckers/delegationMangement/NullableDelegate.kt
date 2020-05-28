package el.arn.opencheckers.delegationMangement

interface DelegateHolder<D> {
    fun setDelegate(delegate: D?)
    fun removeDelegate()
}

class DelegateManager<D>(delegate: D? = null) : DelegateHolder<D> {
    private val handler = if (delegate != null) DelegatesHandlerEngine(delegate) else DelegatesHandlerEngine()

    override fun setDelegate(delegate: D?) {
        handler.clear()
        delegate?.let { handler.add(it) }
    }
    override fun removeDelegate() {
        handler.clear()
    }
    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}