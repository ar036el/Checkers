/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.managers.external_activity_invoker

import android.content.Context
import android.content.Intent
import android.net.Uri

class UrlInvoker(
    private val url: String,
    private val context: Context
) : ExternalActivityInvoker {
    override fun open() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}