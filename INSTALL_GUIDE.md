# Panduan Instalasi Android Studio untuk Mikhmon

## Step 1: Download Android Studio

1. Buka browser yang sudah terbuka di https://developer.android.com/studio
2. Klik tombol **"Download Android Studio"**
3. Scroll ke bawah, centang "I have read and agree to the terms"
4. Klik download (file size ~1GB)

## Step 2: Install Android Studio

1. Setelah download selesai, buka file `android-studio-xxx-windows.exe`
2. Klik **Yes** jika diminta izin administrator
3. Klik **Next** pada welcome screen
4. Pilih komponen yang akan diinstall (biarkan default):
   - Android Studio
   - Android Virtual Device (centang)
5. Pilih lokasi instalasi (default: C:\Program Files\Android\Android Studio)
6. Klik **Install**
7. Tunggu proses instalasi (5-10 menit)
8. Klik **Finish**

## Step 3: Setup Android Studio (First Run)

1. Buka Android Studio dari Start Menu
2. Pilih **Do not import settings** (klik OK)
3. Setup Wizard akan muncul:
   - Pilih **Standard** installation
   - Klik **Next**
   - Pilih tema (Light/Dark)
   - Klik **Next**
   - Review settings, klik **Finish**
4. Tunggu download SDK dan komponen (10-20 menit, tergantung internet)

## Step 4: Buka Project Mikhmon

Setelah setup selesai:

1. Klik **Open** di splash screen
2. Navigate ke: `C:\Users\Wongndeso\Documents\Project AI\aplikasi wifi\MikhmonAndroid`
3. Pilih folder tersebut dan klik **OK**
4. Tunggu Gradle sync (beberapa menit pertama kali)

## Step 5: Build & Run

### Opsi A: Run di Emulator
1. Klik **Device Manager** (icon phone di kanan)
2. Klik **Create Device**
3. Pilih device (contoh: Pixel 6)
4. Pilih system image (Android 14 atau 15)
5. Klik **Next** → **Finish**
6. Jalankan emulator (klik play button)
7. Klik **Run** (icon play hijau) di Android Studio

### Opsi B: Run di HP Real
1. Aktifkan **Developer Options** di HP:
   - Settings → About Phone
   - Tap "Build Number" 7x
2. Aktifkan **USB Debugging**:
   - Settings → Developer Options → USB Debugging (ON)
3. Hubungkan HP ke PC via USB
4. Di Android Studio, HP akan muncul di dropdown device
5. Klik **Run** (icon play hijau)

### Opsi C: Build APK Manual
1. Menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Tunggu proses selesai
3. Klik **locate** untuk membuka folder APK
4. Copy APK ke HP dan install

## Troubleshooting

### Error: "SDK not found"
- Buka **Tools → SDK Manager**
- Download Android SDK 35

### Error: "Gradle sync failed"
- Klik **File → Sync Project with Gradle Files**
- Atau: **Build → Clean Project**

### Error: "JDK not found"
- Android Studio sudah include JDK
- Jika error, cek: **File → Project Structure → SDK Location**

### Slow Build/Emulator
- Enable HAXM: **Tools → SDK Manager → SDK Tools → Intel x86 Emulator Accelerator**
- Untuk AMD: Enable SVM di BIOS

## Shortcut Berguna

| Shortcut | Fungsi |
|----------|--------|
| `Ctrl+F9` | Build Project |
| `Shift+F10` | Run |
| `Shift+F9` | Debug |
| `Ctrl+Shift+A` | Find Action |
| `Ctrl+N` | Find Class |
| `Ctrl+Shift+N` | Find File |
| `Alt+Enter` | Quick Fix |

---

## Butuh Bantuan?

Jika ada error, beri tahu saya dengan:
1. Screenshot error
2. Copy pesan error dari Build Output

Selamat coding! 🎉
