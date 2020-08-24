package el.arn.checkers.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import el.arn.checkers.R


class FirstContactActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goToMatchingActivity()
    }

    private fun goToMatchingActivity() {
        val hasCompletedOnboarding = applicationContext.getSharedPreferences(
            resources.getString(R.string.internal_prefFileKey_onboarding),
            Context.MODE_PRIVATE
        ).getBoolean(resources.getString(R.string.internal_prefFileKey_hasCompletedOnboarding), true) //todo make it false and complete onboarding

        val intent = if (!hasCompletedOnboarding) {
            Intent(this, OnboardingActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }


}