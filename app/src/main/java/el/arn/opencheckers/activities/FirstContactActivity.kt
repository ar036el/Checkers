/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import el.arn.opencheckers.R
import el.arn.opencheckers.helpers.android.setIntervalRunOnUi
import el.arn.opencheckers.helpers.android.setTimeoutUiCompat


class FirstContactActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTimeoutUiCompat(this, 100) { //doing that to prevent a bug reported from some users
            goToMatchingActivity()
        }
    }

    private fun goToMatchingActivity() {
        val hasCompletedOnboarding = getSharedPreferences(
            resources.getString(R.string.internal_prefFileKey_onboarding),
            Context.MODE_PRIVATE
        ).getBoolean(resources.getString(R.string.internal_prefFileKey_hasCompletedOnboarding), false)

        val intent = if (!hasCompletedOnboarding) {
            Intent(this, OnboardingActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }


}