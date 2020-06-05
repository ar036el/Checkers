package el.arn.opencheckers.dialogs

import android.app.Activity
import android.content.Context
import el.arn.opencheckers.App
import el.arn.opencheckers.prefs_managers.PrefsManager

class RateUsDialogInvoker {
    object InvokeAfterXCalls {
        const val FEEDBACK_IS_NEUTRAL_OR_UNSPECIFIED = 3
        const val FEEDBACK_IS_NEGATIVE = 2
        const val FEEDBACK_IS_POSITIVE = 5
        val MAX_VALUE = listOf(FEEDBACK_IS_NEUTRAL_OR_UNSPECIFIED, FEEDBACK_IS_POSITIVE, FEEDBACK_IS_NEGATIVE).max()!!
    }

    private val purchasesPrefsManager = object: PrefsManager(App.instance.getSharedPreferences("rateUsDialog", Context.MODE_PRIVATE)) {
        val callsUntilInvocation = createIntPref("callsUntilPrompt", 0..InvokeAfterXCalls.MAX_VALUE, InvokeAfterXCalls.FEEDBACK_IS_NEUTRAL_OR_UNSPECIFIED)
    }

    fun tryToInvoke(activity: Activity): RateUsDialog? {
        if (purchasesPrefsManager.callsUntilInvocation.value < 0) {
            purchasesPrefsManager.callsUntilInvocation.value--
            if (purchasesPrefsManager.callsUntilInvocation.value == 0) {
                return RateUsDialog(this, activity)
            }
        }
        return null
    }

    fun dontInvokeDialogAgain() {
        purchasesPrefsManager.callsUntilInvocation.value = 0
    }

    fun enableInvokingDialogLater(feedbackState: RateUsDialog.FeedbackState? = null) {
        purchasesPrefsManager.callsUntilInvocation.value =
            when(feedbackState) {
                RateUsDialog.FeedbackState.NeutralFeedbackOrUnspecified, null -> InvokeAfterXCalls.FEEDBACK_IS_NEUTRAL_OR_UNSPECIFIED
                RateUsDialog.FeedbackState.NegativeFeedback -> InvokeAfterXCalls.FEEDBACK_IS_NEGATIVE
                RateUsDialog.FeedbackState.PositiveFeedback -> InvokeAfterXCalls.FEEDBACK_IS_POSITIVE
            }
    }

}