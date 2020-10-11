/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.arealapps.ultimatecheckers.R
import com.arealapps.ultimatecheckers.appRoot
import com.arealapps.ultimatecheckers.gameCore.game_core.checkers_game.configurations.GameLogicConfig
import com.arealapps.ultimatecheckers.helpers.android.SingleSelectionButtonGroup
import com.arealapps.ultimatecheckers.managers.preferences_managers.Preference
import com.arealapps.ultimatecheckers.managers.preferences_managers.PreferencesManager


class OnboardingActivity : AppCompatActivity() {

    val pagesID = arrayOf(
        R.layout.onboarding_page0,
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3,
        R.layout.onboarding_page4,
        R.layout.onboarding_page5
    )
    val totalPages = pagesID.size

    private lateinit var pager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)


        initPagerMechanism()

        appRoot.gamePreferencesManager.addListener(object: PreferencesManager.Listener {
            override fun prefsHaveChanged(changedPreference: Preference<*>) {
                println("yayo key:" + changedPreference.key + "  value:" + changedPreference.value)
            }
        })

    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            pager.currentItem = pager.currentItem - 1
        }
    }


    private fun initPagerMechanism() {
        pager = findViewById(R.id.pager)
        val tabIndicator: TabLayout = findViewById(R.id.tab_indicator)
        val centerBtn: Button = findViewById(R.id.btn_center)
        val prevBtn: TextView = findViewById(R.id.btn_prev)

        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        tabIndicator.setupWithViewPager(pager)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                prevBtn.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                if (position == totalPages - 1) {
                    centerBtn.text = getString(R.string.onboardingActivity_buttonFinish)
                    tabIndicator.visibility = View.INVISIBLE
                } else {
                    centerBtn.text = getString(R.string.onboardingActivity_buttonNext)
                    tabIndicator.visibility = View.VISIBLE
                }
            }
            override fun onPageScrollStateChanged(a: Int) {}
            override fun onPageScrolled(a: Int, b: Float, c: Int) {}
        })

        centerBtn.setOnClickListener {
            if (pager.currentItem == totalPages - 1) {
                declareInPrefsThatOnboardingHasCompleted()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                pager.currentItem += 1
            }
        }

        prevBtn.setOnClickListener {
            pager.currentItem -= 1
        }


        //        tabIndicator.addOnTabSelectedListener(
//            object : TabLayout.ViewPagerOnTabSelectedListener(mPager) {
//                override fun onTabSelected(tab: TabLayout.Tab) {
//                    if (tab.position == pagerAdapter.count - 1) {
//                    }
//                }
//            })


//        mainBtn.setOnClickListener {
//            val mainActivity = Intent(applicationContext, MainActivity::class.java)
//            startActivity(mainActivity)

//            val sharedPref: SharedPreferences =
//                applicationContext.getSharedPreferences(
//                    "onboarding",
//                    Context.MODE_PRIVATE
//                )
//            val editor = sharedPref.edit()
//            editor.putBoolean(PrefKeys.ONBOARDING.HAS_COMPLETED_ONBOARDING, true)
//            editor.apply()
//
//            finish()
//        }

    }


    private fun declareInPrefsThatOnboardingHasCompleted() {
        val sharedPref: SharedPreferences =
            applicationContext.getSharedPreferences(
                resources.getString(R.string.internal_prefFileKey_onboarding),
                Context.MODE_PRIVATE
            )
        val editor = sharedPref.edit()
        editor.putBoolean(
            resources.getString(R.string.internal_prefFileKey_hasCompletedOnboarding),
            true
        )
        editor.apply()
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getCount(): Int = totalPages

        override fun getItem(position: Int): Fragment =
            ScreenSlidePageFragment.newInstance(pagesID[position], position)
    }

    class ScreenSlidePageFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val pageId = requireArguments().getInt("pageId")
            val pageIndexPos = requireArguments().getInt("pageIndexPos")
            val pageFragment = inflater.inflate(pageId, container, false)

            when (pageIndexPos) {
                0 -> Unit //do nothing. no group is here
                1 -> { createSelectionGroupsForIsCapturingMandatory(pageFragment) }
                2 -> { createSelectionGroupsForKingBehaviour(pageFragment) }
                3 -> { createSelectionGroupsForCanPawnCaptureBackwards(pageFragment) }
                4 -> { createSelectionGroupsForBoardSize(pageFragment) }
                5 -> { createSelectionGroupsForPlayerTheme(pageFragment) }
            }

            return pageFragment
        }

        private fun createSelectionGroupsForIsCapturingMandatory(page: View) {
            SingleSelectionButtonGroup(
                appRoot.settingsPreferencesManager.isCapturingMandatory,
                setOf(
                    page.findViewById<RadioButton>(R.id.onboardingPage1_isCapturingMandatory_radioButton_true) to true,
                    page.findViewById<RadioButton>(R.id.onboardingPage1_isCapturingMandatory_radioButton_false) to false
                ),
                {
                    it.isChecked = true
                },
                { /*do nothing*/ }
            )
        }
        private fun createSelectionGroupsForKingBehaviour(page: View) {
            SingleSelectionButtonGroup(
                appRoot.settingsPreferencesManager.kingBehaviour,
                setOf(
                    page.findViewById<RadioButton>(R.id.onboardingPage2_kingBehaviour_radioButton_flyingKings) to GameLogicConfig.KingBehaviourOptions.FlyingKings.id,
                    page.findViewById<RadioButton>(R.id.onboardingPage2_kingBehaviour_radioButton_landsRightAfterCapture) to GameLogicConfig.KingBehaviourOptions.LandsRightAfterCapture.id,
                    page.findViewById<RadioButton>(R.id.onboardingPage2_kingBehaviour_radioButton_noFlyingKings) to GameLogicConfig.KingBehaviourOptions.NoFlyingKings.id
                ),
                {
                    it.isChecked = true
                },
                { /*do nothing*/ }
            )
        }
        private fun createSelectionGroupsForCanPawnCaptureBackwards(page: View) {
            SingleSelectionButtonGroup(
                appRoot.settingsPreferencesManager.canPawnCaptureBackwards,
                setOf(
                    page.findViewById<RadioButton>(R.id.onboardingPage3_canPawnCaptureBackwards_radioButton_always) to GameLogicConfig.CanPawnCaptureBackwardsOptions.Always.id,
                    page.findViewById<RadioButton>(R.id.onboardingPage3_canPawnCaptureBackwards_radioButton_onlyWhenMultiCapture) to GameLogicConfig.CanPawnCaptureBackwardsOptions.OnlyWhenMultiCapture.id,
                    page.findViewById<RadioButton>(R.id.onboardingPage3_canPawnCaptureBackwards_radioButton_never) to GameLogicConfig.CanPawnCaptureBackwardsOptions.Never.id
                ),
                {
                    it.isChecked = true
                },
                { /*do nothing*/ }
            )
        }
        private fun createSelectionGroupsForBoardSize(page: View) {
            SingleSelectionButtonGroup(
                appRoot.settingsPreferencesManager.boardSizeRegular,
                setOf(
                    page.findViewById<RadioButton>(R.id.onboardingPage4_boardSize_radioButton_8x8) to "8",
                    page.findViewById<RadioButton>(R.id.onboardingPage4_boardSize_radioButton_10x10) to "10",
                    page.findViewById<RadioButton>(R.id.onboardingPage4_boardSize_radioButton_12x12) to "12"
                ),
                {
                    it.isChecked = true
                },
                { /*do nothing*/ }
            )
        }
        private fun createSelectionGroupsForPlayerTheme(page: View) {
            SingleSelectionButtonGroup(
                appRoot.settingsPreferencesManager.playersTheme,
                setOf(
                    page.findViewById<ImageView>(R.id.onboardingPage5_playersTheme_theme0) to 0,
                    page.findViewById<ImageView>(R.id.onboardingPage5_playersTheme_theme1) to 1,
                    page.findViewById<ImageView>(R.id.onboardingPage5_playersTheme_theme2) to 2
                ),
                {
                    it.setImageResource(R.color.buttonSelected)
                },
                {
                    it.setImageResource(R.color.transparent)
                }
            )
        }

        companion object {
            fun newInstance(@IdRes pageId: Int, pageIndexPos: Int): ScreenSlidePageFragment {
                val args = Bundle()
                args.putInt("pageId", pageId)
                args.putInt("pageIndexPos", pageIndexPos)
                val f = ScreenSlidePageFragment()
                f.arguments = args
                return f
            }
        }
    }

}





