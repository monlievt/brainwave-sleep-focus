# AUDIO ENGINE SPEC
## Aplikasi Binaural Beats / Brainwave / Sleep & Focus — Android First

**Versi dokumen:** 1.0
**Status:** Siap dieksekusi oleh AI coding agent
**Referensi riset:** jlar0che/BrainWave, horatio-sans-serif/binaural-beats, elder-plinius/binaural-beats-generator, Aegean-E/NeuralBeat, ksylvan/binaural-generator, katiejenkinswebdev/ZenOut

---

## 0. CARA MEMBACA DOKUMEN INI

Dokumen ini adalah **kontrak teknis**, bukan saran. Setiap angka, threshold, dan urutan langkah di sini bersifat final kecuali ditandai `[DECISION NEEDED]`. Kalau agent menemukan ambiguitas yang tidak tercakup di sini, agent harus **berhenti dan bertanya**, bukan menebak dan lanjut.

Urutan implementasi WAJIB mengikuti Bagian 8 (Build Order). Jangan lompat ke fitur lanjutan sebelum fondasi di tahap sebelumnya lulus acceptance criteria-nya.

---

## 1. TUJUAN & CAKUPAN MODUL

Modul ini bertanggung jawab untuk:
1. Menghasilkan **binaural beat** (dua sine tone berbeda frekuensi, kiri/kanan) secara real-time.
2. Menghasilkan **background ambience noise** (white/pink/brown + rain/ocean) secara real-time atau looping dari file.
3. Mencampur (mixing) kedua sumber di atas menjadi satu output stereo yang aman (tidak clipping).
4. Menjalankan **sequence** multi-tahap (preset bisa berpindah dari satu frekuensi ke frekuensi lain secara halus, bukan cuma satu nada statis).
5. Berjalan di **background** (layar mati, app di-minimize) tanpa terputus, dengan kontrol dari notification & lockscreen.
6. Menyediakan kontrol dasar: play/pause, volume master, volume terpisah untuk beat vs noise, sleep timer dengan fade-out, dan cross-fade antar sesi.

**Di luar cakupan modul ini** (dikerjakan modul lain): UI mixer visual, penyimpanan preset ke database, autentikasi user, IAP/subscription.

---

## 2. TEORI DASAR YANG WAJIB DIPAHAMI AGENT SEBELUM CODING

### 2.1 Apa itu binaural beat, secara matematis

Binaural beat BUKAN suara yang benar-benar ada di file audio. Ini adalah dua sine tone murni, masing-masing dikirim ke satu telinga:

```
Telinga kiri  : sin(2π × f_left  × t)
Telinga kanan : sin(2π × f_right × t)
```

Di mana:
```
f_left  = carrier_freq − (beat_freq / 2)
f_right = carrier_freq + (beat_freq / 2)
```

Otak (batang otak, tepatnya) mempersepsikan selisih `f_right − f_left = beat_freq` sebagai "detak" ketiga. Efek ini **hanya terjadi dengan headphone/earphone stereo**. Di speaker mono, dua tone itu akan bercampur di udara dan efek binaural-nya hilang (yang tersisa cuma dua nada dekat yang berdengung — itu namanya *monaural beat*, beda mekanisme, beda area implementasi).

**Konsekuensi untuk UI**: aplikasi WAJIB menampilkan peringatan "gunakan headphone" sebelum sesi binaural dimulai, dan idealnya mendeteksi apakah output sedang ke headphone atau speaker built-in (lihat Bagian 6.4).

### 2.2 Kenapa carrier frequency harus di rentang 180–300 Hz

Sensitivitas sistem pendengaran terhadap perbedaan fase antar-telinga (interaural timing) paling tinggi di rentang frekuensi ini. Di atas ~1000 Hz efek binaural melemah signifikan. **Semua preset di taksonomi konten (Bagian 4) harus menggunakan carrier di rentang 180–300 Hz kecuali dinyatakan lain secara eksplisit oleh desainer konten.**

Default carrier jika tidak ditentukan per-preset: **200 Hz**.

### 2.3 Kenapa TIDAK BOLEH menghitung fase dari waktu absolut

Ini adalah **kesalahan paling umum** yang ditemukan di implementasi binaural beat generator yang buruk, dan sumber utama bug "klik/pop" saat frekuensi berubah.

**SALAH** (jangan lakukan ini):
```
sample[n] = sin(2π × f × (n / sampleRate))
```
Kalau `f` berubah di tengah jalan (misal preset transisi dari 18Hz ke 10Hz), rumus ini menyebabkan lompatan fase mendadak → terdengar sebagai klik/pop yang mengganggu, terutama fatal untuk app sleep karena bisa membangunkan user.

**BENAR** (phase accumulation / phase-continuous synthesis):
```
// Simpan fase kumulatif sebagai state persisten antar block audio
phase += 2π × f × (1 / sampleRate)
phase = phase mod 2π   // jaga phase tetap dalam rentang wajar, hindari overflow
sample[n] = sin(phase)
```

Fase HARUS disimpan sebagai state yang bertahan (persist) melewati batas-batas audio buffer/block. Setiap kali fungsi generate-block dipanggil untuk mengisi buffer berikutnya, ia melanjutkan dari nilai `phase` terakhir, bukan mulai dari nol atau menghitung ulang dari waktu absolut.

**Ini berlaku untuk SEMUA oscillator** dalam sistem: oscillator kiri, oscillator kanan, dan jika ada isochronic pulse envelope.

Precision: gunakan `double` (float64) untuk akumulasi fase, dan `float` (float32) untuk buffer audio yang dikirim ke hardware. Alasan: akumulasi kecil berulang jutaan kali per detik akan kehilangan presisi kalau pakai float32 dari awal.

### 2.4 Lima brainwave band (referensi, dipakai untuk label/UI, bukan logic)

| Band | Rentang Hz | Asosiasi umum |
|---|---|---|
| Delta | 0.5–4 Hz | Tidur nyenyak, deep sleep |
| Theta | 4–8 Hz | Relaksasi dalam, meditasi, kreativitas |
| Alpha | 8–12 Hz | Tenang, fokus rileks |
| Beta | 12–30 Hz | Fokus aktif, kewaspadaan |
| Gamma | 30–100 Hz | Kognisi puncak (catatan: efek binaural melemah di gamma, pertimbangkan isochronic sebagai pendamping — lihat 2.5) |

### 2.5 Isochronic tone (opsional, fase 2 — lihat Bagian 8)

Berbeda dari binaural beat, isochronic tone adalah SATU nada yang dinyalakan-matikan (amplitude modulated) secara berirama pada frekuensi target. Ini TIDAK butuh headphone (bekerja di speaker mono juga), dan efeknya lebih kuat/cepat terasa dibanding binaural pada frekuensi tinggi (gamma).

```
envelope(t) = (1 + square_wave(2π × beat_freq × t)) / 2   // 0 atau 1, on/off
sample[n] = carrier_tone[n] × envelope(t)
```

**Keputusan**: isochronic TIDAK termasuk MVP (lihat Bagian 8). Disiapkan sebagai catatan arsitektur supaya kelas oscillator didesain agar mudah diperluas nanti, tapi jangan diimplementasikan di fase 1.

---

## 3. ARSITEKTUR TEKNIS ANDROID

### 3.1 Pilihan API audio — WAJIB, bukan opsional

Gunakan **`AudioTrack`** dalam mode **`MODE_STREAM`** dengan `AudioAttributes` yang sesuai untuk media playback panjang.

**JANGAN** gunakan `MediaPlayer` untuk sintesis real-time (tidak didesain untuk itu). **JANGAN** gunakan `SoundPool` (didesain untuk sample pendek/efek, bukan streaming panjang).

```
[DECISION NEEDED — agent harus konfirmasi ke user sebelum lanjut]:
Apakah menggunakan AudioTrack native Android SDK (Java/Kotlin) sudah cukup,
atau perlu Oboe (C++ library dari Google untuk audio latency rendah)?

Rekomendasi default: MULAI dengan AudioTrack murni Kotlin (lebih sederhana,
lebih cepat untuk MVP). Upgrade ke Oboe HANYA JIKA testing menunjukkan
audio glitch/underrun yang tidak bisa diatasi dengan tuning buffer size.
Binaural beat tone murni TIDAK butuh ultra-low-latency (bukan aplikasi
musik interaktif), jadi AudioTrack biasa kemungkinan besar cukup.
```

### 3.2 Threading model

- Generation buffer audio **HARUS** berjalan di thread terpisah dari UI thread (dedicated audio thread, prioritas `THREAD_PRIORITY_AUDIO`).
- Komunikasi dari UI (misal user geser slider volume, atau ganti preset) ke audio thread **HARUS** lewat mekanisme thread-safe (misal `AtomicReference`, lock-free queue, atau `Handler` dengan message passing) — **JANGAN** langsung menulis ke variabel shared tanpa sinkronisasi, karena itu menyebabkan race condition dan audio glitch.
- Buffer size: mulai dengan `AudioTrack.getMinBufferSize()` dikali 2 sampai 4 sebagai starting point, lalu sesuaikan berdasarkan testing underrun di device target.

### 3.3 Background playback — foreground service WAJIB

Karena sesi bisa berjalan puluhan menit dengan layar mati (use case sleep!), audio generation **HARUS** berjalan di dalam **Foreground Service** dengan:
- Notification persisten yang menunjukkan preset aktif, kontrol play/pause, dan sisa waktu timer.
- `MediaSession` terintegrasi supaya kontrol lockscreen dan tombol headset (play/pause fisik) berfungsi.
- `WakeLock` PARTIAL_WAKE_LOCK selama sesi aktif (audio tetap perlu CPU jalan walau layar mati) — WAJIB dilepas begitu sesi berhenti/timer habis, jangan sampai wakelock bocor dan menguras baterai.
- Service tipe: `foregroundServiceType="mediaPlayback"` di manifest (requirement Android 10+).

### 3.4 Battery & CPU consideration

- Real-time DSP terus-menerus itu costly. **Precompute apa yang bisa di-precompute**: misal tabel noise filter coefficient (untuk pink/brown noise, lihat Bagian 5) dihitung sekali di awal, bukan tiap sample.
- Buffer/block size untuk generation: rekomendasi **100ms per block** (bukan per-sample calling) — ini konsisten dengan pendekatan yang dipakai NeuralBeat (referensi riset). Artinya pada sample rate 44100Hz, satu block = 4410 sample per channel.
- Sample rate: gunakan **44100 Hz** sebagai default (standar, kompatibel semua device, cukup untuk rentang frekuensi yang kita pakai yang jauh di bawah Nyquist).

---

## 4. FORMAT DATA PRESET — SEQUENCE-BASED, BUKAN SINGLE-VALUE

Ini adalah keputusan desain paling penting di dokumen ini. **Preset TIDAK BOLEH direpresentasikan sebagai satu angka Hz statis.** Preset direpresentasikan sebagai **array of steps**, di mana tiap step adalah salah satu dari dua tipe: `stable` (nada tetap) atau `transition` (nada berubah linear dari A ke B).

Ini memungkinkan preset seperti "Sleep": mulai di Alpha 10Hz (5 menit) → transisi turun ke Theta 6Hz (10 menit) → transisi turun ke Delta 2Hz (sisa waktu) → fade out ke silence. Bukan cuma satu angka statis sepanjang sesi.

### 4.1 Skema JSON preset (data model final)

```json
{
  "preset_id": "sleep_deep",
  "category": "SLEEP",
  "title": "Deep Sleep",
  "description": "People in deep sleep are less apt to wake in response to external stimuli than those in light sleep",
  "carrier_frequency_hz": 200,
  "total_duration_seconds": 2700,
  "steps": [
    {
      "type": "stable",
      "beat_frequency_hz": 10.0,
      "duration_seconds": 300,
      "fade_in_seconds": 10,
      "fade_out_seconds": 0
    },
    {
      "type": "transition",
      "start_beat_frequency_hz": 10.0,
      "end_beat_frequency_hz": 6.0,
      "duration_seconds": 600,
      "fade_in_seconds": 0,
      "fade_out_seconds": 0
    },
    {
      "type": "transition",
      "start_beat_frequency_hz": 6.0,
      "end_beat_frequency_hz": 2.0,
      "duration_seconds": 1800,
      "fade_in_seconds": 0,
      "fade_out_seconds": 30
    }
  ],
  "background_noise": {
    "type": "brown",
    "amplitude": 0.15
  }
}
```

### 4.2 Aturan validasi (agent harus implementasi validator ini)

1. `carrier_frequency_hz` harus di rentang 180–300 (warning jika di luar rentang ini, tapi tidak block — beberapa preset seperti Schumann Resonance 7.83Hz sengaja pakai carrier berbeda, itu keputusan konten bukan bug).
2. `beat_frequency_hz` / `start_beat_frequency_hz` / `end_beat_frequency_hz` harus di rentang **0.5–100 Hz** (di luar ini secara sains bukan lagi "brainwave entrainment" yang bermakna).
3. Untuk step `transition`, jika `start_beat_frequency_hz` tidak diisi, gunakan `end_beat_frequency_hz` dari step sebelumnya (smooth continuation) — kecuali ini step pertama dalam sequence, maka wajib diisi eksplisit.
4. `fade_in_seconds + fade_out_seconds` tidak boleh melebihi `duration_seconds` step tersebut — reject preset saat load jika melanggar ini, jangan silently clamp.
5. `steps` array tidak boleh kosong. Preset dengan array kosong adalah invalid, tolak saat parsing dengan error jelas.
6. `background_noise.amplitude` harus di rentang 0.0–1.0.

### 4.3 Mapping ke taksonomi konten yang sudah ditentukan

Gunakan tabel preset dari riset kompetitor sebagai starting content (lihat lampiran Bagian 10) — total 20 preset dasar di 5 kategori (STUDY, SPIRIT, SLEEP, BODY, BRAIN). Untuk MVP, semua 20 preset ini boleh dibuat sebagai **single `stable` step** (setara app kompetitor), TAPI struktur data harus tetap array-of-steps sejak awal (walau isinya cuma 1 step) — supaya nanti menambah preset "journey" multi-tahap tidak butuh migrasi schema.

---

## 5. GENERASI BACKGROUND NOISE

### 5.1 Jenis noise yang wajib didukung di MVP

| Tipe | Karakter | Formula/pendekatan |
|---|---|---|
| White | Energi rata di semua frekuensi | Random uniform per sample, tanpa filter |
| Pink | Energi turun 3dB/oktaf, lebih "dalam" dari white | Filter Paul Kellet (lihat 5.2) |
| Brown | Energi turun 6dB/oktaf, lebih dalam lagi | Integrasi dari white noise (random walk, dengan leak untuk stabilitas) |

Rain dan ocean (nature sounds) — **fase 2**, lihat Bagian 8. Untuk MVP cukup 3 jenis noise sintetis di atas karena bisa 100% digenerate via kode tanpa file aset.

### 5.2 Algoritma pink noise (Paul Kellet's filter) — WAJIB pakai versi ini, jangan re-derive dari nol

Ini filter IIR 6 pole yang efisien (satu kali hitung per sample, state disimpan antar block, exact seperti oscillator phase):

```
// State: b0, b1, b2, b3, b4, b5, b6 — inisialisasi semua 0 di awal
function generatePinkNoiseSample(white_sample):
    b0 = 0.99886 * b0 + white_sample * 0.0555179
    b1 = 0.99332 * b1 + white_sample * 0.0750759
    b2 = 0.96900 * b2 + white_sample * 0.1538520
    b3 = 0.86650 * b3 + white_sample * 0.3104856
    b4 = 0.55000 * b4 + white_sample * 0.5329522
    b5 = -0.7616 * b5 - white_sample * 0.0168980
    pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white_sample * 0.5362
    b6 = white_sample * 0.115926
    return pink * 0.11   // normalisasi gain, sesuaikan setelah testing loudness
```

### 5.3 Brown noise

```
// State: last_output — inisialisasi 0
function generateBrownNoiseSample(white_sample):
    last_output = (last_output + (0.02 * white_sample)) / 1.02
    return last_output * 3.5   // normalisasi gain, sesuaikan setelah testing loudness
```

### 5.4 Noise dan binaural beat: mono atau stereo?

Noise ditambahkan **sama ke kedua channel** (kiri dan kanan menerima noise identik) — TIDAK di-binaural-kan. Hanya binaural tone yang berbeda antar channel. Ini konsisten dengan implementasi ksylvan/binaural-generator yang jadi referensi.

---

## 6. MIXING & SAFETY (WAJIB — sering diabaikan, sumber bug paling merusak)

### 6.1 Urutan pipeline mixing (ikuti urutan ini persis)

```
1. Generate binaural tone block (kiri, kanan) — hasil dari phase-accumulated oscillator
2. Generate noise block (mono, akan dipakai untuk kedua channel)
3. Terapkan ramp/transition interpolation JIKA step saat ini bertipe "transition"
4. Terapkan fade-in/fade-out envelope JIKA block ini berada di rentang waktu fade
5. Mix: output_left  = (tone_left  × (1 − noise_amplitude)) + (noise × noise_amplitude)
        output_right = (tone_right × (1 − noise_amplitude)) + (noise × noise_amplitude)
6. Terapkan master volume (0.0–1.0, dikontrol slider user)
7. Soft-limiter (lihat 6.2) — LANGKAH TERAKHIR sebelum dikirim ke hardware, tanpa kecuali
```

### 6.2 Soft limiter — WAJIB, tanpa ini app bisa menghasilkan clipping/distorsi berbahaya

```
function softLimit(sample):
    return tanh(sample)   // tanh secara alami membatasi output ke rentang (-1, 1) dengan transisi halus, tidak seperti hard clipping yang bikin distorsi kasar
```

Terapkan `softLimit` ke setiap sample output final, KEDUA channel, SETELAH semua mixing dan volume selesai. Tidak ada jalur di mana sample dikirim ke `AudioTrack.write()` tanpa melewati fungsi ini.

### 6.3 Verifikasi otomatis (unit test yang WAJIB ada, bukan opsional)

Agent harus menulis test otomatis yang memverifikasi, untuk setiap kombinasi preset + noise:
1. **No sample melebihi amplitude ±1.0** (clipping check) — scan seluruh buffer yang di-generate.
2. **DC offset mendekati 0** — rata-rata semua sample dalam window 1 detik harus mendekati 0 (mencegah kerusakan speaker/driver headphone jangka panjang dan hilangnya dynamic range).
3. **Verifikasi frekuensi via FFT** — untuk binaural stable step, ambil 1 detik buffer, jalankan FFT, pastikan peak frequency di channel kiri = `carrier − beat/2` dan channel kanan = `carrier + beat/2`, dengan toleransi ±0.5 Hz.
4. **Tidak ada klik/pop di titik transisi** — deteksi lonjakan turunan sample-to-sample (delta) yang tidak wajar di titik sambungan antar step; ini akan menangkap bug phase-accumulation yang salah.

### 6.4 Deteksi output device (headphone vs speaker)

Gunakan `AudioManager` untuk mendeteksi apakah output audio sedang menuju wired headset, Bluetooth headset, atau speaker built-in (`AudioDeviceInfo` API, `getDevices(GET_DEVICES_OUTPUTS)`).

- Jika terdeteksi speaker built-in dan preset yang dipilih adalah tipe binaural murni (bukan isochronic), tampilkan warning non-blocking ("Untuk hasil optimal, gunakan headphone") — **JANGAN block playback**, cukup informasikan, karena user mungkin sengaja mau dengar preview.
- Jika headphone dicabut di tengah sesi, **pause otomatis** (bukan lanjut ke speaker tanpa peringatan) dan tampilkan notifikasi.

---

## 7. FITUR KONTROL SESI (SPEC PERILAKU, BUKAN OPSIONAL)

### 7.1 Sleep timer dengan fade-out

- User set durasi timer (misal 30 menit) yang independen dari `total_duration_seconds` preset.
- 30 detik terakhir sebelum timer habis: terapkan **linear fade-out** dari volume saat ini ke 0.
- Setelah fade-out selesai: hentikan `AudioTrack`, lepas `WakeLock`, hentikan foreground service (atau turunkan ke background notification biasa).
- **Jangan** memotong audio secara tiba-tiba di detik terakhir (hard stop tanpa fade) — ini pengalaman buruk khususnya untuk app sleep.

### 7.2 Transisi antar sesi (kalau user ganti preset saat sedang play)

- Terapkan **cross-fade** 1–2 detik: preset lama fade-out sambil preset baru fade-in secara bersamaan, bukan cut langsung.
- State phase accumulator untuk oscillator preset baru **HARUS di-reset ke 0** saat mulai (tidak melanjutkan phase dari preset lama, karena frekuensinya beda konteks).

### 7.3 Pause/resume

- Saat pause: hentikan pengisian buffer baru ke `AudioTrack`, TAPI simpan seluruh state (phase accumulator, posisi dalam sequence steps, waktu tersisa) supaya resume melanjutkan persis dari titik yang sama — bukan restart dari awal step.
- Terapkan fade-out cepat (200–300ms) saat pause dan fade-in cepat saat resume untuk menghindari klik akibat stop/start `AudioTrack` mendadak.

---

## 8. BUILD ORDER — IKUTI URUTAN INI, JANGAN LOMPAT

Ini urutan implementasi wajib. Setiap tahap punya **acceptance criteria** yang harus lulus sebelum lanjut ke tahap berikutnya. Kalau tahap N gagal acceptance criteria, JANGAN lanjut ke tahap N+1 — perbaiki dulu.

### TAHAP 1 — Single stable tone, tanpa UI
**Scope:** Satu oscillator kiri + kanan, frekuensi stable (misal carrier 200Hz, beat 10Hz), diputar 10 detik via `AudioTrack`, tanpa noise, tanpa fade, tanpa preset system.
**Acceptance criteria:**
- Audio terdengar tanpa klik/pop/glitch dari awal sampai akhir.
- Unit test FFT (Bagian 6.3 poin 3) lulus untuk kasus ini.
- Tidak ada clipping (Bagian 6.3 poin 1).

### TAHAP 2 — Transition step + phase continuity
**Scope:** Tambahkan step type `transition`, uji dengan preset 2-step (stable → transition ke frekuensi lain).
**Acceptance criteria:**
- Tidak ada klik/pop di titik sambungan antar step (Bagian 6.3 poin 4).
- Transisi frekuensi terdengar halus secara subjektif DAN terverifikasi lewat spectrogram (frequency berubah linear terhadap waktu, bukan meloncat).

### TAHAP 3 — Background noise + mixing
**Scope:** Implementasi white/pink/brown noise generator (Bagian 5), mixing pipeline lengkap (Bagian 6.1), soft limiter (Bagian 6.2).
**Acceptance criteria:**
- Ketiga jenis noise lulus DC offset check.
- Kombinasi tone + noise di volume maksimum tidak clipping (soft limiter bekerja).
- Noise terdengar sama persis di kedua channel (tidak ter-binaural-kan secara tidak sengaja).

### TAHAP 4 — Preset system lengkap + validator
**Scope:** Parser JSON preset (Bagian 4), validator (Bagian 4.2), loading 20 preset dari taksonomi konten (Bagian 10).
**Acceptance criteria:**
- Semua 20 preset ter-load tanpa error validasi.
- Preset dengan data invalid (sengaja dibuat untuk testing) ditolak dengan pesan error yang jelas, bukan crash silent.

### TAHAP 5 — Background playback & foreground service
**Scope:** Foreground service, MediaSession, notification controls, WakeLock management (Bagian 3.3).
**Acceptance criteria:**
- Audio tetap jalan mulus setelah layar dimatikan 10+ menit.
- WakeLock terlepas dengan benar saat sesi berhenti (verifikasi via `adb shell dumpsys power`, tidak ada wakelock bocor).
- Kontrol dari notification dan lockscreen berfungsi (play/pause minimal).

### TAHAP 6 — Kontrol sesi: sleep timer, cross-fade, pause/resume
**Scope:** Bagian 7 lengkap.
**Acceptance criteria:**
- Sleep timer fade-out berjalan mulus, tidak hard-cut.
- Pause lalu resume melanjutkan dari posisi & fase yang sama persis, tidak restart.
- Ganti preset saat playing menghasilkan cross-fade, bukan cut.

### TAHAP 7 — Device detection & UX safety
**Scope:** Bagian 6.4 (deteksi headphone/speaker, auto-pause saat headphone dicabut).
**Acceptance criteria:**
- Mencabut headphone di tengah sesi memicu pause otomatis dan notifikasi, bukan lanjut ke speaker diam-diam.

### FASE 2 (di luar MVP, JANGAN dikerjakan sebelum Tahap 1–7 lulus semua)
- Isochronic tone generator (Bagian 2.5).
- Rain/ocean nature sound (baik sebagai file loop atau sintesis algoritmik).
- Custom preset builder untuk user (mixer UI, ini juga bergantung pada modul UI terpisah).
- Monaural beat mode (untuk yang mau pakai speaker tanpa headphone).

---

## 9. STRUKTUR FILE/MODUL YANG DISARANKAN (Kotlin)

```
audio-engine/
├── core/
│   ├── PhaseAccumulator.kt        // oscillator dasar, phase-continuous
│   ├── BinauralToneGenerator.kt   // pasangan oscillator kiri/kanan dari carrier+beat
│   ├── NoiseGenerator.kt          // interface + white/pink/brown implementation
│   ├── SoftLimiter.kt             // fungsi tanh limiter
│   └── MixingPipeline.kt          // orkestrasi urutan Bagian 6.1
├── preset/
│   ├── PresetModel.kt             // data class sesuai skema Bagian 4.1
│   ├── PresetValidator.kt         // aturan Bagian 4.2
│   ├── PresetLoader.kt            // parsing JSON
│   └── SequenceScheduler.kt       // tracking posisi dalam array steps seiring waktu berjalan
├── playback/
│   ├── AudioEngineService.kt      // Foreground Service
│   ├── AudioTrackManager.kt       // wrapper AudioTrack, buffer writing loop
│   ├── SessionController.kt       // play/pause/resume/timer/crossfade (Bagian 7)
│   └── DeviceOutputMonitor.kt     // deteksi headphone (Bagian 6.4)
└── test/
    ├── ClippingTest.kt
    ├── DcOffsetTest.kt
    ├── FftFrequencyTest.kt
    └── PhaseContinuityTest.kt
```

---

## 10. LAMPIRAN — 20 PRESET AWAL (DATA KONTEN SIAP PAKAI)

Sumber: taksonomi dari riset kompetitor, sudah divalidasi format Bagian 4. Semua sebagai single `stable` step untuk MVP.

**STUDY**
| Preset | Beat Hz | Deskripsi |
|---|---|---|
| Memory | 4.0 | Increasing of memory retention functionalities |
| Focus | 14.0 | Associated with problem solving and information tasks |
| Study Aid | 12.0 | Good for absorbing information passively |

**SPIRIT**
| Preset | Beat Hz | Deskripsi |
|---|---|---|
| Trance | 5.5 | Associated with Inner Guidance, intuition, and heat generation |
| Astral Travel | 7.0 | Let your body travel across the dreams |
| Chanting | 4.5 | Mantra used for deep meditation throughout the ages |
| Solfeggio | 7.83 | Earth Resonance / Schumann Resonance |
| Third Eye | 13.0 | Invisible eye that lets you perceive things beyond ordinary sight |

**SLEEP**
| Preset | Beat Hz | Deskripsi |
|---|---|---|
| Sleep | 2.0 | Natural periodic suspension of consciousness |
| Deep Sleep | 3.9 | Less apt to wake from external stimuli |
| Lucid Dream | 1.5 | Aware that one is dreaming |

**BODY**
| Preset | Beat Hz | Deskripsi |
|---|---|---|
| Universal Healing | 1.5 | Abraham's Universal Healing Rate, associated with sleep |
| Overcome Addiction | 8.0 | Useful for addictive personalities |
| Fatigue Energizer | 20.0 | Frekuensi untuk mengatasi kelelahan |
| Inflammation Problems | 20.0 | Membantu masalah inflamasi umum |

**BRAIN**
| Preset | Beat Hz | Deskripsi |
|---|---|---|
| Intelligence | 15.4 | Associated with cortex, efek peningkatan intelegensi |
| Creativity | 10.6 | Baik untuk kontemplasi ide/solusi kreatif |
| Relaxation | 6.0 | Relaksasi mental dan fisik |
| Euphoria | 20.0 | Perasaan bahagia, excitement, well-being |
| Intuition | 5.5 | Membangkitkan intuisi |

**Catatan konten**: carrier frequency untuk semua preset di atas default **200 Hz** kecuali Solfeggio (7.83Hz) yang secara tradisi kadang dipasangkan dengan carrier berbeda — `[DECISION NEEDED]` konfirmasi ke tim konten sebelum finalize, tapi untuk MVP boleh pakai default 200Hz juga demi konsistensi engine.

---

## 11. DISCLAIMER YANG WAJIB ADA DI APP (bukan opsional, isu compliance)

Berdasarkan riset kompetitor (NeuralBeat mencantumkan ini secara eksplisit), app WAJIB menampilkan disclaimer berikut minimal di halaman "About" dan idealnya saat onboarding pertama kali:

> Informasi tentang frekuensi brainwave dan kegunaannya bersifat edukasional. Efek entrainment brainwave bervariasi antar individu dan tidak dimaksudkan untuk mendiagnosis, mengobati, menyembuhkan, atau mencegah penyakit apapun. Konsultasikan dengan tenaga medis sebelum menggunakan alat entrainment audio, khususnya jika Anda memiliki kondisi medis tertentu (termasuk epilepsi) atau sedang hamil. Jangan gunakan saat mengemudi atau mengoperasikan mesin.

**JANGAN** membuat klaim medis di copy marketing atau deskripsi preset manapun (hindari kata seperti "menyembuhkan", "terapi", "mengobati"). Gunakan bahasa deskriptif-edukasional seperti yang sudah ada di taksonomi Bagian 10 (misal "associated with", "good for", bukan "cures" atau "treats").

---

## 12. RINGKASAN KEPUTUSAN YANG SUDAH FINAL VS YANG MASIH TERBUKA

**Sudah final (jangan didiskusikan ulang, langsung eksekusi):**
- Real-time synthesis untuk binaural tone (bukan pre-rendered file).
- Phase accumulation, bukan absolute-time sine calculation.
- AudioTrack MODE_STREAM sebagai starting point.
- Format preset: array-of-steps, bukan single value.
- Soft limiter (tanh) di akhir pipeline, tanpa kecuali.
- Noise sintetis (white/pink/brown) via kode untuk MVP.
- Urutan build order Bagian 8.

**Masih terbuka `[DECISION NEEDED]`, agent harus tanya sebelum eksekusi:**
- AudioTrack murni vs Oboe (Bagian 3.1) — default AudioTrack, upgrade jika perlu.
- Carrier frequency untuk preset Solfeggio (Bagian 10, catatan konten).
- Rain/ocean: sintesis algoritmik vs file aset (baru relevan di Fase 2).
