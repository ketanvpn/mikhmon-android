# Mikhmon Android - Project Architecture

## Overview
Aplikasi Android untuk manajemen hotspot Mikrotik dengan fitur lengkap seperti Mikhmon web-based.

## Tech Stack
- **Language**: Kotlin 100%
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: OkHttp + Retrofit (for REST), custom Mikrotik API client
- **Database**: Room (local cache), DataStore (preferences)
- **Async**: Coroutines + Flow
- **UI**: Jetpack Compose + Material 3
- **Logging**: Timber + custom Logger
- **Testing**: JUnit5, MockK, Turbine

## Project Structure

```
app/
├── src/main/java/com/mikhmon/android/
│   ├── MikhmonApp.kt                    # Application class
│   │
│   ├── di/                              # Dependency Injection
│   │   ├── AppModule.kt
│   │   ├── NetworkModule.kt
│   │   ├── DatabaseModule.kt
│   │   └── RepositoryModule.kt
│   │
│   ├── core/                            # Core Layer
│   │   ├── api/                         # Mikrotik API Client
│   │   │   ├── MikrotikApi.kt
│   │   │   ├── MikrotikConnection.kt
│   │   │   ├── MikrotikRequest.kt
│   │   │   └── MikrotikResponse.kt
│   │   │
│   │   ├── logging/                     # Logging System
│   │   │   ├── Logger.kt
│   │   │   ├── LogLevel.kt
│   │   │   └── LogEntry.kt
│   │   │
│   │   ├── util/                        # Utilities
│   │   │   ├── TimeUtils.kt
│   │   │   ├── ByteUtils.kt
│   │   │   └── ValidationUtils.kt
│   │   │
│   │   └── network/                     # Network State
│   │       └── NetworkMonitor.kt
│   │
│   ├── data/                            # Data Layer
│   │   ├── model/                       # Data Models
│   │   │   ├── User.kt
│   │   │   ├── UserProfile.kt
│   │   │   ├── Voucher.kt
│   │   │   ├── Router.kt
│   │   │   └── Session.kt
│   │   │
│   │   ├── remote/                      # Remote Data Sources
│   │   │   ├── MikrotikDataSource.kt
│   │   │   ├── UserRemoteDataSource.kt
│   │   │   └── ReportRemoteDataSource.kt
│   │   │
│   │   ├── local/                       # Local Data Sources
│   │   │   ├── database/
│   │   │   │   ├── MikhmonDatabase.kt
│   │   │   │   ├── UserDao.kt
│   │   │   │   └── LogDao.kt
│   │   │   └── preferences/
│   │   │       └── AppPreferences.kt
│   │   │
│   │   ├── repository/                  # Repositories
│   │   │   ├── UserRepository.kt
│   │   │   ├── RouterRepository.kt
│   │   │   ├── VoucherRepository.kt
│   │   │   └── ReportRepository.kt
│   │   │
│   │   └── mapper/                      # Data Mappers
│   │       └── UserMapper.kt
│   │
│   ├── domain/                          # Domain Layer
│   │   ├── model/                       # Domain Models
│   │   │   ├── HotspotUser.kt
│   │   │   ├── UserStatus.kt
│   │   │   └── Package.kt
│   │   │
│   │   ├── usecase/                     # Use Cases
│   │   │   ├── user/
│   │   │   │   ├── GetUsersUseCase.kt
│   │   │   │   ├── AddUserUseCase.kt
│   │   │   │   ├── UpdateUserUseCase.kt
│   │   │   │   └── DeleteUserUseCase.kt
│   │   │   ├── voucher/
│   │   │   │   ├── GenerateVouchersUseCase.kt
│   │   │   │   └── PrintVouchersUseCase.kt
│   │   │   └── router/
│   │   │       ├── ConnectRouterUseCase.kt
│   │   │       └── GetRouterStatusUseCase.kt
│   │   │
│   │   └── repository/                  # Repository Interfaces
│   │       └── IUserRepository.kt
│   │
│   ├── presentation/                    # Presentation Layer
│   │   ├── navigation/
│   │   │   ├── NavGraph.kt
│   │   │   └── Screen.kt
│   │   │
│   │   ├── common/                      # Shared UI Components
│   │   │   ├── components/
│   │   │   │   ├── MikhmonButton.kt
│   │   │   │   ├── MikhmonTextField.kt
│   │   │   │   ├── LoadingIndicator.kt
│   │   │   │   └── ErrorDialog.kt
│   │   │   └── theme/
│   │   │       ├── Theme.kt
│   │   │       ├── Color.kt
│   │   │       └── Typography.kt
│   │   │
│   │   ├── features/                    # Feature-based Screens
│   │   │   ├── login/
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   └── LoginViewModel.kt
│   │   │   │
│   │   │   ├── dashboard/
│   │   │   │   ├── DashboardScreen.kt
│   │   │   │   └── DashboardViewModel.kt
│   │   │   │
│   │   │   ├── users/
│   │   │   │   ├── UserListScreen.kt
│   │   │   │   ├── UserDetailScreen.kt
│   │   │   │   ├── AddUserScreen.kt
│   │   │   │   └── UserViewModel.kt
│   │   │   │
│   │   │   ├── vouchers/
│   │   │   │   ├── VoucherListScreen.kt
│   │   │   │   ├── GenerateVoucherScreen.kt
│   │   │   │   └── VoucherViewModel.kt
│   │   │   │
│   │   │   ├── profiles/
│   │   │   │   ├── ProfileListScreen.kt
│   │   │   │   ├── AddProfileScreen.kt
│   │   │   │   └── ProfileViewModel.kt
│   │   │   │
│   │   │   ├── monitoring/
│   │   │   │   ├── ActiveUsersScreen.kt
│   │   │   │   ├── TrafficMonitorScreen.kt
│   │   │   │   └── MonitoringViewModel.kt
│   │   │   │
│   │   │   ├── reports/
│   │   │   │   ├── ReportScreen.kt
│   │   │   │   └── ReportViewModel.kt
│   │   │   │
│   │   │   ├── routers/
│   │   │   │   ├── RouterListScreen.kt
│   │   │   │   ├── AddRouterScreen.kt
│   │   │   │   └── RouterViewModel.kt
│   │   │   │
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   │
│   │   └── MainActivity.kt
│   │
│   └── service/                         # Background Services
│       ├── MikrotikSyncService.kt
│       └── NotificationService.kt
│
├── build.gradle.kts
└── proguard-rules.pro
```

## Feature List

### 1. Router Management
- Add/Edit/Delete routers
- Multi-router support
- Connection status monitoring
- Auto-reconnect on disconnect

### 2. Hotspot User Management
- View all hotspot users
- Add/Edit/Delete users
- Enable/Disable users
- Reset user password
- User status (online/offline)
- Session details

### 3. User Profile Management
- Create/Edit/Delete profiles
- Speed limit settings (up/down)
- Data limit settings
- Time limit settings
- Validity period

### 4. Voucher System
- Generate batch vouchers
- Print vouchers (thermal printer support)
- Voucher templates
- Voucher status tracking
- QR code generation

### 5. Real-time Monitoring
- Active users list
- Traffic usage per user
- Bandwidth monitoring
- Resource usage (CPU, RAM)
- Signal strength (for wireless)

### 6. Reporting
- Daily/Monthly reports
- Income reports
- User statistics
- Traffic reports
- Export to PDF/CSV

### 7. Settings
- Theme (Light/Dark)
- Notification settings
- Auto-refresh interval
- Logging level
- Data backup/restore

## Logging System

### Log Levels
- **VERBOSE**: Detailed debugging info
- **DEBUG**: Debug information
- **INFO**: General information
- **WARNING**: Warnings
- **ERROR**: Errors
- **CRITICAL**: Critical errors

### Log Categories
- `API` - Mikrotik API calls
- `AUTH` - Authentication
- `USER` - User operations
- `VOUCHER` - Voucher operations
- `SYNC` - Data synchronization
- `UI` - UI events
- `SYSTEM` - System events

### Log Entry Format
```
[TIMESTAMP] [LEVEL] [CATEGORY] [CORRELATION_ID] Message
Example:
[2026-08-02 08:28:34.123] [DEBUG] [API] [abc123] Connecting to router 192.168.88.1:8728
```

## Error Handling Strategy
- Result wrapper pattern for all operations
- Sealed classes for error types
- User-friendly error messages
- Automatic retry with exponential backoff
- Offline mode with local cache

## Security
- Encrypted credential storage
- Session timeout
- Biometric authentication option
- SSL/TLS support for API
- No logging of sensitive data

## Testing Strategy
- Unit tests for domain layer
- Integration tests for data layer
- UI tests for critical flows
- Mock Mikrotik API for testing
