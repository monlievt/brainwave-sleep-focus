# AGENTS.md
## Project: Aplikasi Binaural Beats / Brainwave / Sleep & Focus
### Android First — AI Coding Agent Reference

> **Format:** Dokumen ini mengikuti [AGENTS.md spec](https://github.com/agentsmd/agents.md).
> Dibaca otomatis oleh AI coding agent di setiap sesi. Jangan hapus atau rename file ini.

---

## 1. IDENTITAS PROJECT

| Field | Value |
|---|---|
| **Nama project** | Binaural Beats / Brainwave / Sleep & Focus |
| **App display name** | **Brainwave — Sleep & Focus** |
| **Application ID (package name)** | **`com.monliev.brainwave`** — JANGAN diubah setelah rilis pertama |
| **Platform target** | Android first (API level 24 / Android 7.0 minimum), iOS menyusul di Fase 5 |
| **Bahasa implementasi** | Kotlin (Android SDK native) |
| **Bahasa UI** | **Bahasa Inggris** — semua string resource, copy UI, empty state, tombol, label |
| **Fase aktif** | Fase 1 — MVP Android (fitur MUST HAVE) |
| **Workspace root** | `/Volumes/Backup/Antigravity/Binaural Beats/` |
| **Docs directory** | `/Volumes/Backup/Antigravity/Binaural Beats/docs/` |
| **Preset JSON files** | `assets/presets/` — 20 file sudah tersedia, langsung pakai |

---

## 2. HIERARKI DOKUMEN — BACA SEBELUM MULAI

Empat dokumen wajib dibaca sebelum mengerjakan task apapun. Urutan prioritas jika ada konflik:

```
prd-main.md  →  menang untuk scope/fitur/prioritas
     ↓
audio-engine-spec.md  →  menang untuk implementasi teknis audio
     ↓
ux-flow-screen-spec.md  →  menang untuk perilaku UI & navigasi
     ↓
design-system.md  →  menang untuk visual & komponen
```

| Dokumen | Path | Otoritas |
|---|---|---|
| `prd-main.md` | `docs/prd-main.md` | Scope, fitur, MoSCoW prioritas |
| `audio-engine-spec.md` | `docs/audio-engine-spec.md` | Kontrak teknis audio — angka dan urutan di sini bersifat **final** |
| `ux-flow-screen-spec.md` | `docs/ux-flow-screen-spec.md` | Screen-by-screen spec, state, navigasi |
| `design-system.md` | `docs/design-system.md` | Token warna, tipografi, komponen |
| `legal/privacy-policy.md` | `legal/privacy-policy.md` | Template Privacy Policy — wajib difinalisasi sebelum submit Play Store |
| `legal/terms-of-service.md` | `legal/terms-of-service.md` | Template Terms of Service — wajib difinalisasi sebelum submit Play Store |

---

## 3. KEPUTUSAN YANG SUDAH FINAL — JANGAN DISKUSIKAN ULANG

Langsung eksekusi tanpa bertanya:

### UI & Copy
- **Bahasa UI: Bahasa Inggris** — semua string harus masuk `strings.xml`, tidak boleh hardcoded
- String copy contoh di `ux-flow-screen-spec.md` ("Oops! Your library is empty", "You have no sessions", dll) sudah dalam Bahasa Inggris — pakai langsung as-is
- Struktur `strings.xml` WAJIB siap di-localize (key naming konsisten, tidak ada hardcoded string tersebar)

### Audio Engine
- Real-time synthesis untuk binaural tone (bukan pre-rendered file/streaming)
- **Phase accumulation** — BUKAN absolute-time sine calculation (lihat `audio-engine-spec.md` §2.3)
- `double` (float64) untuk akumulasi fase, `float32` hanya untuk buffer output ke hardware
- `AudioTrack MODE_STREAM` sebagai API audio (bukan MediaPlayer, bukan SoundPool)
- Format preset: array-of-steps, BUKAN single value
- **Soft limiter `tanh()`** di akhir pipeline mixing — tanpa kecuali, setiap sample, kedua channel
- Background noise (white/pink/brown) via kode sintetis, bukan file aset
- Noise ditambahkan **identik ke kedua channel** — tidak di-binaural-kan

### Arsitektur
- Foreground Service wajib untuk background playback (layar mati)
- **Foreground Service restart policy: `START_STICKY`** — jika OS kill service di tengah sesi, service di-restart. State sesi tidak bisa di-resume dari posisi yang sama, tapi lebih baik daripada diam tanpa recovery
- Audio generation di dedicated thread dengan prioritas `THREAD_PRIORITY_AUDIO`
- Komunikasi UI → audio thread via mekanisme thread-safe (AtomicReference/lock-free queue)
- 44100 Hz sample rate, 100ms block size (4410 sample/channel per block)
- Dark mode sebagai tema **default** saat install pertama

### Monetisasi & Side Menu
- **Banner premium di Side Menu: INCLUDE di MVP** — tampilkan banner promosi fitur premium milik sendiri (bukan third-party ads). Banner ini adalah placeholder untuk fitur Library/Sessions/Mixer yang akan unlock di versi berbayar. Copy dan visual banner ditentukan saat Monetization Doc selesai; untuk MVP gunakan placeholder dengan teks "Unlock Premium Features"

### Legal
- **Privacy Policy & Terms of Service**: template tersedia di `legal/privacy-policy.md` dan `legal/terms-of-service.md`. Wajib difinalisasi (di-review tim legal/owner) sebelum submit ke Play Store. Agent boleh reference link dokumen ini dari About Screen, tapi wajib note bahwa ini still template
- About Screen harus menampilkan disclaimer verbatim (lihat Bagian 10) dan link ke Privacy Policy & ToS

### Library & Dependencies (sudah final — langsung pakai, tidak perlu tanya)

| Library | Pilihan | Kegunaan |
|---|---|---|
| JSON parsing | **`kotlinx.serialization`** | Parse preset JSON dari `assets/presets/` |
| Navigation | **Jetpack Navigation Component** | Bottom nav 3-tab, back stack management |
| Dependency Injection | **Hilt** | ViewModel, Service, Repository injection |
| Local storage / preferences | **DataStore (Preferences)** | Theme preference, first-launch flag, timer state |
| Concurrency | **Kotlin Coroutines + Flow** | Audio thread management, UI state |
| Testing | **JUnit 4 + MockK** | Unit test untuk audio engine modules |

### Preset JSON Files
- Semua 20 preset tersedia di `assets/presets/` — satu file per preset
- Format mengikuti schema `audio-engine-spec.md` §4.1
- Saat build, copy ke `app/src/main/assets/presets/` — dibaca via `AssetManager` saat Splash Screen
- Jangan re-generate atau modifikasi data beat Hz tanpa instruksi dari tim konten

### Unit Test — Gate per Tahap Build Order
Empat unit test (§6.3 `audio-engine-spec.md`) wajib **lulus secara manual** sebelum setiap tahap dinyatakan selesai. Tidak perlu CI server untuk MVP — cukup jalankan test suite lewat Android Studio / `./gradlew test` dan pastikan green sebelum lanjut ke tahap berikutnya:

| Test | File | Kapan dijalankan |
|---|---|---|
| Clipping test | `ClippingTest.kt` | Wajib lulus sebelum Tahap 1 ✓ |
| DC offset test | `DcOffsetTest.kt` | Wajib lulus sebelum Tahap 3 ✓ |
| FFT frequency test | `FftFrequencyTest.kt` | Wajib lulus sebelum Tahap 1 ✓ |
| Phase continuity test | `PhaseContinuityTest.kt` | Wajib lulus sebelum Tahap 2 ✓ |

### Build Order (ikuti urutan ini, tidak boleh dilompati)
1. Single stable tone tanpa UI → lulus acceptance criteria FFT + clipping test
2. Transition step + phase continuity → tidak ada klik/pop di sambungan antar step
3. Background noise + mixing pipeline + soft limiter
4. Preset system + JSON parser + validator (20 preset)
5. Background playback + foreground service + WakeLock
6. Kontrol sesi: sleep timer, cross-fade, pause/resume
7. Device detection: headphone vs speaker, auto-pause saat headphone dicabut

---

## 4. KEPUTUSAN TERBUKA — PROTOKOL WAJIB

Semua item di bawah ini masuk **Kelas DEFAULT** — agent boleh langsung proceed dengan nilai default yang tertulis, tapi wajib mencatat di code comment bahwa ini adalah default assumption yang bisa di-override:

| Item | Default yang dipakai |
|---|---|
| Font family | Roboto (system Android) |
| Icon set | Material Symbols Outlined (konsisten satu keluarga, jangan campur) |
| Theme switch mechanism | DataStore terpusat (bukan resource qualifier) |
| AudioTrack vs Oboe | AudioTrack murni; upgrade ke Oboe HANYA JIKA testing device menunjukkan audio glitch/underrun yang tidak bisa diatasi dengan tuning buffer size |
| Carrier Hz preset Solfeggio | 200 Hz (default engine) |
| Ilustrasi Player Screen | Animasi halus (opacity/scale breathing loop, bukan statis) |
| Volume control UI | Inline slider expand di bawah transport controls |
| Close sesi (X button) | Langsung keluar tanpa dialog konfirmasi, fade-out 200-300ms |
| Breathing Exercise di MVP | **Tombol disembunyikan di MVP**, diaktifkan di rilis 1.1 |
| Side menu | Bottom sheet |

### Item yang Ditunda ke Pre-Release (jangan dikerjakan sekarang)

| Item | Kapan dikerjakan |
|---|---|
| **Deep link dari notifikasi foreground service** | Tahap akhir sebelum rilis, bersamaan dengan input Google AdMob ID |
| **Google AdMob integration** | Tahap akhir sebelum rilis |

---

## 5. ATURAN KERJA WAJIB

### 5.1 Urutan implementasi
- **WAJIB** mengikuti Build Order §8 `audio-engine-spec.md` — tidak boleh melompat tahap
- Setiap tahap harus lulus **semua** acceptance criteria-nya sebelum lanjut ke tahap berikutnya
- Jika tahap N gagal acceptance criteria: perbaiki dulu, jangan lanjut ke N+1

### 5.2 Handling ambiguitas
- Jika menemukan ambiguitas yang **tidak tercakup** di dokumen manapun: **berhenti dan bertanya**, jangan menebak dan lanjut
- Jika menemukan **konflik antar dokumen**: gunakan hierarki di Bagian 2 untuk menentukan mana yang menang; jika tidak bisa diselesaikan dengan hierarki, escalate ke user

### 5.3 Unit test — wajib ada, bukan opsional
Empat test berikut (`audio-engine-spec.md` §6.3) harus **diimplementasikan bersamaan dengan modul yang diuji**, bukan setelah semua selesai:

| Test | File | Kriteria pass |
|---|---|---|
| Clipping test | `ClippingTest.kt` | Tidak ada sample yang melebihi amplitude ±1.0 |
| DC offset test | `DcOffsetTest.kt` | Rata-rata sample dalam window 1 detik mendekati 0 |
| FFT frequency test | `FftFrequencyTest.kt` | Peak channel kiri = carrier − beat/2, kanan = carrier + beat/2, toleransi ±0.5 Hz |
| Phase continuity test | `PhaseContinuityTest.kt` | Tidak ada lonjakan delta sample-to-sample yang tidak wajar di titik transisi |

### 5.4 State handling — tidak ada screen tanpa state lengkap
Setiap screen yang diimplementasikan **wajib** menangani semua state yang didefinisikan di `ux-flow-screen-spec.md`:
- `Normal` (happy path)
- `Loading` (skeleton loader, bukan layar kosong atau spinner blocking)
- `Empty` (jika relevan)
- `Error` (pesan informatif + retry, tidak boleh silent fail)

### 5.5 WakeLock hygiene
- Acquire WakeLock (`PARTIAL_WAKE_LOCK`) hanya saat sesi audio aktif
- Release WakeLock segera setelah: timer habis, user stop, service di-stop, atau error fatal
- **Verifikasi via** `adb shell dumpsys power` — tidak ada wakelock yang bocor setelah sesi berakhir

### 5.6 Aksesibilitas minimum
- Tap target semua tombol interaktif minimum **48×48dp** (termasuk icon visual yang lebih kecil)
- Tombol Play/Pause: 72×72dp
- Slider thumb: 20dp visual, 40dp tap target

---

## 6. ANTI-PATTERNS YANG DILARANG

Hal-hal berikut ditemukan sebagai sumber bug di implementasi binaural beat generator lain dan **tidak boleh ada dalam codebase ini**:

```kotlin
// DILARANG — absolute-time sine, menyebabkan phase jump saat frekuensi berubah
sample[n] = sin(2 * PI * frequency * (n / sampleRate))

// WAJIB — phase accumulation dengan state persisten antar buffer
phase += 2 * PI * frequency * (1.0 / sampleRate)  // double, bukan float
phase = phase.rem(2 * PI)
sample[n] = sin(phase).toFloat()  // konversi ke float32 hanya di output
```

```kotlin
// DILARANG — MediaPlayer atau SoundPool untuk synthesis
mediaPlayer.setDataSource(...)

// WAJIB — AudioTrack MODE_STREAM
AudioTrack(attrs, format, bufferSize, AudioTrack.MODE_STREAM, sessionId)
```

```kotlin
// DILARANG — menulis ke variabel shared audio dari UI thread tanpa sinkronisasi
audioEngine.frequency = slider.value  // race condition

// WAJIB — thread-safe communication
frequencyRef.set(slider.value)  // AtomicReference atau equivalent
```

```kotlin
// DILARANG — sample dikirim ke AudioTrack tanpa soft limiter
audioTrack.write(rawBuffer, ...)

// WAJIB — soft limiter tanh() setelah semua mixing, sebelum write
val limited = tanh(mixedSample)
audioTrack.write(limitedBuffer, ...)
```

### Peringatan khusus: pseudocode pink noise (audio-engine-spec.md §5.2)
Variabel `b6` digunakan sebelum di-assign di baris `pink = ... + b6 + ...`. Ini **disengaja** — `b6` membawa nilai dari iterasi sebelumnya (state filter). Jangan menukar urutan kedua baris tersebut untuk "memperbaiki" apa yang terlihat seperti use-before-assign. Melakukannya akan merusak karakteristik filter pink noise.

Implementasi yang benar di Kotlin:
```kotlin
// State: b0–b6 diinisialisasi 0.0 di constructor, persist antar panggilan
fun generatePinkNoiseSample(white: Double): Double {
    b0 = 0.99886 * b0 + white * 0.0555179
    b1 = 0.99332 * b1 + white * 0.0750759
    b2 = 0.96900 * b2 + white * 0.1538520
    b3 = 0.86650 * b3 + white * 0.3104856
    b4 = 0.55000 * b4 + white * 0.5329522
    b5 = -0.7616 * b5 - white * 0.0168980
    // INTENTIONAL: b6 below is the value from the PREVIOUS call (state variable)
    // Do NOT reorder these two lines — this is the Paul Kellett filter design
    val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
    b6 = white * 0.115926  // updated AFTER use; takes effect next call
    return pink * 0.11
}
```

---

## 7. STRUKTUR FILE YANG DISARANKAN

```
app/src/main/java/.../
├── audio/
│   ├── core/
│   │   ├── PhaseAccumulator.kt        // oscillator dasar, phase-continuous
│   │   ├── BinauralToneGenerator.kt   // pasangan oscillator kiri/kanan
│   │   ├── NoiseGenerator.kt          // interface + white/pink/brown
│   │   ├── SoftLimiter.kt             // tanh limiter
│   │   └── MixingPipeline.kt          // orkestrasi pipeline §6.1
│   ├── preset/
│   │   ├── PresetModel.kt             // data class skema §4.1
│   │   ├── PresetValidator.kt         // aturan §4.2
│   │   ├── PresetLoader.kt            // JSON parser
│   │   └── SequenceScheduler.kt       // tracking posisi steps
│   └── playback/
│       ├── AudioEngineService.kt      // Foreground Service (START_STICKY)
│       ├── AudioTrackManager.kt       // wrapper AudioTrack
│       ├── SessionController.kt       // play/pause/timer/crossfade §7
│       └── DeviceOutputMonitor.kt     // deteksi headphone §6.4
└── ui/
    ├── home/
    ├── category/
    ├── player/
    ├── library/
    ├── sessions/
    └── common/                        // komponen reusable design system

test/audio/
├── ClippingTest.kt
├── DcOffsetTest.kt
├── FftFrequencyTest.kt
└── PhaseContinuityTest.kt

legal/
├── privacy-policy.md                  // template — wajib difinalisasi sebelum rilis
└── terms-of-service.md               // template — wajib difinalisasi sebelum rilis
```

---

## 8. REFERENSI KONTEN — 20 PRESET MVP

Semua preset menggunakan **carrier default 200 Hz**. Format implementasi: single `stable` step untuk MVP, tapi **struktur data tetap array-of-steps**.

| Kategori | Preset | Beat Hz | Catatan |
|---|---|---|---|
| STUDY | Memory | 4.0 | |
| STUDY | Focus | 14.0 | |
| STUDY | Study Aid | 12.0 | |
| SPIRIT | Trance | 5.5 | |
| SPIRIT | Astral Travel | 7.0 | |
| SPIRIT | Chanting | 4.5 | |
| SPIRIT | Solfeggio | 7.83 | ⚠️ Carrier: default 200 Hz, konfirmasi jika berbeda |
| SPIRIT | Third Eye | 13.0 | |
| SLEEP | Sleep | 2.0 | |
| SLEEP | Deep Sleep | 3.9 | |
| SLEEP | Lucid Dream | 1.5 | |
| BODY | Universal Healing | 1.5 | |
| BODY | Overcome Addiction | 8.0 | |
| BODY | Fatigue Energizer | 20.0 | ⚠️ Hz sama dengan Inflammation Problems — ini data dari riset, jangan ubah tanpa instruksi tim konten |
| BODY | Inflammation Problems | 20.0 | ⚠️ Lihat catatan di atas |
| BRAIN | Intelligence | 15.4 | |
| BRAIN | Creativity | 10.6 | |
| BRAIN | Relaxation | 6.0 | |
| BRAIN | Euphoria | 20.0 | |
| BRAIN | Intuition | 5.5 | |

---

## 9. WARNA KATEGORI — REFERENSI CEPAT

| Kategori | Token | Hex |
|---|---|---|
| STUDY | `color/category/study` | `#FF6B6B` |
| SPIRIT | `color/category/spirit` | `#FFA451` |
| SLEEP | `color/category/sleep` | `#6DD98C` |
| BODY | `color/category/body` | `#5DBEEA` |
| BRAIN | `color/category/brain` | `#8C7CF0` |

Warna kategori dipakai **hanya sebagai aksen** (strip kiri card, icon, kontrol aktif di Player Screen) — tidak sebagai background penuh.

---

## 10. DISCLAIMER WAJIB (VERBATIM — JANGAN PARAFRASE)

Teks ini harus muncul **verbatim** (persis seperti ini) di:
1. Onboarding slide 3 (dengan checkbox "I understand")
2. About Screen (selalu bisa diakses offline, bukan link keluar)

```
Information about brainwave frequencies and their uses is educational in nature.
The effects of brainwave entrainment vary between individuals and are not intended
to diagnose, treat, cure, or prevent any disease or medical condition.
Consult a medical professional before using audio entrainment tools, especially
if you have a specific medical condition (including epilepsy) or are pregnant.
Do not use while driving or operating machinery.
```

Copy marketing dan deskripsi preset **dilarang** menggunakan kata: "cure", "treat", "therapy", "heal". Gunakan bahasa deskriptif-edukasional: "associated with", "good for", "helps with".

---

## 11. CHECKLIST SEBELUM MULAI CODING

Sebelum mulai implementasi apapun di session baru, verifikasi:

- [ ] Sudah baca dokumen yang relevan untuk task saat ini (lihat Bagian 2)
- [ ] Cek apakah task menyentuh item yang ditunda ke pre-release (Bagian 4, tabel "Ditunda") — jika ya, skip dulu
- [ ] Cek apakah Build Order dilanggar (Bagian 5.1)
- [ ] Pastikan unit test untuk modul yang diimplementasikan ikut ditulis dan dijalankan (Bagian 5.3)
- [ ] Pastikan semua state screen ditangani: Normal, Loading, Error, Empty (Bagian 5.4)
- [ ] Semua string UI masuk `strings.xml` dalam Bahasa Inggris, tidak ada yang hardcoded

---

*Dokumen ini dibuat berdasarkan analisis `docs/` project per 2026-08-15. Update dokumen ini jika ada keputusan baru yang mengubah asumsi atau aturan di atas.*
