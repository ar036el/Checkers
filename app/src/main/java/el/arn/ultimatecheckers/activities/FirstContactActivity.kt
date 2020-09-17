/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import el.arn.ultimatecheckers.R


class FirstContactActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goToMatchingActivity()
    }

    private fun goToMatchingActivity() {
        val hasCompletedOnboarding = applicationContext.getSharedPreferences(
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