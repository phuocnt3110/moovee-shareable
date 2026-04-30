package com.nphstudio.mooveeon.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nphlab.sdk.ads.NphAds
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.FragmentSettingsBinding
import com.nphstudio.mooveeon.utils.TranslationHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()

        // Load banner ad
        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-settings-bottom-auto")

        binding.btnFavorites.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("start_tab", 1) // 1 for Favorites
            }
            findNavController().navigate(R.id.historyFragment, bundle)
        }

        binding.btnHistory.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("start_tab", 0) // 0 for History
            }
            findNavController().navigate(R.id.historyFragment, bundle)
        }

        binding.btnLanguage.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("fromSettings", true)
            }
            findNavController().navigate(R.id.languageFragment, bundle)
        }

        binding.btnPremium.setOnClickListener {
            Toast.makeText(requireContext(), "Premium feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        binding.btnPremium.text = TranslationHelper.getString("btn_premium", getString(R.string.btn_premium))
        binding.tvFavorites.text = TranslationHelper.getString("favorites", "Favorites")
        binding.tvHistory.text = TranslationHelper.getString("history", "History")
        binding.tvLanguage.text = TranslationHelper.getString("language", "Language")
        binding.tvRate.text = TranslationHelper.getString("rate_app", "Rate App")
        binding.tvShare.text = TranslationHelper.getString("share_app", "Share App")
        binding.tvPrivacy.text = TranslationHelper.getString("privacy_policy", "Privacy Policy")
        binding.tvTerms.text = TranslationHelper.getString("terms_of_service", "Term of Service")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
