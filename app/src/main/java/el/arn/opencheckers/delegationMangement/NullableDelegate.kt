package el.arn.opencheckers.delegationMangement

interface HoldsDelegate<D> {
    fun setDelegate(delegate: D?)
    fun removeDelegate()
}

class DelegateManager<D>(delegate: D? = null) : HoldsDelegate<D> {
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