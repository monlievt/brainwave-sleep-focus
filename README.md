# Brainwave — Sleep & Focus (Android)

An Android-first native application designed to deliver real-time synthesized binaural beats, cognitive entrainment sequences, and mindfulness exercises for sleep, study, and deep relaxation. Built entirely in modern native Kotlin, Jetpack Compose, and Android SDK APIs.

---

## 📱 Features & Highlights

- **Real-Time Binaural Synthesis**: Real-time phase-continuous sound wave generation (no pre-rendered files or streaming).
- **Core Category Presets**: 20 research-backed presets targeting Alpha, Beta, Delta, Theta, and Gamma cognitive states (Study, Sleep, Brain, Spirit, Body categories).
- **Advanced Dynamic Visualizer**: Captivating wave animations reacting in real time to carriers and active beat frequencies.
- **Sleep & Bedtime Scheduler**: Set automatic bedtime alarms that launch specific presets (Standard/Free users can watch Rewarded Ads to temporarily bypass this premium lock).
- **Daily Streak & Statistics**: Track mindfulness progress, streaking patterns, and minutes listened using native clean custom bar charts.
- **Guided Breathing Exercises**: Visual and interactive breathing companion with custom inhalation, holding, and exhalation loops.
- **Premium Model**: Simulates Google Play Billing for Lifetime/Monthly upgrades, suppressing all banner and native Google AdMob ads.
- **AdMob Ecosystem**: Dynamically positioned native ads and loop-prevented App Open and Interstitial ads.
- **Offline Legal Compliance**: Complete, offline-readable Privacy Policy and Terms of Service embedded directly in the app.

---

## 🛠️ Technical Specifications & Architecture

### Audio Generation
- **Phase Accumulation**: Uses phase accumulation (`double` precision) to prevent click/pop artifacts on frequency transitions instead of absolute-time calculation.
- **Mixing Pipeline & Soft Limiter**: Custom pipeline mixing binaural tones with synthesized background noise (white, pink, brown) backed by a hard-limiting `tanh()` filter to prevent output clipping.
- **Low-level API**: Implemented via `AudioTrack` in `MODE_STREAM` (rather than MediaPlayer or SoundPool) operating on a dedicated priority audio thread (`THREAD_PRIORITY_AUDIO`).
- **Foreground Playback**: Relies on a `START_STICKY` Foreground Service with partial `WakeLocks` to guarantee uninterrupted playback even when the screen is turned off.
- **Audio Hygiene**: Detects headphone disconnection dynamically and auto-pauses playback to avoid public speaker blasting.

### Modern Android Tech Stack
- **UI Framework**: 100% Jetpack Compose with custom HSL dark mode palettes.
- **Navigation**: Jetpack Navigation 3 with serializable `NavKey` type safety.
- **Local Storage**: Room DB (alarms, history logs) + DataStore Preferences (theme preferences, session states, daily streaks).
- **JSON Parser**: `kotlinx.serialization` for assets management.
- **Dependency Injection**: Hilt (hilt-navigation-compose).

---

## 📁 Repository Structure

```
.
├── app/src/main/java/.../
│   ├── audio/
│   │   ├── core/              # Synthesizers, Mixing Pipeline, Soft Limiter
│   │   ├── preset/            # JSON preset loader, Sequence schedulers, validators
│   │   └── playback/          # Audio Foreground Service, AudioTrack manager, AdMob manager
│   ├── data/                  # Room DB entities, DAOs, repositories
│   └── ui/
│       ├── main/              # HomeScreen, PlayerScreen, Statistics, Paywall, Alarms
│       └── theme/             # Custom theme colors and tokens
├── assets/presets/            # 20 preloaded category preset JSON files
├── docs/                      # Original PRD, Design System, Audio Engine, and UX specifications
├── legal/                     # Privacy Policy and Terms of Service markdown files
├── build.gradle.kts           # Workspace configuration
└── README.md                  # Project overview
```

---

## 🚀 Building and Running

### Prerequisites
- Android Studio Koala / Ladybug or newer.
- Android SDK 24+ (Minimum API level 24 / Android 7.0 target).
- JDK 17.

### Compilation
Build the project using Gradle:
```bash
./gradlew assembleDebug
```

To run unit tests verifying the audio engine modules:
```bash
./gradlew test
```

### Deploying
Install and launch on a connected device:
```bash
./gradlew installDebug
```

---

## 🧪 Unit Test Suite

Four critical test files are executed to protect audio synthesis characteristics:

| Unit Test | Target File | Verification Criteria |
|---|---|---|
| **Clipping Test** | `ClippingTest.kt` | Ensures no mixing output samples exceed amplitude `±1.0`. |
| **DC Offset Test** | `DcOffsetTest.kt` | Confirms the sample mean over a 1-second window approaches 0. |
| **FFT Frequency Test** | `FftFrequencyTest.kt` | Validates left carrier frequency is `carrier - beat/2` and right is `carrier + beat/2` (tolerance `±0.5` Hz). |
| **Phase Continuity Test** | `PhaseContinuityTest.kt` | Guarantees zero clicking/popping transients across step transitions. |

---

## 📄 License & Terms

Information regarding brainwave frequencies is educational in nature. Developed by **Monliev Labs** (`support@monliev.com`). Templates for Privacy Policy and Terms of Service are located in the `/legal` directory.
