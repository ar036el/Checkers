package el.arn.opencheckers.delegationMangement

interface LimitedDelegate {

    val destroyIf: (() -> Boolean)?
    val destroyAfterIf: (() -> Boolean)?

    val destroyAfterTotalCallsOf: Int?
    val destroyAfterCall: Boolean
    fun destroy()

}
object LimitedDelegateFactory {
    fun createImpl() = LimitedDelegateImpl()
}

class LimitedDelegateImpl(
    override var destroyIf: (() -> Boolean)? = null,
    override var destroyAfterIf: (() -> Boolean)? = null,
    override var destroyAfterTotalCallsOf: Int? = null,
    override var destroyAfterCall: Boolean = false
) : LimitedDelegate {
    override fun destroy() {
        destroyed = true
    }
    var destroyed: Boolean = false
        private set
}