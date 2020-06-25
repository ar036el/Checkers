package el.arn.opencheckers.tools.feedback_manager

import android.content.Context
import el.arn.opencheckers.AppRoot
import el.arn.opencheckers.appRoot
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class CustomReportSender(private val appRoot: AppRoot) : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        appRoot.userFeedbackManager.sendCrashReport(errorContent.toJSON())
    }
}

class CustomReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender = CustomReportSender(appRoot)
    override fun enabled(config: CoreConfiguration): Boolean = true
}