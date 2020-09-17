/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers.acra

import android.content.Context
import el.arn.ultimatecheckers.AppRoot
import el.arn.ultimatecheckers.appRoot
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class CustomReportSender(private val appRoot: AppRoot) : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        //appRoot.userFeedbackManager.sendCrashReport(errorContent.toJSON())
    }
}

class CustomReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender =
        CustomReportSender(appRoot)
    override fun enabled(config: CoreConfiguration): Boolean = true
}