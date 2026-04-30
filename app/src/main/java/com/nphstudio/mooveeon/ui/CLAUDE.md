# CLAUDE.md - UI Layer Context

## Responsibility
UI layer chứa tất cả màn hình (Fragments) và Activity duy nhất của app. Mỗi screen là 1 Fragment, navigate qua Jetpack Navigation Component.

## Architecture
- **Single Activity**: `MainActivity.kt` — hosts `NavHostFragment`, manages toolbar
- **Fragments**: Mỗi thư mục con = 1 screen (splash, language, onboarding, home, settings, feed, discover, history, search)
- **ViewBinding**: Tất cả dùng ViewBinding pattern

## Screen Flow
```
Splash → Language → Onboarding → Home → Settings
                                  ├── Feed
                                  ├── Discover
                                  ├── History
                                  └── Search
```

## Key Patterns

### Fragment Template
```kotlin
class XxxFragment : Fragment() {
    private var _binding: FragmentXxxBinding? = null
    private val binding get() = _binding!!
    // onCreateView → inflate binding
    // onViewCreated → setup UI + ads
    // onDestroyView → _binding = null
}
```

### Ad Integration
- Splash: `NphAds.showSplash()` → callback → navigate
- Interstitial: `NphAds.showInterstitial()` → onDismissed/onFailed → navigate
- Banner: `NphAds.loadBannerInto(container, nameSpace)` in `onViewCreated`
- Preload: `NphAds.preload()` cho ads ở màn tiếp theo
- Premium: `NphAds.setPremium(true)` khi IAP success

## Rules
- Check `isAdded` trước navigate trong ad callbacks
- Handle cả `onAdDismissed` + `onAdFailed`
- `NphAds.destroy(this)` trong `MainActivity.onDestroy()`
- nameSpace phải khớp `docs/MO.md`

## Files

| File | Mô tả |
|------|--------|
| `MainActivity.kt` | Single Activity, NavHost, toolbar, NphAds.destroy() |
| `splash/SplashFragment.kt` | Splash screen + App Open ad |
| `language/LanguageFragment.kt` | Language selection + interstitial |
| `onboarding/OnboardingFragment.kt` | Onboarding flow + interstitial |
| `home/HomeFragment.kt` | Main screen, banner, native, preload |
| `settings/SettingsFragment.kt` | Settings, premium toggle, banner |
| `feed/` | Feed feature (TODO) |
| `discover/` | Discover feature (TODO) |
| `history/` | History feature (TODO) |
| `search/` | Search feature (TODO) |
