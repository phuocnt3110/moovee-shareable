# Monetization Specification (MO)

> **TODO:** NPH Lab điền spec monetization trước khi giao cho dev.
> Dev đọc file này để biết đặt ads ở đâu với nameSpace nào.

## Ad Placements

### App Open Ads
| nameSpace | Vị trí | Ghi chú |
|---|---|---|
| `nsp_ao_splash` | Cold start — Splash screen | Show 1 lần khi mở app |
| `nsp_ao_resume` | Warm start — quay lại từ background | Exclude: Premium, Splash |

### Interstitial Ads
| nameSpace | Trigger | interval | stepCount | Ghi chú |
|---|---|---|---|---|
| `nsp_inter_language` | Language → Onboarding | 25s | 1 | |
| `nsp_inter_onboarding` | Onboarding → Home | 25s | 1 | |
| `nsp_inter_settings` | Home → Settings | 25s | 2 | Skip lần đầu |

### Rewarded Ads
| nameSpace | Trigger | Reward | Ghi chú |
|---|---|---|---|
| <!-- nsp_reward_xxx --> | <!-- Mô tả --> | <!-- Reward gì --> | <!-- Ghi chú --> |

### Rewarded Interstitial Ads
| nameSpace | Trigger | Reward | Ghi chú |
|---|---|---|---|
| <!-- nsp_ri_xxx --> | <!-- Mô tả --> | <!-- Reward gì --> | <!-- Opt-in không bắt buộc --> |

### Banner Ads

Types: `banner_adaptive`, `collapsible_top`, `collapsible_bottom`

| nameSpace | Vị trí | Type | Ghi chú |
|---|---|---|---|
| `nsp_bn_onboarding` | Onboarding — bottom | banner_adaptive | |
| `nsp_bn_home_bottom` | Home — bottom | banner_adaptive | |
| `nsp_bn_settings` | Settings — bottom | banner_adaptive | |

### Native Ads
| nameSpace | Vị trí | Type | Ghi chú |
|---|---|---|---|
| <!-- nsp_native_xxx --> | <!-- Mô tả --> | <!-- native_small/medium --> | <!-- Ghi chú --> |

## Premium Policy
- Premium users: ALL ads disabled via `NphAds.setPremium(true)`
- IAP product ID: <!-- com.nphstudio.appname.premium -->

## Notes
<!-- Ghi chú thêm về chiến lược monetization -->
