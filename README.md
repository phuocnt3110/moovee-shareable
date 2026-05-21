# MooveeOn

A short-form video drama streaming app built with Kotlin and Jetpack components.

## Features

- Browse trending and new release drama series
- Watch episodes with vertical swipe feed (TikTok-style)
- Playback speed control
- Episode list with tabbed pagination
- Watch history tracking
- Favorites management
- Multi-language support (16 languages)
- Onboarding flow
- Search functionality

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 24
- **Architecture:** Single Activity + Navigation Component
- **Video Player:** AndroidX Media3 (ExoPlayer)
- **Image Loading:** Glide
- **Database:** Room
- **Networking:** Retrofit + OkHttp
- **UI:** Material Design 3, ViewBinding, ConstraintLayout

## Getting Started

```bash
git clone https://github.com/phuocnt3110/moovee-shareable.git
cd moovee-shareable
./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/nphstudio/mooveeon/
├── MyApp.kt                    # Application class
├── data/
│   ├── local/                  # Room database, DAOs, entities
│   ├── model/                  # Data models (DramaSeries, Episode)
│   ├── remote/                 # Retrofit API service
│   └── repository/             # Data repository
├── ui/
│   ├── MainActivity.kt         # Single Activity + NavHost
│   ├── splash/                 # Splash screen
│   ├── language/               # Language selection
│   ├── onboarding/             # Onboarding flow
│   ├── home/                   # Home screen (trending, new releases)
│   ├── feed/                   # Video feed (vertical swipe player)
│   ├── discover/               # Discover/browse
│   ├── history/                # Watch history & favorites
│   ├── search/                 # Search functionality
│   └── settings/               # App settings
└── utils/                      # Helpers (Locale, Translation)
```

## License

This project is provided for educational and demonstration purposes.
