package el.arn.opencheckers.tools.external_activity_invoker

import android.content.Context
import android.content.Intent
import android.net.Uri

class UrlInvoker(
    private val url: String,
    private val context: Context
) : ExternalActivityInvoker() {
    override fun open() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}