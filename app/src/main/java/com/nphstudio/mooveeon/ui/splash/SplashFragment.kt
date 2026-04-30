package com.nphstudio.mooveeon.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nphlab.sdk.ads.NphAds
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.FragmentSplashBinding
import com.nphstudio.mooveeon.utils.LocaleHelper

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Preload first interstitial to ensure it's ready after language selection
        NphAds.preload(requireActivity(), "nsp-interstitial-language-fullscreen-complete")

        NphAds.showSplash(requireActivity()) {
            if (isAdded) {
                if (LocaleHelper.isLanguageSelected(requireContext())) {
                    findNavController().navigate(R.id.action_splash_to_home)
                } else {
                    findNavController().navigate(R.id.action_splash_to_language)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
