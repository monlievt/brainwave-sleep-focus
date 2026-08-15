# UX FLOW & SCREEN SPEC
## Aplikasi Binaural Beats / Brainwave / Sleep & Focus — Android First

**Versi dokumen:** 1.0
**Status:** Siap dieksekusi oleh AI coding agent
**Dokumen terkait:** `prd-main.md` (sumber kebenaran fitur & prioritas), `audio-engine-spec.md` (kontrak teknis modul audio)

---

## 0. CARA MEMBACA DOKUMEN INI

Dokumen ini menurunkan Bagian 6 & 7 dari `prd-main.md` menjadi spesifikasi screen-by-screen yang bisa langsung dieksekusi. Setiap screen didefinisikan dengan format yang sama:

- **Tujuan** — kenapa screen ini ada.
- **Trigger masuk** — dari mana user bisa sampai ke screen ini.
- **Komponen** — elemen UI yang wajib ada, urut dari atas ke bawah.
- **State** — kondisi berbeda yang WAJIB ditangani (default/loading/empty/error), bukan opsional.
- **Interaksi** — apa yang terjadi saat user melakukan aksi tertentu.
- **Exit points** — ke mana user bisa pergi dari screen ini.

**Aturan wajib untuk agent**: jangan implementasi satu screen pun tanpa state kosong/loading/error yang didefinisikan di sini. Screenshot kompetitor yang jadi referensi awal HANYA menunjukkan happy path (data sudah ada, koneksi lancar) — kalau agent hanya meniru screenshot, aplikasi akan crash atau terlihat rusak begitu data kosong/loading pertama kali.

---

## 1. INFORMATION ARCHITECTURE (PETA LENGKAP)

```
[Splash Screen]
      ↓
[Onboarding] (hanya first launch)
      ↓
┌─────────────────────────────────────────┐
│         BOTTOM NAVIGATION (3 tab)         │
├─────────────┬─────────────┬───────────────┤
│    Home     │   Library   │   Sessions    │
└─────────────┴─────────────┴───────────────┘
      ↓              ↓              ↓
[Category Detail] [Library Detail] [Session Detail]
      ↓
[Player Screen] ←──────────────────┘
      ↓
[Breathing Exercise] (modal/sub-screen dari Player)

[Side Menu / Hamburger] — dapat diakses dari Home, Library, Sessions
      ↓
├── Settings (Night mode, dll)
├── Share it
├── Rate this application
├── Our applications
├── Contact
└── About (termasuk Disclaimer)
```

---

## 2. SPLASH SCREEN

**Tujuan:** Loading awal app (inisialisasi audio engine, load preset data lokal), branding singkat.

**Trigger masuk:** App dibuka (cold start).

**Komponen:**
- Logo/nama app di tengah.
- Tidak ada spinner yang terlihat kalau load < 500ms (splash native Android sudah cukup); jika load > 500ms, tampilkan indikator progress minimal.

**State:**
| State | Perilaku |
|---|---|
| Normal | Load preset JSON lokal (Bagian 4 `audio-engine-spec.md`), validasi, lanjut ke Onboarding (first launch) atau Home (sudah pernah buka) |
| Error load data | Preset data corrupt/gagal parse → tampilkan pesan singkat "Gagal memuat data, coba lagi" dengan tombol retry. JANGAN biarkan app lanjut ke Home dengan data kosong tanpa pemberitahuan. |

**Exit points:** Onboarding (first launch) atau Home (returning user).

---

## 3. ONBOARDING (FIRST LAUNCH ONLY)

**Tujuan:** Perkenalan singkat + menampilkan disclaimer wajib (PRD M9) sebelum user mulai pakai app.

**Trigger masuk:** Hanya saat `is_first_launch == true` (disimpan di local preference, dicek sekali lalu tidak pernah ditampilkan lagi kecuali user reinstall).

**Komponen (3 slide, swipeable atau tombol Next):**

*Slide 1 — Perkenalan*
- Ilustrasi singkat + judul (misal "Temukan ketenangan lewat suara")
- Tombol "Lanjut"

*Slide 2 — Cara pakai*
- Penjelasan singkat: pakai headphone/earphone untuk hasil terbaik, pilih kategori sesuai kebutuhan.
- Tombol "Lanjut"

*Slide 3 — Disclaimer (WAJIB, tidak boleh di-skip tanpa dibaca)*
- Teks disclaimer lengkap dari `audio-engine-spec.md` Bagian 11.
- Checkbox "Saya mengerti" — tombol "Mulai" **disabled** sampai checkbox dicentang.
- Tombol "Mulai" → set `is_first_launch = false`, lanjut ke Home.

**State:** Tidak ada state loading/error kompleks di sini — ini murni UI statis. Satu-satunya validasi: tombol "Mulai" di slide 3 tidak aktif sebelum checkbox dicentang.

**Interaksi:**
- Swipe kiri/kanan atau tombol Next/Back antar slide.
- Tombol "Lewati" (skip) HANYA muncul di slide 1-2, TIDAK ADA di slide 3 (disclaimer tidak boleh di-skip).

**Exit points:** Home (setelah slide 3 selesai).

---

## 4. HOME SCREEN

**Tujuan:** Entry point utama, menampilkan 5 kategori konten agar user cepat menemukan preset sesuai kebutuhan (PRD User Story #1).

**Trigger masuk:** Bottom nav tab "Home" (default tab aktif saat app dibuka), atau kembali dari screen lain.

**Komponen (urut dari atas):**
1. Header: hamburger icon (kiri) untuk buka Side Menu, judul "Home", icon statistik/grafik (kanan, opsional — placeholder untuk fitur S4 di rilis 1.1, boleh disembunyikan di MVP jika belum ada fungsinya).
2. List 5 card kategori, tiap card berisi:
   - Warna aksen kiri (border/strip warna) sesuai kategori — lihat mapping warna di Bagian 9.
   - Icon kategori.
   - Judul kategori (STUDY/SPIRIT/SLEEP/BODY/BRAIN).
   - Subjudul: daftar singkat nama preset dipisah "|" (misal "Memory | Focus | Aid").
3. Bottom navigation (Home/Library/Sessions).

**State:**
| State | Perilaku |
|---|---|
| Normal | 5 card kategori tampil sesuai data preset yang sudah divalidasi saat splash |
| Loading | Jika data belum siap (jarang terjadi karena load sudah selesai di splash) → tampilkan skeleton loader berbentuk card, bukan layar putih kosong |
| Error | Jika somehow data kategori gagal ditampilkan → tampilkan pesan error + tombol retry, JANGAN tampilkan Home kosong tanpa penjelasan |

**Interaksi:**
- Tap card kategori → masuk ke Category Detail Screen (Bagian 5), membawa parameter `category_id`.
- Tap hamburger icon → buka Side Menu (Bagian 10).

**Exit points:** Category Detail, Side Menu, atau pindah tab bottom nav (Library/Sessions).

---

## 5. CATEGORY DETAIL SCREEN

**Tujuan:** Menampilkan daftar preset dalam satu kategori beserta deskripsi (PRD User Story #2).

**Trigger masuk:** Tap card kategori dari Home.

**Komponen:**
1. Header: tombol back (panah kiri), judul kategori (contoh "STUDY"), icon kategori besar di kanan atas.
2. List card preset, tiap card berisi:
   - Warna aksen kiri konsisten dengan warna kategori.
   - Angka frekuensi besar (misal "4.0 Hz").
   - Nama preset (misal "Memory").
   - Deskripsi singkat (1-2 baris, dari data preset).

**State:**
| State | Perilaku |
|---|---|
| Normal | List preset sesuai `category_id` yang diterima dari Home |
| Empty (seharusnya tidak pernah terjadi untuk 5 kategori MVP karena semua sudah terisi 20 preset) | Jika kategori punya 0 preset (edge case untuk kategori baru di masa depan) → tampilkan pesan "Belum ada preset di kategori ini" dengan ilustrasi netral, BUKAN layar kosong tanpa penjelasan |
| Loading | Skeleton loader untuk list card, kalau ada delay loading data |

**Interaksi:**
- Tap card preset → masuk ke Player Screen (Bagian 6), membawa parameter `preset_id`.
- Tap tombol back → kembali ke Home.

**Exit points:** Player Screen, atau back ke Home.

---

## 6. PLAYER SCREEN (paling kompleks — detail penuh)

**Tujuan:** Kontrol utama sesi audio — play/pause, timer, volume, ganti preset, breathing exercise.

**Trigger masuk:** Tap preset dari Category Detail, atau tap item dari Library/Sessions (rilis 1.1).

### 6.1 Komponen (urut dari atas ke bawah)

1. **Header**
   - Tombol close (X, biasanya kiri atas, warna sesuai aksen kategori) — menghentikan sesi dan kembali ke screen sebelumnya.
   - Judul preset (contoh "Memory") di tengah.

2. **Area ilustrasi/visual utama**
   - Ilustrasi statis atau animasi ringan (bernafas pelan / breathing motion) terkait tema kategori.
   - `[DECISION NEEDED]` — apakah ilustrasi bergerak halus (subtle animation, misal opacity/scale breathing loop) atau statis penuh seperti referensi kompetitor. Rekomendasi: animasi halus (bukan animasi besar) karena app kompetitor terlihat kaku dengan ilustrasi 100% statis — ini peluang diferensiasi murah tanpa menambah kompleksitas.

3. **Tombol "Breathing exercises"**
   - Tombol lebar di bawah ilustrasi.
   - Tap → buka Breathing Exercise sebagai modal/bottom sheet (Bagian 7), TIDAK menghentikan audio yang sedang jalan.

4. **Kontrol transport (3 tombol horizontal)**
   - **Kiri: Timer** (icon stopwatch) → tap membuka picker sleep timer (Bagian 6.3).
   - **Tengah: Play/Pause** (tombol besar, lingkaran) → toggle status audio.
   - **Kanan: Volume** (icon speaker) → tap membuka slider volume (inline expand atau bottom sheet, lihat Bagian 6.4).

5. **Bottom navigation TIDAK ditampilkan di Player Screen** — Player adalah full-screen experience, ini beda dari screen lain yang selalu punya bottom nav. Alasan: konsistensi dengan pola kompetitor DAN mencegah user tidak sengaja pindah tab saat sedang fokus/tidur (screen accidental tap).

### 6.2 State

| State | Perilaku |
|---|---|
| Loading (audio engine mempersiapkan buffer pertama) | Tombol play/pause menampilkan spinner kecil di dalamnya, BUKAN membiarkan user tap berkali-kali tanpa respons visual |
| Playing | Tombol tengah menampilkan icon pause, ilustrasi dalam mode "aktif" (animasi jalan jika ada) |
| Paused | Tombol tengah menampilkan icon play, ilustrasi dalam mode "diam" |
| Error (audio gagal start — misal AudioTrack gagal inisialisasi) | Tampilkan pesan singkat non-intrusive (snackbar/toast) "Gagal memutar audio, coba lagi" + tombol retry. JANGAN silent fail di mana user menekan play tapi tidak ada suara tanpa penjelasan apapun |
| Headphone tidak terdeteksi (lihat `audio-engine-spec.md` Bagian 6.4) | Banner non-blocking di atas ilustrasi: "Gunakan headphone untuk hasil terbaik" — muncul saat masuk screen jika terdeteksi speaker built-in, bisa di-dismiss, TIDAK block play |
| Headphone dicabut di tengah sesi | Auto-pause + snackbar "Sesi dijeda karena headphone dilepas" |

### 6.3 Sleep Timer — detail interaksi

- Tap icon timer → buka bottom sheet dengan pilihan durasi preset (misal 15/30/45/60 menit) + opsi custom.
- Setelah dipilih, icon timer berubah menampilkan countdown singkat (misal badge kecil "29:45").
- 30 detik terakhir: audio fade-out otomatis (spec teknis di `audio-engine-spec.md` Bagian 7.1) — UI menampilkan progress fade visual (misal ilustrasi meredup pelan) supaya user tahu sesi akan segera berakhir kalau kebetulan masih terjaga.
- Setelah timer habis: Player Screen otomatis kembali ke screen sebelumnya (Category Detail atau Home), TIDAK meninggalkan user di Player Screen dengan audio yang sudah mati tanpa indikasi.

### 6.4 Volume Control — detail interaksi

- Tap icon volume → slider muncul (inline di bawah kontrol transport, atau bottom sheet — `[DECISION NEEDED]`, rekomendasi: inline slider yang muncul dengan animasi expand supaya user tidak kehilangan konteks visual player).
- Minimal 1 slider: **Master volume**.
- Untuk MVP, kontrol volume terpisah antara binaural tone vs background noise **TIDAK WAJIB** di Player Screen (itu masuk fitur mixer C1 di rilis 2.0) — cukup 1 slider master untuk MVP.

### 6.5 Ganti preset di tengah sesi

- Skenario: user sedang di Player Screen preset A, lalu (dari Category Detail atau Library) memilih preset B.
- Perilaku: navigasi ke Player Screen preset B TANPA menutup dulu ke Home — audio melakukan cross-fade sesuai spec teknis (`audio-engine-spec.md` Bagian 7.2), Player Screen langsung menampilkan preset B begitu cross-fade dimulai.

### 6.6 Interaksi tombol close (X)

- Tap X → konfirmasi TIDAK diperlukan jika sesi baru berjalan < 2 menit (asumsi user belum "berinvestasi" waktu).
- Jika sesi sudah berjalan ≥ 2 menit, `[DECISION NEEDED]` — apakah perlu dialog konfirmasi "Yakin ingin mengakhiri sesi?" atau langsung keluar. Rekomendasi: langsung keluar tanpa konfirmasi (mengikuti pola kompetitor & prinsip UX "jangan menghalangi exit"), audio berhenti dengan fade-out cepat (200-300ms sesuai spec pause di `audio-engine-spec.md` Bagian 7.3) bukan cut mendadak.

**Exit points:** Kembali ke screen sebelumnya (Category Detail/Library/Home), atau ke Breathing Exercise (modal, tidak menghentikan audio).

---

## 7. BREATHING EXERCISE (MODAL/SUB-SCREEN)

**Tujuan:** Panduan visual bernapas terpisah dari player, sebagai pelengkap sesi (PRD S3 — diferensiasi karena kompetitor punya tombol ini tapi isinya tidak jelas).

**Trigger masuk:** Tap tombol "Breathing exercises" dari Player Screen.

**Komponen:**
- Modal/bottom sheet full atau setengah layar (`[DECISION NEEDED]` — rekomendasi: full-screen modal supaya fokus, dengan tombol close kembali ke Player).
- Animasi visual bernapas (lingkaran/bentuk yang membesar-mengecil mengikuti pola: tarik napas → tahan → buang napas → tahan).
- Teks instruksi mengikuti animasi ("Tarik napas...", "Tahan...", "Buang napas...").
- Pilihan pola napas (`[DECISION NEEDED untuk konten]` — rekomendasi pola sederhana untuk MVP: box breathing 4-4-4-4 detik, karena paling umum dan mudah diikuti tanpa penjelasan rumit).

**Catatan penting:** Audio dari Player Screen (binaural beat) **TETAP BERJALAN** di background selama Breathing Exercise modal terbuka — modal ini murni lapisan visual tambahan, bukan mengganti/menghentikan audio engine.

**State:**
| State | Perilaku |
|---|---|
| Normal | Animasi berjalan loop otomatis begitu modal dibuka |
| — | Tidak ada state loading/error kompleks — ini murni animasi lokal tanpa dependency data eksternal |

**Interaksi:**
- Tap close/back → tutup modal, kembali ke Player Screen, audio binaural tetap dalam kondisi yang sama (playing/paused) seperti sebelum modal dibuka.

**Exit points:** Kembali ke Player Screen.

**Catatan scope MVP:** Fitur ini masuk kategori SHOULD HAVE (S3) di PRD, bukan MUST HAVE. Untuk MVP murni (M1-M12), tombol "Breathing exercises" boleh **disembunyikan sepenuhnya** dari Player Screen jika tim ingin mempercepat rilis pertama, lalu diaktifkan saat rilis 1.1. `[DECISION NEEDED]` — konfirmasi apakah tombol ini include di MVP awal atau ditunda.

---

## 8. LIBRARY SCREEN

**Tujuan:** Tempat preset favorit/kustom user tersimpan (fungsi penuh baru aktif di rilis 1.1 / PRD S1).

**Trigger masuk:** Bottom nav tab "Library".

**Komponen (state kosong — ini yang aktif di MVP):**
1. Header: hamburger icon, judul "Library", icon statistik.
2. Tombol "Create preset" (lebar, warna aksen utama) — di MVP, tap tombol ini menampilkan pesan "Fitur ini akan segera hadir" (karena custom preset builder masuk C2, rilis 2.0), BUKAN membuka form kosong yang tidak berfungsi.
3. Ilustrasi kotak kosong (empty state) + teks "Oops! Your library is empty. Start now creating your own ones" (atau versi Bahasa Indonesia — `[DECISION NEEDED]` soal bahasa UI final, lihat Bagian 11).

**Komponen (state terisi — aktif mulai rilis 1.1):**
- List card preset tersimpan user, mirip struktur card di Category Detail, dengan tambahan opsi hapus/edit per item (swipe atau long-press menu).

**State:**
| State | Perilaku |
|---|---|
| Empty (default MVP) | Seperti dijelaskan di atas |
| Terisi (rilis 1.1) | List preset tersimpan |
| Loading | Skeleton loader saat fetch data lokal |

**Interaksi (MVP):**
- Tap "Create preset" → pesan "segera hadir" (toast/snackbar), TIDAK membuka screen baru yang kosong/broken.

**Interaksi (rilis 1.1):**
- Tap "Create preset" → buka Custom Preset Builder (di luar scope dokumen ini, akan didetailkan saat S1/C2 mulai dikerjakan).
- Tap item di list → Player Screen dengan preset tersebut.

**Exit points:** Player Screen (rilis 1.1), atau pindah tab.

---

## 9. SESSIONS SCREEN

**Tujuan:** Riwayat sesi yang sudah dijalankan & penjadwalan sesi otomatis (fungsi penuh di rilis 1.1 / PRD S2).

**Trigger masuk:** Bottom nav tab "Sessions".

**Komponen (state kosong — aktif di MVP):**
1. Header: hamburger icon, judul "Sessions", icon statistik.
2. Tombol "Create session" (lebar) — MVP: sama seperti Library, tap → pesan "segera hadir".
3. Ilustrasi + teks "You have no sessions. Try to create one right now!" (atau versi lokal).

**Komponen (state terisi — rilis 1.1):**
- List riwayat sesi (tanggal, preset yang dipakai, durasi) DAN/ATAU jadwal sesi otomatis mendatang — `[DECISION NEEDED]` apakah Sessions ini murni riwayat (log historis) atau juga mencakup penjadwalan (scheduler untuk sesi berulang tiap malam). Rekomendasi: gabungkan keduanya dalam satu screen dengan 2 sub-tab ("Riwayat" / "Terjadwal") saat rilis 1.1, tapi untuk MVP cukup empty state generik.

**State:** Sama pola dengan Library Screen (Empty default MVP / Terisi rilis 1.1 / Loading).

**Interaksi (MVP):** Tap "Create session" → pesan "segera hadir".

**Exit points:** Player Screen (rilis 1.1, jika tap riwayat untuk replay), atau pindah tab.

---

## 10. SIDE MENU (HAMBURGER)

**Tujuan:** Akses ke pengaturan dan halaman informasi/support.

**Trigger masuk:** Tap hamburger icon dari Home/Library/Sessions.

**Komponen:**
- Muncul sebagai bottom sheet atau slide-in drawer (`[DECISION NEEDED]` — rekomendasi bottom sheet, konsisten dengan referensi kompetitor yang dianalisis).
- Banner promosi premium di atas (jika model freemium aktif — lihat PRD Bagian 9) — untuk MVP tanpa ads generic, banner ini HANYA untuk fitur premium milik sendiri, BUKAN ads pihak ketiga.
- List menu:
  1. **Night mode** — toggle switch, langsung terapkan dark mode tanpa perlu restart app.
  2. **Share it** — buka share sheet Android native untuk membagikan link app.
  3. **Our applications** — jika ada app lain dari publisher yang sama (opsional, boleh disembunyikan jika belum ada app lain).
  4. **Rate this application** — buka Play Store listing app ini.
  5. **Contact** — buka email client atau form kontak sederhana.
  6. **About** — masuk ke About Screen (Bagian 11).

**State:** Murni UI statis, tidak ada loading/error kompleks kecuali toggle Night Mode gagal tersimpan (edge case, tampilkan toast error singkat jika local storage gagal write).

**Interaksi:** Tap item manapun → aksi sesuai (toggle, buka external app/browser, atau navigasi ke About).

**Exit points:** Tutup menu (tap di luar area menu / swipe down jika bottom sheet), atau navigasi ke About Screen.

---

## 11. ABOUT SCREEN (TERMASUK DISCLAIMER)

**Tujuan:** Informasi app + menampilkan ulang disclaimer kesehatan (PRD M9) supaya selalu bisa diakses, tidak cuma sekali muncul di onboarding.

**Trigger masuk:** Dari Side Menu → About.

**Komponen:**
1. Header: tombol back, judul "About".
2. Logo/nama app + versi aplikasi.
3. Deskripsi singkat app.
4. **Disclaimer lengkap** (teks sama persis dengan `audio-engine-spec.md` Bagian 11) — WAJIB ditampilkan penuh, bukan link keluar ke halaman web terpisah, supaya selalu bisa diakses offline.
5. Link ke Privacy Policy & Terms of Service (jika sudah tersedia; kalau belum, `[DECISION NEEDED]` — tim legal perlu menyiapkan sebelum rilis Play Store karena ini requirement wajib Google Play, bukan opsional).

**State:** Murni statis, tidak ada loading/error.

**Exit points:** Back ke Side Menu / screen asal.

---

## 12. BAHASA UI

`[DECISION NEEDED]` — dokumen ini dan referensi kompetitor menampilkan teks contoh dalam Bahasa Inggris ("Oops! Your library is empty..."), tapi target user disebutkan berbahasa Indonesia (percakapan riset dalam Bahasa Indonesia). Agent harus konfirmasi ke tim:
- **Opsi A**: UI penuh Bahasa Indonesia sejak awal.
- **Opsi B**: UI Bahasa Inggris (mengikuti pola kompetitor, lebih mudah untuk ekspansi internasional/iOS nanti), dengan kemungkinan localization Bahasa Indonesia di rilis berikutnya.

Rekomendasi: **Opsi A (Bahasa Indonesia)** untuk rilis Android awal karena target pasar utama jelas lokal, tapi struktur string HARUS memakai resource file (`strings.xml`) yang siap di-localize, BUKAN hardcoded, supaya tidak perlu refactor besar saat ekspansi.

---

## 13. MAPPING WARNA KATEGORI (referensi cepat — detail lengkap di Design System)

| Kategori | Warna aksen (referensi dari analisis kompetitor) |
|---|---|
| STUDY | Merah/coral |
| SPIRIT | Oranye |
| SLEEP | Hijau |
| BODY | Biru muda |
| BRAIN | Ungu/indigo |

Warna final (hex code), tipografi, dan komponen reusable didetailkan di dokumen **Design System** terpisah — dokumen ini hanya memastikan konsistensi mapping kategori-ke-warna di seluruh screen (Home card, Category Detail header, Player Screen accent).

---

## 14. RINGKASAN SEMUA `[DECISION NEEDED]` DI DOKUMEN INI

Agent WAJIB mengonfirmasi poin-poin berikut sebelum implementasi final (boleh mulai dengan rekomendasi default yang tertulis, tapi tandai sebagai asumsi sementara):

1. **Bagian 6.1** — ilustrasi Player Screen: animasi halus vs statis. *Rekomendasi: animasi halus.*
2. **Bagian 6.4** — volume control: inline slider vs bottom sheet. *Rekomendasi: inline slider.*
3. **Bagian 6.6** — konfirmasi dialog saat close sesi ≥ 2 menit. *Rekomendasi: tanpa dialog, langsung keluar dengan fade-out.*
4. **Bagian 7** — modal breathing exercise: full-screen vs setengah layar; termasuk di MVP atau ditunda ke rilis 1.1. *Rekomendasi: full-screen, ditunda ke rilis 1.1 (tombol disembunyikan di MVP).*
5. **Bagian 9** — Sessions screen: murni riwayat vs riwayat+penjadwalan. *Rekomendasi: gabungan, tapi baru relevan di rilis 1.1.*
6. **Bagian 10** — Side menu: bottom sheet vs slide-in drawer. *Rekomendasi: bottom sheet.*
7. **Bagian 12** — bahasa UI: Indonesia vs Inggris. *Rekomendasi: Indonesia, dengan string resource siap di-localize.*

---

## 15. DOKUMEN TERKAIT (STATUS)

| Dokumen | Status |
|---|---|
| `audio-engine-spec.md` | ✅ Selesai |
| `prd-main.md` | ✅ Selesai |
| `ux-flow-screen-spec.md` (dokumen ini) | ✅ Selesai |
| Design System | ⏳ Belum dibuat — disarankan berikutnya, karena Bagian 13 di sini butuh hex code final & tipografi |
| Monetization Doc | ⏳ Belum dibuat |
| Compliance Doc (Play Store policy) | ⏳ Belum dibuat |
