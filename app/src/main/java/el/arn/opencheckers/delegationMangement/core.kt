package el.arn.opencheckers.delegationMangement



interface LimitedDelegate {

    val destroyIf: (() -> Boolean)?
    val destroyAfterIf: (() -> Boolean)?

}


class DelegatesHandlerEngine<D>(vararg delegates: D) {
    private val list = mutableListOf(*delegates)

    fun add(vararg delegates: D) = list.addAll(delegates)
    fun remove(vararg delegates: D) = list.removeAll(delegates)
    fun clear () = list.clear()
    fun contains(delegate: D) = list.contains(delegate)

    fun notifyAll(action: (D) -> Unit) {
        val delegatesToRemove = mutableSetOf<D>()
        for (delegate in list) {
            if (delegate is LimitedDelegate && delegate.destroyIf?.invoke() == true) {
                delegatesToRemove.add(delegate)
                continue
            }

            action.invoke(delegate)


            if (delegate is LimitedDelegate && delegate.destroyAfterIf?.invoke() == true) {
                delegatesToRemove.add(delegate)
            }
        }

        list -= delegatesToRemove
    }
}
