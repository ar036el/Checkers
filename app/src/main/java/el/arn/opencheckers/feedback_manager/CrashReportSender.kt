package el.arn.opencheckers.feedback_manager

import android.content.Context
import el.arn.opencheckers.App
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class CustomReportSender : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        App.instance.feedbackManager.sendCrashReport(errorContent.toJSON())
    }
}

class CustomReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender = CustomReportSender()
    override fun enabled(config: CoreConfiguration): Boolean = true
}