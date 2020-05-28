package el.arn.opencheckers.delegationMangement

interface NonNullDelegateHolder<D> {
    fun setDelegate(delegate: D)
}

class NonNullDelegateManager<D>(delegate: D) : NonNullDelegateHolder<D> {
    private val handler = DelegatesHandlerEngine(delegate)

    override fun setDelegate(delegate: D) {
        handler.clear()
        handler.add(delegate)
    }

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}