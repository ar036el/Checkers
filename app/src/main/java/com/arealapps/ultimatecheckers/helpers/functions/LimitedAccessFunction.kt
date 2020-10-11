/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.functions

class LimitedAccessFunction(private val function: (params: Array<out Any?>) -> Unit, var accesses: Int = 0) {
    fun invokeIfHasAccess(vararg params: Any?) {
        if (accesses > 0) {
            function.invoke(params)
            accesses--
        }
    }

    fun grantOneAccess() { accesses = 1 }
}