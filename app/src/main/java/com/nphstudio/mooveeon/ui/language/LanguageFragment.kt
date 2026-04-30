package com.nphstudio.mooveeon.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nphlab.sdk.ads.AdError
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.FragmentLanguageBinding
import com.nphstudio.mooveeon.ui.MainActivity
import com.nphstudio.mooveeon.utils.LocaleHelper
import com.nphstudio.mooveeon.utils.TranslationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        
        // Preload next interstitial
        NphAds.preload(requireActivity(), "nsp-interstitial-onboarding-fullscreen-complete")

        binding.btnContinue.setOnClickListener {
            applyLanguage()
        }
        
        binding.ivDone.setOnClickListener {
            applyLanguage()
        }
        
        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-language-bottom-auto")
    }

    private fun setupUI() {
        binding.tvTitle.text = TranslationHelper.getString("select_language", getString(R.string.select_language))
        binding.btnContinue.text = TranslationHelper.getString("btn_continue", getString(R.string.btn_continue))
        binding.tvLoading.text = TranslationHelper.getString("applying_language", "Applying language...")
        binding.tvSuccess.text = TranslationHelper.getString("changes_applied", "Changes applied!")
    }

    private fun applyLanguage() {
        val selectedLanguage = languageAdapter.getSelectedItem()?.code ?: "en"
        LocaleHelper.setLocale(requireContext(), selectedLanguage)
        
        // Update UI immediately with new translations
        setupUI()
        (activity as? MainActivity)?.updateNavigationTitles()

        lifecycleScope.launch {
            // Hide main UI elements
            binding.rvLanguages.isVisible = false
            binding.btnContinue.isVisible = false
            binding.ivDone.isVisible = false

            binding.clLoading.isVisible = true
            delay(1200)
            binding.clLoading.isVisible = false
            binding.clSuccess.isVisible = true
            delay(1000)
            
            navigateNext()
        }
    }

    private fun setupRecyclerView() {
        val languages = listOf(
            LanguageItem(R.string.lang_hindi, R.drawable.ic_flag_hi, "hi"),
            LanguageItem(R.string.lang_french, R.drawable.ic_flag_fr, "fr"),
            LanguageItem(R.string.lang_english, R.drawable.ic_flag_en, "en"),
            LanguageItem(R.string.lang_spanish, R.drawable.ic_flag_es, "es"),
            LanguageItem(R.string.lang_portuguese, R.drawable.ic_flag_pt, "pt"),
            LanguageItem(R.string.lang_arabic, R.drawable.ic_flag_ar, "ar"),
            LanguageItem(R.string.lang_bulgarian, R.drawable.ic_flag_bg, "bg"),
            LanguageItem(R.string.lang_czech, R.drawable.ic_flag_cz, "cs"),
            LanguageItem(R.string.lang_danish, R.drawable.ic_flag_dk, "da"),
            LanguageItem(R.string.lang_german, R.drawable.ic_flag_de, "de"),
            LanguageItem(R.string.lang_greek, R.drawable.ic_flag_gr, "el"),
            LanguageItem(R.string.lang_urdu, R.drawable.ic_flag_ur, "ur"),
            LanguageItem(R.string.lang_chinese_simplified, R.drawable.ic_flag_zh, "zh-hans"),
            LanguageItem(R.string.lang_chinese_traditional, R.drawable.ic_flag_zh, "zh-hant"),
            LanguageItem(R.string.lang_vietnamese, R.drawable.ic_flag_vi, "vi"),
            LanguageItem(R.string.lang_brazilian, R.drawable.ic_flag_br, "pt-br")
        )

        val currentLang = if (LocaleHelper.isLanguageSelected(requireContext())) {
            LocaleHelper.getLanguage(requireContext())
        } else {
            "en"
        }

        val initialPosition = languages.indexOfFirst { it.code == currentLang }.let {
            if (it == -1) 2 else it // Default to 2 (English) if not found
        }

        languageAdapter = LanguageAdapter(languages) {
            // Handle language selection if needed
        }
        languageAdapter.setSelectedPosition(initialPosition)

        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = languageAdapter
            scrollToPosition(initialPosition)
        }
    }

    private fun navigateNext() {
        val fromSettings = arguments?.getBoolean("fromSettings", false) ?: false
        val actionId = if (fromSettings) R.id.action_language_to_home else R.id.action_language_to_onboarding

        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-language-fullscreen-complete",
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigate(actionId)
                }
                override fun onAdFailed(error: AdError) {
                    if (isAdded) findNavController().navigate(actionId)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
