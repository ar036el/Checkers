package el.arn.opencheckers.delegationMangement


class DelegatesHandlerEngine<D>(vararg delegates: D) {
    private val list = mutableListOf(*delegates)

    fun add(vararg delegates: D) = list.addAll(delegates)
    fun remove(vararg delegates: D) = list.removeAll(delegates)
    fun clear () = list.clear()
    fun contains(delegate: D) = list.contains(delegate)

    fun notifyAll(action: (D) -> Unit) {
        val delegatesToRemove = mutableSetOf<D>()

        fun remove(delegate: LimitedDelegateImpl) {
            delegatesToRemove.add(delegate as D)
            delegate.destroy()
        }

        for (delegate in list) {

            if (delegate is LimitedDelegateImpl) {
                if (delegate.destroyIf?.invoke() == true
                    || delegate.destroyed) {
                    remove(delegate)
                    continue
                }
            }

            action.invoke(delegate)

            if (delegate is LimitedDelegateImpl) {
                if (delegate.destroyAfterTotalCallsOf != null) {
                    delegate.destroyAfterTotalCallsOf = delegate.destroyAfterTotalCallsOf?.let { it - 1 }
                }
                if (delegate.destroyAfterIf?.invoke() == true
                    || delegate.destroyAfterTotalCallsOf ?: 100 <= 0
                    || delegate.destroyAfterCall) {
                    remove(delegate)
                }
            }

        }

        list -= delegatesToRemove
    }

}

object DelegationManagerFactory {
    fun <D>delegateManager() = DelegateManager<D>()
    fun <D>delegatesManager() = DelegatesManager<D>()
}