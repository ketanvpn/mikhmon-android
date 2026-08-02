# Mikhmon Android

Aplikasi Android untuk manajemen hotspot MikroTik dengan fitur lengkap seperti Mikhmon web-based.

## Fitur Utama

### ✅ Sudah Diimplementasikan

1. **Router Management**
   - Multi-router support
   - Connection management
   - Auto-reconnect

2. **Hotspot User Management**
   - List semua user
   - Add/Edit/Delete user
   - Enable/Disable user
   - Filter by profile
   - Search user

3. **User Profile Management**
   - List profiles
   - Speed limit settings
   - Validity settings

4. **Voucher System**
   - Generate batch vouchers
   - Multiple character modes
   - Voucher status tracking

5. **Real-time Monitoring**
   - Active users list
   - Traffic monitoring
   - Auto-refresh

6. **Dashboard**
   - Router status
   - Quick stats
   - Quick actions

7. **Settings**
   - Theme selection
   - Log viewer
   - App preferences

## Tech Stack

- **Language**: Kotlin 100%
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Custom Mikrotik API Client
- **Database**: Room
- **UI**: Jetpack Compose + Material 3
- **Logging**: Timber + Custom Logger

## Project Structure

```
app/
├── core/           # Core layer (API, Logging, Utils)
├── data/           # Data layer (Models, Repositories)
├── domain/         # Domain layer (Use Cases)
└── presentation/   # UI layer (Screens, ViewModels)
```

## Cara Build

1. Buka project di Android Studio
2. Sync Gradle
3. Build > Make Project
4. Run di device/emulator

## Logging System

Aplikasi memiliki sistem logging komprehensif dengan kategori:
- `API` - Mikrotik API calls
- `AUTH` - Authentication
- `USER` - User operations
- `VOUCHER` - Voucher operations
- `SYNC` - Data synchronization
- `UI` - UI events
- `SYSTEM` - System events

## Fitur Coming Soon

- [ ] Voucher printing (thermal printer)
- [ ] QR code generation
- [ ] Sales reports with charts
- [ ] Push notifications
- [ ] Multi-language support
- [ ] Dark/Light theme
- [ ] Data export (CSV, PDF)

## Lisensi

MIT License

---

Dibuat dengan ❤️ untuk komunitas MikroTik Indonesia
