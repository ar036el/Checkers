package el.arn.checkers.tools.feedback_manager


class FeedbackManager {

    private val mailSender =
        MailSender(
            "opencheckersfeedbacksender@gmail.com",
            "lalalala000",
            "smtp.gmail.com",
            "587"
        )
    private val from = "openCheckersFeedbackSender"
    private val to = "opencheckersfeedback@gmail.com"


    fun sendCrashReport(crashReportMessage: String) {
        mailSender.sendMailAsync(from, to,"crashReport", crashReportMessage)
    }

    fun sendFeedback(stars: Int?, message: String?) {
        val body = "stars:" + (stars?.toString() ?: "null") + "\nmessage:" + (message ?: "null")

        mailSender.sendMailAsync(from, to,"user feedback", body)
    }


}