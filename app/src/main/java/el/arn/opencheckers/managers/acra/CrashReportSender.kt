/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.managers.acra

import android.content.Context
import android.util.Log
import el.arn.opencheckers.AppRoot
import el.arn.opencheckers.appRoot
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class CustomReportSender(private val appRoot: AppRoot) : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        try { //because it's a crash report, no unhandled exception is allowed inside- nothing should handle an error of an error report
            Log.e("ACRA/MailSender", "Trying to send crash report")
            appRoot.userFeedbackManager.sendCrashReport(errorContent.toJSON())
        } catch (e: Throwable) {
            Log.e("ACRA/MailSender", "failed to send crash report. crash report crashed.")
        }
    }
}

class CustomReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender =
        CustomReportSender(appRoot)
    override fun enabled(config: CoreConfiguration): Boolean = true
}