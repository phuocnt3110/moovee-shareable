package com.nphstudio.mooveeon.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.nphlab.sdk.ads.NphAds
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.ActivityMainBinding
import com.nphstudio.mooveeon.utils.LocaleHelper
import com.nphstudio.mooveeon.utils.TranslationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configure top-level destinations (no back button)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.splashFragment, R.id.languageFragment, R.id.onboardingFragment)
        )

        binding.bottomNav.setupWithNavController(navController)
        setupBottomNav()

        // Hide bottom nav on specific screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.languageFragment, R.id.onboardingFragment, R.id.searchFragment, R.id.feedFragment -> {
                    binding.bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    fun updateNavigationTitles() {
        setupBottomNav()
    }

    private fun setupBottomNav() {
        val menu = binding.bottomNav.menu
        menu.findItem(R.id.homeFragment).title = TranslationHelper.getString("home_title", "Home")
        menu.findItem(R.id.discoverFragment).title = TranslationHelper.getString("discover_title", "Discover")
        menu.findItem(R.id.historyFragment).title = TranslationHelper.getString("history_title", "History")
        menu.findItem(R.id.settingsFragment).title = TranslationHelper.getString("me_title", "Me")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        NphAds.destroy(this)
        super.onDestroy()
    }
}
