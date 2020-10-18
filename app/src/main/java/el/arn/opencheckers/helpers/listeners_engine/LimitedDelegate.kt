/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.helpers.listeners_engine

interface LimitedListener {

    val destroyIf: (() -> Boolean)?
    val destroyAfterIf: (() -> Boolean)?

    val destroyAfterTotalCallsOf: Int?
    val destroyAfterCall: Boolean
    fun destroy()

}
object LimitedListenerFactory {
    fun createImpl() = LimitedListenerImpl()
}

class LimitedListenerImpl(
    override var destroyIf: (() -> Boolean)? = null,
    override var destroyAfterIf: (() -> Boolean)? = null,
    override var destroyAfterTotalCallsOf: Int? = null,
    override var destroyAfterCall: Boolean = false
) : LimitedListener {
    override fun destroy() {
        destroyed = true
    }
    var destroyed: Boolean = false
        private set
}