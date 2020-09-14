package el.arn.checkers.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.android.radioButtons
import el.arn.checkers.managers.preferences_managers.Preference
import el.arn.checkers.managers.preferences_managers.PreferencesManager


class OnboardingActivity : AppCompatActivity() {

    val settingsPreferencesManager = appRoot.settingsPreferencesManager

    val onboardingPagesID = arrayOf(
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3,
        R.layout.onboarding_page4,
        R.layout.onboarding_page5,
        R.layout.onboarding_page6
    )
    val totalPages = onboardingPagesID.size

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

    //used for 'onclick'
    fun onPreferenceRadioButtonClick(view: View) {
        val radioButton = view as RadioButton
        val radioGroup = view.parent as RadioGroup
        val pref = settingsPreferencesManager.getPrefByKey(radioGroup.tag as String)
        val prefSelection = radioButton.tag as String
        when (pref.defaultValue) {
            is String -> {
                pref.value = prefSelection
            }
            is Boolean -> {
                pref.value = prefSelection.toBoolean()
            }
            else -> {
                throw InternalError("Unhandled type")
            }
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
                declareOnPrefsOnboardingHasCompleted()
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


    private fun declareOnPrefsOnboardingHasCompleted() {
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

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */


    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getCount(): Int = totalPages

        override fun getItem(position: Int): Fragment =
            ScreenSlidePageFragment.newInstance(
                onboardingPagesID[position]
            )
    }


    class ScreenSlidePageFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val fragment = inflater.inflate(requireArguments().getInt("fragmentID"), container, false)

            //on pages 2-5
            val preferenceRadioGroup = fragment.findViewById<RadioGroup>(R.id.preferenceRadioGroup)
            //on page 6
            val selectPieceContainer = fragment.findViewById<ConstraintLayout>(R.id.onboarding_selectPiece_container)

            if (preferenceRadioGroup != null) {
                val pref = appRoot.settingsPreferencesManager.getPrefByKey(preferenceRadioGroup.tag as String)
                val radioButtons = preferenceRadioGroup.radioButtons

                val radioButtonToBeSelected: RadioButton = when (pref.value) {
                    is String -> {
                        radioButtons.first { it.tag.toString() == pref.value }
                    }
                    is Boolean -> {
                        radioButtons.first { it.tag.toString().toBoolean() == pref.value }
                    }
                    else -> throw InternalError("unhandled type")
                }

                radioButtonToBeSelected.isChecked = true

            } else if (selectPieceContainer != null) {
                val piecesSelectionAreas = listOf<ImageView> (
                    fragment.findViewById(R.id.onboarding_selectPiece_0_selectionArea),
                    fragment.findViewById(R.id.onboarding_selectPiece_1_selectionArea),
                    fragment.findViewById(R.id.onboarding_selectPiece_2_selectionArea)
                )
                val pref = appRoot.settingsPreferencesManager.playersTheme
                val selectedPieceTheme = piecesSelectionAreas.first { piecesSelectionAreas.indexOf(it) == pref.value}

                piecesSelectionAreas.forEach { it.setImageResource(R.color.transparent) }
                selectedPieceTheme.setImageResource(R.color.buttonSelected)
            }

            return fragment
        }


        companion object {
            fun newInstance(fragmentID: Int): ScreenSlidePageFragment {
                val args = Bundle()
                args.putInt("fragmentID", fragmentID)
                val f = ScreenSlidePageFragment()
                f.arguments = args
                return f
            }
        }
    }
}



