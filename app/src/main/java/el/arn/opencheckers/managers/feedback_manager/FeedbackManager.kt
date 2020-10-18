/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.managers.feedback_manager

import el.arn.opencheckers.R
import el.arn.opencheckers.helpers.android.stringFromRes


interface FeedbackManager {
    fun sendCrashReport(crashReportMessage: String)
    fun sendFeedback(stars: Int?, message: String?)
}

class FeedbackManagerImpl : FeedbackManager {
    private val emailSender =
        EmailSender(
            "opencheckers@gmail.com",
            "0CFF2E484DE5FAD11BFA1ACCA1DE4DB78373",
            "smtp.elasticemail.com",
            "2525"
        )
    private val from = "opencheckers@gmail.com"
    private val to = "ultimatecheckersfeedback@protonmail.com"


    override fun sendCrashReport(crashReportMessage: String) {
        emailSender.sendMailAsync(from, to, "crashReport", crashReportMessage)
    }

    override fun sendFeedback(stars: Int?, message: String?) {
        val body = "stars:" + (stars?.toString() ?: "null") + "\nmessage:" + (message ?: "null")

        emailSender.sendMailAsync(from, to,"user feedback", body)
    }


}