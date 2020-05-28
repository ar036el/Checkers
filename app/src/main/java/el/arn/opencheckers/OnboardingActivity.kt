package el.arn.opencheckers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class OnboardingActivity : AppCompatActivity() {

    val onboardingPagesID = arrayOf(
        R.layout.onboarding_page1,
        R.layout.onboarding_page2,
        R.layout.onboarding_page3,
        R.layout.onboarding_page4
    )
    val totalPages = onboardingPagesID.size


    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)



        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter



        val tabIndicator: TabLayout = findViewById(R.id.tab_indicator);
        tabIndicator.setupWithViewPager(mPager);


        val mainBtn: Button = findViewById(R.id.btn_main);
        val prevBtn: TextView = findViewById(R.id.btn_prev);


        mPager.addOnPageChangeListener( object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                prevBtn.visibility = if (position == 0) View.GONE else View.VISIBLE
                if (position == totalPages - 1) {
                    mainBtn.text = getString(R.string.onboarding_btn_finish)
                    tabIndicator.visibility = View.INVISIBLE
                } else {
                    mainBtn.text = getString(R.string.onboarding_btn_next)
                    tabIndicator.visibility = View.VISIBLE
                }
            }
            override fun onPageScrollStateChanged(a: Int) {}
            override fun onPageScrolled(a: Int, b: Float, c: Int) {}
        })



        mainBtn.setOnClickListener {
            if (mPager.currentItem == totalPages - 1) {
                val sharedPref: SharedPreferences =
                    applicationContext.getSharedPreferences(
                        resources.getString(R.string.prefCategory_onboarding),
                        Context.MODE_PRIVATE
                    )
                //getString(R.string.saved_high_score_key) better practice TODO
                val editor = sharedPref.edit()
                editor.putBoolean(resources.getString(R.string.pref_hasCompletedOnboarding), true)
                editor.apply()


                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                mPager.currentItem += 1
            }
        }

        prevBtn.setOnClickListener {
            mPager.currentItem -= 1
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
//            //getString(R.string.saved_high_score_key) better practice TODO
//            val editor = sharedPref.edit()
//            editor.putBoolean(PrefKeys.ONBOARDING.HAS_COMPLETED_ONBOARDING, true)
//            editor.apply()
//
//            finish()
//        }



    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            mPager.currentItem = mPager.currentItem - 1
        }
    }



    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */


    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment = ScreenSlidePageFragment(onboardingPagesID[position])
    }


    class ScreenSlidePageFragment(private val fragmentID: Int) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View = inflater.inflate(fragmentID, container, false)
    }
}

//TODo most of android:/ res should be imported locally