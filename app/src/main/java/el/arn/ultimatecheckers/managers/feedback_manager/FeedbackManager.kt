/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers.feedback_manager

import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.helpers.android.stringFromRes


interface FeedbackManager {
    fun sendCrashReport(crashReportMessage: String)
    fun sendFeedback(stars: Int?, message: String?)
}

class FeedbackManagerImpl : FeedbackManager {
    private val emailSender =
        EmailSender(
            stringFromRes(R.string.internal_feedbackSenderEmail),
            "lalalala000",
            "smtp.gmail.com",
            "587"
        )
    private val from = stringFromRes(R.string.internal_feedbackSenderEmail)
    private val to = stringFromRes(R.string.internal_feedbackRecipientEmail)


    override fun sendCrashReport(crashReportMessage: String) {
        try { //because it's a crash report, no unhandled exception is allowed
            emailSender.sendMailAsync(from, to, "crashReport", crashReportMessage)
        } catch (e: Throwable) { }
    }

    override fun sendFeedback(stars: Int?, message: String?) {
        val body = "stars:" + (stars?.toString() ?: "null") + "\nmessage:" + (message ?: "null")

        emailSender.sendMailAsync(from, to,"user feedback", body)
    }


}