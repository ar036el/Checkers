package el.arn.opencheckers.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import el.arn.opencheckers.R


class StartingActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goToMatchingActivity()
    }

    private fun goToMatchingActivity() {
        val hasCompletedOnboarding = applicationContext.getSharedPreferences(
            resources.getString(R.string.prefCategory_onboarding),
            Context.MODE_PRIVATE
        ).getBoolean(resources.getString(R.string.pref_hasCompletedOnboarding), false)

        val intent = if (!hasCompletedOnboarding) {
            Intent(this, OnboardingActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }


}