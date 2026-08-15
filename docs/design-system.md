# DESIGN SYSTEM
## Aplikasi Binaural Beats / Brainwave / Sleep & Focus — Android First

**Versi dokumen:** 1.0
**Status:** Siap dieksekusi oleh AI coding agent
**Dokumen terkait:** `prd-main.md`, `ux-flow-screen-spec.md`
**Filosofi dokumen:** Lean tapi lengkap — cukup token & komponen untuk konsistensi penuh di semua screen yang sudah didefinisikan di UX Flow Spec, tanpa token/komponen berlebih yang tidak dipakai di MVP.

---

## 0. KEPUTUSAN UTAMA (JANGAN DIDISKUSIKAN ULANG)

1. **Dark mode adalah tema DEFAULT**, bukan Light. Alasan: mayoritas use case app ini adalah malam hari/menjelang tidur (kategori SLEEP), dark mode mengurangi silau dan warna aksen kategori terlihat lebih hidup di atas background gelap.
2. **Light mode tetap disediakan** sebagai opsi (toggle "Night mode" di Side Menu, sesuai `ux-flow-screen-spec.md` Bagian 10) — relevan untuk use case STUDY/FOCUS di siang hari.
3. Toggle di Side Menu berlabel **"Night mode"** tapi secara teknis mengontrol dark/light theme secara umum — saat pertama kali install, toggle ini dalam posisi **ON (dark aktif)** sebagai default.
4. Sistem warna dibangun sebagai **token** (bukan hardcoded hex tersebar di kode), supaya switching dark/light tinggal ganti mapping token, bukan ganti value satu-satu di tiap screen.

---

## 1. COLOR TOKENS

### 1.1 Base tokens — Dark Theme (default)

| Token | Hex | Kegunaan |
|---|---|---|
| `color/background/primary` | `#0F1115` | Background utama seluruh screen |
| `color/background/secondary` | `#1A1D24` | Background card, bottom sheet, elevated surface |
| `color/background/tertiary` | `#242832` | Background elemen di atas card (misal nested component) |
| `color/text/primary` | `#F5F6F8` | Judul, teks utama |
| `color/text/secondary` | `#A8ADB8` | Subjudul, deskripsi, teks sekunder |
| `color/text/disabled` | `#5C6270` | Teks/tombol nonaktif |
| `color/border/subtle` | `#2E323C` | Divider, border card tipis |
| `color/overlay/scrim` | `#000000` @ 60% opacity | Overlay di belakang modal/bottom sheet |

### 1.2 Base tokens — Light Theme

| Token | Hex | Kegunaan |
|---|---|---|
| `color/background/primary` | `#FFFFFF` | Background utama seluruh screen |
| `color/background/secondary` | `#F4F5F7` | Background card, bottom sheet |
| `color/background/tertiary` | `#E9EAED` | Background elemen nested |
| `color/text/primary` | `#1A1D24` | Judul, teks utama |
| `color/text/secondary` | `#5C6270` | Subjudul, deskripsi |
| `color/text/disabled` | `#B0B4BC` | Teks/tombol nonaktif |
| `color/border/subtle` | `#E0E2E7` | Divider, border card tipis |
| `color/overlay/scrim` | `#000000` @ 40% opacity | Overlay di belakang modal/bottom sheet |

### 1.3 Warna aksen kategori (sama di kedua tema, tidak berubah dark/light)

Mengikuti mapping yang sudah dikunci di `ux-flow-screen-spec.md` Bagian 13, sekarang dengan hex final:

| Kategori | Token | Hex | Catatan |
|---|---|---|---|
| STUDY | `color/category/study` | `#FF6B6B` | Merah/coral |
| SPIRIT | `color/category/spirit` | `#FFA451` | Oranye |
| SLEEP | `color/category/sleep` | `#6DD98C` | Hijau |
| BODY | `color/category/body` | `#5DBEEA` | Biru muda |
| BRAIN | `color/category/brain` | `#8C7CF0` | Ungu/indigo |

**Aturan pemakaian**: warna kategori dipakai untuk strip aksen kiri card, icon kategori, dan warna tombol/kontrol utama di Player Screen SAAT preset dari kategori tersebut sedang aktif (misal tombol play di Player Screen preset SLEEP pakai `color/category/sleep`). Warna kategori TIDAK dipakai sebagai background penuh — hanya aksen, supaya tidak bentrok dengan dark/light base.

### 1.4 Semantic colors (sama di kedua tema)

| Token | Hex | Kegunaan |
|---|---|---|
| `color/semantic/error` | `#FF5A5F` | Pesan error, validasi gagal |
| `color/semantic/success` | `#4CD97B` | Konfirmasi berhasil |
| `color/semantic/warning` | `#FFB84D` | Banner warning (misal "gunakan headphone") |
| `color/brand/primary` | `#6C5CE7` | Warna brand utama — tombol CTA utama (misal "Create preset", "Mulai" di onboarding), TIDAK terikat kategori tertentu |

### 1.5 Kontras & aksesibilitas

Semua kombinasi `color/text/*` di atas `color/background/*` WAJIB memenuhi rasio kontras minimum **4.5:1** (WCAG AA untuk teks normal) — nilai hex di atas sudah dipilih memenuhi ini, TAPI jika agent mengubah nilai apapun di tabel ini, WAJIB verifikasi ulang rasio kontras sebelum dipakai.

---

## 2. TIPOGRAFI

### 2.1 Font family

`[DECISION NEEDED]` — rekomendasi: gunakan font system default Android (**Roboto** atau **Google Sans** jika tersedia) untuk MVP, supaya tidak menambah bundle size dari custom font, dan rendering sudah teroptimasi di semua device Android. Custom font (misal font display yang lebih personality-driven) bisa dipertimbangkan di rilis 1.1+ setelah brand identity lebih matang.

### 2.2 Skala tipografi

| Token | Ukuran (sp) | Weight | Line height | Kegunaan |
|---|---|---|---|---|
| `type/display` | 32 | Bold (700) | 40 | Judul besar (misal "Home", "Library" — judul screen level atas) |
| `type/heading-1` | 24 | Bold (700) | 32 | Judul kategori di Category Detail (misal "STUDY"), judul preset di Player |
| `type/heading-2` | 18 | SemiBold (600) | 24 | Judul card, judul section |
| `type/body` | 16 | Regular (400) | 24 | Teks deskripsi, konten utama |
| `type/body-small` | 14 | Regular (400) | 20 | Subjudul card (misal "Memory \| Focus \| Aid"), caption |
| `type/label` | 13 | Medium (500) | 16 | Label tombol, label bottom nav |
| `type/frequency` | 28 | Bold (700) | 32 | Khusus angka frekuensi Hz di card preset (misal "4.0 Hz") — dibuat token terpisah karena ini elemen visual yang menonjol dan berulang di banyak screen |

---

## 3. SPACING SCALE

Berbasis kelipatan 4dp (standar Android), token bernama supaya konsisten dipakai bukan angka bebas:

| Token | Nilai (dp) |
|---|---|
| `space/xs` | 4 |
| `space/sm` | 8 |
| `space/md` | 16 |
| `space/lg` | 24 |
| `space/xl` | 32 |
| `space/xxl` | 48 |

**Aturan pemakaian umum**: padding dalam card = `space/md`, jarak antar card dalam list = `space/sm`, margin screen kiri-kanan = `space/md`, jarak antar section besar (misal header ke content) = `space/lg`.

---

## 4. RADIUS & ELEVATION

| Token | Nilai | Kegunaan |
|---|---|---|
| `radius/sm` | 8dp | Chip, badge kecil |
| `radius/md` | 12dp | Card standar (kategori, preset) |
| `radius/lg` | 20dp | Bottom sheet (radius atas), tombol besar (CTA) |
| `radius/full` | 999dp (pill) | Tombol play/pause bulat, avatar |

| Token | Elevation (dp) | Kegunaan |
|---|---|---|
| `elevation/card` | 2 | Card standar di list |
| `elevation/floating` | 8 | Bottom sheet, modal, FAB jika ada |

**Catatan dark mode**: elevation di dark theme sebaiknya direpresentasikan dengan sedikit peningkatan brightness pada `color/background/secondary` (bukan hanya shadow, karena shadow kurang terlihat di background gelap) — sesuai praktik Material Design dark theme.

---

## 5. KOMPONEN INTI

Hanya komponen yang benar-benar dipakai di screen yang sudah didefinisikan di `ux-flow-screen-spec.md`. Jangan tambah komponen di luar daftar ini tanpa alasan yang terhubung ke screen spec yang sudah ada.

### 5.1 Category Card (Home Screen)

**Anatomi:**
- Container: `color/background/secondary`, `radius/md`, padding `space/md`.
- Strip aksen kiri: lebar 4dp, warna sesuai `color/category/*`, menempel di tepi kiri container, tinggi penuh mengikuti card.
- Icon kategori: 40x40dp, warna sesuai `color/category/*`, posisi kiri atas dalam card.
- Judul: `type/heading-2`, `color/text/primary`.
- Subjudul (daftar preset dipisah "|"): `type/body-small`, `color/text/secondary`.

**State:**
- Default: seperti di atas.
- Pressed: `color/background/tertiary` sebagai background sementara (ripple effect Android native).

### 5.2 Preset Card (Category Detail Screen)

**Anatomi:**
- Container: sama seperti Category Card (background, radius, strip aksen).
- Angka frekuensi: `type/frequency`, warna sesuai `color/category/*` kategori aktif.
- Judul preset: `type/heading-2`, `color/text/primary`.
- Deskripsi: `type/body-small`, `color/text/secondary`, maksimal 2 baris (truncate dengan ellipsis jika lebih panjang).

**State:** Sama pola dengan Category Card (default/pressed).

### 5.3 Primary Button (CTA)

**Anatomi:**
- Background: `color/brand/primary` (untuk aksi umum) ATAU warna kategori aktif (untuk aksi kontekstual di Player Screen, misal tombol terkait sesi yang sedang berjalan).
- Text: `type/label`, warna kontras terhadap background tombol (putih di kebanyakan kasus).
- Radius: `radius/lg`.
- Padding vertikal: `space/md`, horizontal: `space/lg`.
- Lebar: full-width mengikuti container kecuali dinyatakan lain (misal tombol di dalam bottom sheet kecil).

**State:**
| State | Perilaku visual |
|---|---|
| Default | Seperti anatomi di atas |
| Pressed | Opacity turun ke 85% |
| Disabled | Background `color/text/disabled`, text `color/background/primary` (kontras rendah secara sengaja untuk sinyal nonaktif) — dipakai misal tombol "Mulai" di Onboarding slide 3 sebelum checkbox dicentang |
| Loading | Spinner kecil menggantikan teks, tombol tetap ukuran sama (jangan resize saat loading) |

### 5.4 Icon Button (transport control, header icons)

**Anatomi:**
- Ukuran tap target minimum **48x48dp** (standar aksesibilitas Android, WAJIB dipenuhi walau icon visualnya lebih kecil, misal icon 24x24dp di dalam tap area 48x48dp).
- Icon warna `color/text/primary` untuk icon netral (back, hamburger), warna kategori aktif untuk icon kontekstual (misal tombol play di Player Screen).

**Varian khusus — Tombol Play/Pause (Player Screen):**
- Ukuran lebih besar dari icon button biasa: 72x72dp.
- Background: warna kategori aktif dengan opacity rendah (misal 15%) sebagai lingkaran, icon di tengah warna kategori aktif solid.
- `radius/full`.

### 5.5 Bottom Navigation

**Anatomi:**
- 3 item (Home/Library/Sessions), masing-masing: icon di atas, label `type/label` di bawah.
- Item aktif: icon + label warna `color/brand/primary`.
- Item nonaktif: icon + label warna `color/text/secondary`.
- Background: `color/background/secondary`, dengan `color/border/subtle` sebagai top border tipis (1dp) untuk memisahkan dari content di atasnya.
- **Tidak muncul di Player Screen** (sesuai keputusan `ux-flow-screen-spec.md` Bagian 6.1).

### 5.6 Slider (Volume Control)

**Anatomi:**
- Track: `color/background/tertiary` untuk bagian belum terisi, warna kategori aktif untuk bagian terisi.
- Thumb: lingkaran solid warna kategori aktif, ukuran 20dp, dengan tap target diperluas ke 40dp untuk kemudahan drag di layar kecil/kondisi kamar gelap.

### 5.7 Bottom Sheet

**Anatomi:**
- Background: `color/background/secondary`.
- Radius atas: `radius/lg` (kiri & kanan atas saja, bawah menyatu dengan tepi layar).
- Drag handle: garis kecil abu-abu (`color/border/subtle`, diperbesar sedikit opacity) di tengah atas, 32x4dp.
- Overlay di belakangnya: `color/overlay/scrim`.
- Dipakai untuk: Side Menu, Sleep Timer picker, Breathing Exercise (jika diputuskan bottom sheet bukan full-screen — lihat `ux-flow-screen-spec.md` Bagian 14 poin 4).

### 5.8 Toggle Switch (Night Mode, dll)

**Anatomi:**
- Mengikuti komponen switch native Android/Material, dengan warna track aktif = `color/brand/primary`, track nonaktif = `color/background/tertiary`.

### 5.9 Empty State (Library/Sessions kosong)

**Anatomi:**
- Ilustrasi netral (warna monokrom/muted, TIDAK memakai warna kategori spesifik karena empty state ini general, bukan terikat satu kategori) — posisi tengah.
- Teks judul kecil: `type/body`, `color/text/secondary`.
- Posisi vertikal: tengah area content (bukan menempel ke atas atau bawah).

### 5.10 Skeleton Loader

**Anatomi:**
- Bentuk mengikuti komponen aslinya (card kategori/preset), warna `color/background/tertiary` dengan animasi shimmer halus (gradient bergerak dari kiri ke kanan, durasi loop 1.2 detik).
- Dipakai di semua state "Loading" yang didefinisikan di `ux-flow-screen-spec.md` (Home, Category Detail, Library, Sessions).

---

## 6. ICON STYLE

`[DECISION NEEDED]` — rekomendasi: gunakan **icon set outline/line-art** (bukan filled solid) untuk kesan ringan dan tenang, konsisten dengan tema wellness. Icon kategori (STUDY/SPIRIT/SLEEP/BODY/BRAIN) mengikuti gaya line-art sesuai referensi kompetitor yang sudah dianalisis (icon lampu otak, lotus, bulan sabit, orang meditasi, otak dengan tangan). Stroke width konsisten **1.5–2dp** di semua icon.

Sumber icon: gunakan icon set open-source yang konsisten satu keluarga (misal Lucide Icons, Phosphor Icons, atau Material Symbols Outlined) — **JANGAN campur beberapa icon set berbeda gaya** dalam satu app, ini akan terlihat tidak konsisten secara visual.

---

## 7. MOTION & ANIMATION

Durasi dan easing standar, dipakai konsisten di semua transisi UI (terpisah dari fade audio yang sudah didefinisikan di `audio-engine-spec.md`):

| Token | Durasi | Easing | Kegunaan |
|---|---|---|---|
| `motion/fast` | 150ms | ease-out | Perubahan kecil (pressed state, toggle) |
| `motion/medium` | 250ms | ease-in-out | Transisi antar screen, buka/tutup bottom sheet |
| `motion/slow` | 400ms | ease-in-out | Perubahan besar (misal expand volume slider inline di Player Screen) |
| `motion/breathing-illustration` | 4000ms loop | ease-in-out (sine-like) | Animasi halus ilustrasi Player Screen (lihat `ux-flow-screen-spec.md` Bagian 6.1) — durasi 4 detik per siklus membesar-mengecil, MENYATU dengan konsep breathing, bukan animasi cepat/playful |

**Prinsip umum**: animasi di app ini harus terasa "tenang", bukan cepat/energik. Hindari easing bouncy/elastic yang biasa dipakai di app gaming — semua transisi pakai ease-in-out atau ease-out yang halus.

---

## 8. DARK/LIGHT THEME — CATATAN IMPLEMENTASI TEKNIS

- Implementasikan sebagai **2 set token value** yang di-swap berdasarkan state toggle "Night mode" (lihat Bagian 0), BUKAN dua layout terpisah. Semua komponen di Bagian 5 harus otomatis mengikuti tema aktif tanpa perlu kode kondisional tersebar di tiap screen.
- Android: gunakan mekanisme resource qualifier (`values-night/`) ATAU state management terpusat (misal `DataStore` untuk preference + theme provider di level aplikasi) — `[DECISION NEEDED]` teknis, tapi prinsipnya: satu sumber kebenaran untuk tema aktif, bukan dicek ulang per screen secara manual.
- Warna kategori (Bagian 1.3) dan semantic colors (Bagian 1.4) **TIDAK berubah** antara dark/light — hanya token background/text/border yang berbeda mapping.
- Default saat install pertama: **dark** (lihat Bagian 0 poin 3).

---

## 9. RINGKASAN SEMUA `[DECISION NEEDED]` DI DOKUMEN INI

1. **Bagian 2.1** — font family: system default (Roboto) vs custom. *Rekomendasi: system default untuk MVP.*
2. **Bagian 6** — icon set: outline/line-art dari library open-source mana. *Rekomendasi: Lucide Icons atau Material Symbols Outlined, pilih satu, konsisten.*
3. **Bagian 8** — mekanisme teknis switch tema: resource qualifier vs state management terpusat. *Rekomendasi: state management terpusat (lebih fleksibel untuk toggle manual, bukan cuma ikut system dark mode OS).*

---

## 10. DOKUMEN TERKAIT (STATUS)

| Dokumen | Status |
|---|---|
| `audio-engine-spec.md` | ✅ Selesai |
| `prd-main.md` | ✅ Selesai |
| `ux-flow-screen-spec.md` | ✅ Selesai |
| `design-system.md` (dokumen ini) | ✅ Selesai |
| Monetization Doc | ⏳ Belum dibuat |
| Compliance Doc (Play Store policy) | ⏳ Belum dibuat |
