# PRD — Aplikasi Binaural Beats / Brainwave / Sleep & Focus
## Android First, iOS Next

**Versi dokumen:** 1.0
**Status:** Siap dieksekusi oleh AI coding agent
**Dokumen terkait:** `audio-engine-spec.md` (lampiran teknis wajib dibaca sebelum implementasi modul audio)

---

## 0. CARA MEMBACA DOKUMEN INI

Dokumen ini adalah sumber kebenaran tunggal (single source of truth) untuk *apa* yang dibangun dan *kenapa*. Untuk *bagaimana* modul audio dibangun secara teknis, rujuk `audio-engine-spec.md` — jangan duplikasi keputusan teknis di sana ke sini.

Prioritas fitur memakai notasi **MoSCoW**: Must have, Should have, Could have, Won't have (untuk versi ini). Agent WAJIB membangun sesuai urutan prioritas ini, bukan urutan yang menurut agent "lebih mudah dulu".

Kalau ada bagian yang bertentangan antar dokumen, PRD ini yang menang untuk soal *scope/fitur*, dan `audio-engine-spec.md` yang menang untuk soal *implementasi teknis audio*.

---

## 1. RINGKASAN PRODUK

Aplikasi mobile Android (iOS menyusul) yang menyediakan binaural beat, brainwave entrainment, dan soundscape untuk kebutuhan tidur, fokus, meditasi, dan relaksasi. Berbeda dari kompetitor kelas menengah (single-tone statis, ads generic), produk ini dibangun dengan real-time audio synthesis yang presisi, kemampuan sequence multi-tahap (bukan nada statis), dan arsitektur yang siap dikembangkan menuju fitur mixer/layering ala mynoise.net di rilis berikutnya.

---

## 2. LATAR BELAKANG & MASALAH

### 2.1 Masalah yang ingin diselesaikan
Orang yang kesulitan tidur, fokus kerja/belajar, atau ingin bermeditasi sering mencari bantuan audio, tapi opsi yang tersedia di Play Store saat ini (contoh kompetitor: app 500rb+ unduhan yang dianalisis di riset awal) punya keterbatasan:
- Preset nada statis tunggal sepanjang sesi (tidak ada progresi/journey).
- Ads generic yang tidak relevan dengan konteks wellness (mengganggu pengalaman menjelang tidur).
- Tidak ada indikasi jelas soal engine audio-nya presisi secara ilmiah atau tidak.
- Tidak ada kemampuan kombinasi/layering suara (mixer).

### 2.2 Peluang
Riset kompetitor open-source (BrainWave, NeuralBeat, binaural-generator, dkk) menunjukkan standar teknis yang jauh lebih matang tersedia gratis sebagai referensi — phase-continuous synthesis, sequence-based preset, noise generation presisi — tapi belum ada yang mengemasnya dalam app mobile konsumer yang rapi dan mudah dipakai orang awam.

---

## 3. TARGET PENGGUNA

### 3.1 Persona utama

| Persona | Kebutuhan | Konteks pakai |
|---|---|---|
| **Insomnia/sleep-seeker** | Tidur lebih cepat/nyenyak | Malam hari, layar mati, headphone/earphone, sesi panjang (30-90 menit) |
| **Pekerja/pelajar fokus** | Konsentrasi saat kerja/belajar | Siang/sore, sesi menengah (25-60 menit), sering multitasking dengan app lain |
| **Praktisi meditasi** | Relaksasi, kondisi trance/spiritual | Kapan saja, sesi terjadwal, kadang sambil breathing exercise |

### 3.2 Yang BUKAN target utama (di luar fokus persona utama, tapi tidak dilarang pakai)
- Pengguna yang mencari terapi medis formal (app ini bukan alat medis, lihat disclaimer di `audio-engine-spec.md` Bagian 11).
- Musisi/audio engineer yang butuh kontrol DSP granular (itu use case NeuralBeat/BrainWave versi desktop, bukan app mobile konsumer).

---

## 4. GOALS & SUCCESS METRICS

### 4.1 Goals produk (versi 1.0 / MVP)
- User bisa menemukan preset yang sesuai kebutuhan dalam waktu kurang dari 30 detik dari buka app.
- Audio berjalan mulus tanpa glitch/klik selama sesi panjang (30+ menit), termasuk saat layar mati.
- App terasa "lebih niat/presisi" dibanding kompetitor — tercermin dari kualitas transisi antar frekuensi dan tidak adanya ads yang mengganggu momen sensitif (menjelang tidur).

### 4.2 Success metrics (indikatif — sesuaikan dengan tooling analytics yang dipakai nanti)
- **Retention D7** lebih tinggi dari baseline app sejenis (acuan: app kompetitor yang dianalisis).
- **Rata-rata durasi sesi selesai** (bukan di-skip di tengah) sebagai indikator preset benar-benar membantu.
- **Crash-free session rate** > 99.5% khusus untuk modul audio (karena crash saat sesi tidur = pengalaman sangat buruk).
- **Konversi ke premium** (jika model freemium dipilih, lihat Bagian 9).

---

## 5. FEATURE LIST (MoSCoW)

### 5.1 MUST HAVE (MVP — wajib ada di rilis pertama)

| # | Fitur | Catatan |
|---|---|---|
| M1 | 5 kategori konten: STUDY, SPIRIT, SLEEP, BODY, BRAIN dengan 20 preset dasar | Data lengkap ada di `audio-engine-spec.md` Bagian 10 |
| M2 | Real-time binaural beat generation (phase-continuous) | Spec teknis lengkap di `audio-engine-spec.md` |
| M3 | Background noise (white/pink/brown) sebagai layer opsional per preset | — |
| M4 | Player screen: play/pause, volume, sleep timer dengan fade-out | — |
| M5 | Background playback (layar mati, notification control, lockscreen) | Foreground service wajib, lihat spec teknis |
| M6 | Home screen menampilkan 5 kategori | Sesuai referensi kompetitor, lihat Bagian 6 (Screen Overview) |
| M7 | Halaman detail kategori menampilkan daftar preset dengan deskripsi | — |
| M8 | Deteksi headphone vs speaker, warning jika pakai speaker built-in | — |
| M9 | Disclaimer kesehatan/edukasional saat onboarding dan di halaman About | Teks final di `audio-engine-spec.md` Bagian 11 |
| M10 | Dark mode | Konteks pakai mayoritas malam hari/menjelang tidur |
| M11 | Cross-fade saat ganti preset di tengah sesi | — |
| M12 | Pause/resume mempertahankan posisi & fase persis | — |

### 5.2 SHOULD HAVE (rilis 1.1 — segera setelah MVP stabil)

| # | Fitur | Catatan |
|---|---|---|
| S1 | Library — simpan preset favorit/kustom user | Bottom nav sudah dialokasikan sejak MVP (lihat Bagian 6), tapi fungsi create-custom-preset baru di sini |
| S2 | Sessions — riwayat & penjadwalan sesi (mis. alarm sesi otomatis tiap malam) | — |
| S3 | Breathing exercise terpisah dari player (guided visual) | Kompetitor punya tombol ini tapi tidak jelas isinya; ini peluang diferensiasi |
| S4 | Statistik sederhana (streak, total menit sesi) | Mendorong retention |
| S5 | Preset journey multi-tahap tambahan (bukan cuma single stable tone) | Engine sudah mendukung sequence sejak awal (lihat `audio-engine-spec.md` Bagian 4), tinggal tambah konten |

### 5.3 COULD HAVE (rilis 2.0 — diferensiator besar, setelah traksi awal terbukti)

| # | Fitur | Catatan |
|---|---|---|
| C1 | Mixer/layering ala mynoise.net — kombinasikan beberapa background sound + binaural tone dengan slider volume masing-masing | Ini diferensiator utama vs kompetitor kelas menengah |
| C2 | Custom preset builder — user pilih carrier + beat freq sendiri | Engine sudah siap secara arsitektur |
| C3 | Isochronic tone mode | Sudah dicatat sebagai extension point di audio engine spec |
| C4 | Rain/ocean nature sound | Fase 2 di audio engine spec |
| C5 | Export sesi ke file audio (untuk didengar di luar app, offline penuh) | — |
| C6 | Widget home screen Android | — |

### 5.4 WON'T HAVE (secara eksplisit di luar scope versi ini — jangan dikerjakan tanpa diskusi ulang)

- Integrasi wearable/smartwatch untuk deteksi tidur otomatis.
- Social feature (share sesi ke teman, leaderboard, dsb).
- AI-generated soundscape dinamis berbasis mood detection.
- Versi iOS (masuk roadmap terpisah, lihat Bagian 11, tapi bukan bagian dari sprint MVP Android).
- Klaim atau fitur yang mengarah ke diagnosis/pengobatan medis (lihat disclaimer, ini batasan permanen bukan sekadar "belum waktunya").

---

## 6. SCREEN OVERVIEW (ringkas — detail penuh di dokumen UX Flow terpisah)

Struktur navigasi mengikuti pola yang sudah terbukti dari analisis kompetitor, dengan penyesuaian:

```
Bottom Navigation (3 tab utama):
├── Home       → 5 kategori (STUDY/SPIRIT/SLEEP/BODY/BRAIN) sebagai card list
├── Library    → preset favorit/kustom user (kosong di awal, MVP: tampilkan empty state + guide ke Home)
└── Sessions   → riwayat & jadwal sesi (kosong di awal, MVP: tampilkan empty state)

Alur utama:
Home → tap kategori → Category Detail (list preset dengan Hz + deskripsi)
     → tap preset → Player Screen (kontrol utama sesi)

Side menu (hamburger icon):
├── Night mode toggle
├── Share it
├── Rate this application
├── Contact
└── About (termasuk disclaimer wajib)
```

**Catatan MVP**: tab Library dan Sessions boleh ditampilkan di navigasi sejak awal (konsisten dengan struktur 3-tab), tapi isinya baru fungsional penuh di rilis 1.1 (S1, S2). Untuk MVP, cukup tampilkan empty state yang mengarahkan user kembali ke Home.

Dokumen UX Flow & Screen Spec terpisah akan merinci: state kosong/loading/error tiap screen, komponen visual, dan interaksi detail Player Screen (termasuk breathing exercise placeholder untuk S3).

---

## 7. USER STORIES (MVP)

1. **Sebagai** pengguna yang susah tidur, **saya ingin** membuka app dan langsung menemukan kategori SLEEP dari Home, **supaya** saya tidak perlu mencari-cari saat sudah mengantuk.
2. **Sebagai** pengguna, **saya ingin** melihat deskripsi singkat tiap preset (bukan cuma angka Hz), **supaya** saya paham apa manfaat yang diklaim tanpa perlu riset sendiri.
3. **Sebagai** pengguna yang memutar sesi tidur, **saya ingin** audio tetap berjalan saat layar mati dan HP di-lock, **supaya** saya bisa taruh HP dan langsung tidur.
4. **Sebagai** pengguna, **saya ingin** mengatur sleep timer supaya audio berhenti otomatis dengan fade-out halus, **supaya** saya tidak terbangun karena audio berhenti mendadak atau berjalan semalaman menguras baterai.
5. **Sebagai** pengguna yang memakai speaker (bukan headphone) secara tidak sadar, **saya ingin** diingatkan bahwa binaural beat butuh headphone, **supaya** saya tidak kecewa karena efeknya tidak terasa.
6. **Sebagai** pengguna, **saya ingin** mengganti preset di tengah sesi tanpa suara terputus kasar, **supaya** transisinya tidak mengagetkan.
7. **Sebagai** pengguna baru, **saya ingin** melihat penjelasan singkat bahwa efek ini bersifat edukasional dan bukan pengobatan medis, **supaya** saya punya ekspektasi yang wajar.

---

## 8. NON-FUNCTIONAL REQUIREMENTS

| Kategori | Requirement |
|---|---|
| **Performance** | Tidak ada audio underrun/glitch pada device kelas menengah (target: Android device dengan RAM 3GB+, API level 24/Android 7.0 ke atas sebagai minimum) |
| **Battery** | Sesi 60 menit dengan layar mati tidak boleh menghabiskan lebih dari perkiraan wajar untuk streaming audio biasa (acuan pembanding: aplikasi pemutar musik standar) — WakeLock dikelola ketat sesuai `audio-engine-spec.md` Bagian 3.3 |
| **Offline-first** | Seluruh fitur MUST HAVE (M1-M12) berfungsi 100% tanpa koneksi internet, karena audio digenerate lokal, bukan streaming dari server |
| **Aksesibilitas** | Kontras warna memadai di dark mode, ukuran teks dapat diperbesar mengikuti sistem, tombol kontrol utama (play/pause) berukuran cukup besar untuk dioperasikan dalam kondisi kamar gelap/mata setengah tertutup |
| **Privasi** | Tidak ada data sesi/preferensi yang dikirim ke server pihak ketiga tanpa consent eksplisit; jika ada analytics, harus anonymized dan disebutkan di privacy policy |
| **Stabilitas** | Crash-free session rate > 99.5% (lihat Bagian 4.2) |

---

## 9. MONETISASI (RINGKAS — dokumen bisnis terpisah untuk detail penuh)

**Model default untuk MVP:** Freemium dengan batasan wajar, TANPA banner ads generic yang mengganggu (ini pembeda eksplisit dari kompetitor yang dianalisis, yang pakai ads Shopee/trading app — tidak relevan dengan konteks wellness).

- **Gratis:** akses ke seluruh 20 preset dasar (M1), sleep timer, background playback — fitur inti tetap gratis supaya app tetap kompetitif secara adopsi awal.
- **Premium (opsional, `[DECISION NEEDED]` — konfirmasi model bisnis final sebelum implementasi payment):** akses ke fitur S1-S5 dan C1-C6 (Library kustom tak terbatas, mixer, journey preset lanjutan, export audio), atau alternatifnya satu kali beli (lifetime unlock) tanpa subscription berulang.

Detail struktur harga, provider payment (Google Play Billing), dan copy paywall didokumenkan terpisah di **Monetization Doc** — belum dibuat, disarankan sebagai dokumen lanjutan.

---

## 10. ASUMSI & RISIKO

| Asumsi/Risiko | Mitigasi |
|---|---|
| Real-time synthesis di device Android low-end mungkin tidak semulus device flagship | Testing wajib di device kelas menengah-bawah sebelum rilis, bukan cuma emulator/flagship |
| User awam mungkin tidak paham kenapa harus pakai headphone | Onboarding + warning eksplisit (M8), bukan diasumsikan user akan baca deskripsi preset |
| Klaim manfaat kesehatan/kognitif bisa menimbulkan masalah kepatuhan Play Store | Disclaimer wajib (M9) + bahasa deskriptif bukan klaim medis, konsisten di seluruh copy app termasuk marketing |
| Precision audio engine (phase-continuity dkk) menambah kompleksitas dev dibanding kompetitor yang mungkin cuma pakai file statis | Ini trade-off yang disengaja demi kualitas & diferensiasi jangka panjang — lihat `audio-engine-spec.md` Bagian 1 |

---

## 11. ROADMAP RINGKAS

```
Fase 1 — MVP Android (fitur MUST HAVE, Bagian 5.1)
   ↓
Fase 2 — Rilis 1.1 Android (SHOULD HAVE: Library, Sessions, breathing exercise, statistik)
   ↓
Fase 3 — Monetisasi penuh diaktifkan (setelah traksi/retention awal terukur)
   ↓
Fase 4 — Rilis 2.0 Android (COULD HAVE: mixer/layering, custom preset builder — diferensiator utama)
   ↓
Fase 5 — Port iOS (setelah Android stabil dan validated, arsitektur audio engine idealnya sudah modular untuk memudahkan port logic non-UI)
```

Milestone detail per fase (tanggal, sprint breakdown) didokumentasikan terpisah setelah tim/AI agent mengonfirmasi kapasitas eksekusi.

---

## 12. DOKUMEN TERKAIT (STATUS)

| Dokumen | Status |
|---|---|
| `audio-engine-spec.md` | ✅ Selesai — lampiran teknis wajib untuk modul audio |
| `prd-main.md` (dokumen ini) | ✅ Selesai |
| UX Flow & Screen Spec | ⏳ Belum dibuat — disarankan berikutnya |
| Design System | ⏳ Belum dibuat |
| Monetization Doc | ⏳ Belum dibuat (ringkasan awal ada di Bagian 9) |
| Compliance Doc (Play Store policy) | ⏳ Belum dibuat (disclaimer dasar ada di `audio-engine-spec.md` Bagian 11) |
