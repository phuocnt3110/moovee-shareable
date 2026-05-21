package com.nphstudio.mooveeon.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.FragmentOnboardingBinding
import com.nphstudio.mooveeon.utils.TranslationHelper

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupViewPager()

        binding.tvNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < (binding.viewPager.adapter?.itemCount ?: 0)) {
                binding.viewPager.currentItem = nextItem
            } else {
                navigateToHome()
            }
        }

        binding.tvSkip.setOnClickListener {
            navigateToHome()
        }

        binding.btnGetStarted.setOnClickListener {
            navigateToHome()
        }
    }

    private fun setupUI() {
        binding.tvSkip.text = TranslationHelper.getString("btn_skip", "Skip")
        binding.tvNext.text = TranslationHelper.getString("btn_next", "Next")
    }

    private fun setupViewPager() {
        val items = listOf(
            OnboardingItem(
                TranslationHelper.getString("onboarding_title_1", getString(R.string.onboarding_title_1)),
                TranslationHelper.getString("onboarding_desc_1", getString(R.string.onboarding_desc_1)),
                R.drawable.img_placeholder
            ),
            OnboardingItem(
                TranslationHelper.getString("onboarding_title_2", getString(R.string.onboarding_title_2)),
                TranslationHelper.getString("onboarding_desc_2", getString(R.string.onboarding_desc_2)),
                R.drawable.img_placeholder
            ),
            OnboardingItem(
                TranslationHelper.getString("onboarding_title_3", getString(R.string.onboarding_title_3)),
                TranslationHelper.getString("onboarding_desc_3", getString(R.string.onboarding_desc_3)),
                R.drawable.img_placeholder
            ),
            OnboardingItem(
                TranslationHelper.getString("onboarding_title_4", getString(R.string.onboarding_title_4)),
                TranslationHelper.getString("onboarding_desc_4", getString(R.string.onboarding_desc_4)),
                R.drawable.img_placeholder
            )
        )

        binding.viewPager.adapter = OnboardingAdapter(items)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val isLastPage = position == items.size - 1
                binding.tvNext.isVisible = !isLastPage
                binding.tvSkip.isVisible = !isLastPage
                
                // Cập nhật text cho nút chính nếu cần, ở đây nút "View now" luôn hiện
                binding.btnGetStarted.text = if (isLastPage) {
                    TranslationHelper.getString("btn_get_started", getString(R.string.btn_get_started))
                } else {
                    TranslationHelper.getString("btn_view_now", getString(R.string.btn_view_now))
                }
            }
        })
    }

    private fun navigateToHome() {
        if (!isAdded) return
        findNavController().navigate(R.id.action_onboarding_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
