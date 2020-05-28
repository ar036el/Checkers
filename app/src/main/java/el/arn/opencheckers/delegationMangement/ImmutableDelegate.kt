package el.arn.opencheckers.delegationMangement

interface ImmutableDelegateHolder<D>

class ImmutableDelegateManager<D>(delegate: D) : ImmutableDelegateHolder<D> {
    private val handler = DelegatesHandlerEngine(delegate)

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}