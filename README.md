# SpaceHub Android App

Native Android client for the **SpaceHub Co-Working Space Management System**.

> **Main repository:** [[https://github.com/ICT-Mahidol/2025-ITCS383-Emerald  ](https://github.com/Chayocha23/2025-ITCS383-Emerald.git)
](https://github.com/Chayocha23/2025-ITCS383-Emerald.git)
> **Backend (Render):** [[https://two025-itcs383-emerald-backend.onrender.com](https://two025-itcs383-emerald-backend.onrender.com)](https://github.com/btk5/2025-ITCS383-Bughair-Android.git)

---

## Features

All web application features are available on mobile:

### Customer
| Feature | Description |
|---|---|
| Login / Register | Secure authentication with role-based routing |
| Dashboard | Membership status + upcoming bookings summary |
| Desk Booking | 3-step flow: select date → choose time slot → payment |
| My Bookings | View, track, and cancel reservations |
| Membership | Subscribe to Day/Month/Year plans with 3 payment methods |
| Customer Support | Submit tickets, quick-reply buttons, view replies (Feature 2) |
| Notifications | Badge polling every 30 s for unread messages + booking updates (Feature 3) |

### Employee
| Feature | Description |
|---|---|
| Reservations | View all reservations, confirm pending, check-in confirmed |
| Support Tickets | View pending tickets, reply to customers |

### Manager
| Feature | Description |
|---|---|
| Daily Summary | Reservations, income, net income, member count |
| Employee Count | Overview of active employees |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM (ViewModel + LiveData) |
| Networking | Retrofit 2 + OkHttp + Gson |
| Async | Kotlin Coroutines |
| Navigation | Navigation Component + Bottom Navigation |
| UI | Material Components 3 |
| Testing | JUnit 4 + Mockito + Coroutines Test |
| CI | GitHub Actions |
| Code Quality | SonarQube Cloud |

---

## Setup & Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps

1. Clone this repository:
   ```bash
   git clone [[https://github.com/ICT-Mahidol/2025-ITCS383-Bughair-Android.git](https://github.com/Poschapat/2025-ITCS383-Bughair-Android.git)](https://github.com/btk5/2025-ITCS383-Bughair-Android.git)
   cd SpaceHub-Android
   ```

2. Open in Android Studio → File → Open

3. Set the backend URL in `RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "https://two025-itcs383-emerald-backend.onrender.com/"
   ```

4. Run on emulator or physical device (Android 7.0+ / API 24+)

### Build via command line
```bash
./gradlew assembleDebug
```

### Run tests
```bash
./gradlew test
```

---

## Project Structure

```
app/src/main/java/com/spacehub/app/
├── data/
│   ├── model/          # Data classes (User, Booking, Membership, etc.)
│   ├── network/        # ApiService (Retrofit interface) + RetrofitClient
│   └── repository/     # SpaceHubRepository (single source of truth)
├── ui/
│   ├── auth/           # SplashActivity, LoginActivity, RegisterActivity, AuthViewModel
│   ├── customer/       # MainActivity, 5 Fragments (Dashboard, Book, MyBookings, Membership, Support)
│   ├── employee/       # EmployeeActivity + adapters
│   └── manager/        # ManagerActivity
└── util/               # SessionManager (SharedPreferences)

app/src/test/java/com/spacehub/app/
├── AuthViewModelTest.kt        # 10 test cases
├── CustomerViewModelTest.kt    # 18 test cases
├── SessionManagerTest.kt       # 12 test cases
└── SpaceHubRepositoryTest.kt   # 20 test cases
```

---

## API Endpoints Used

| Method | Endpoint | Used by |
|---|---|---|
| POST | `/api/login` | LoginActivity |
| POST | `/api/register` | RegisterActivity |
| GET | `/api/membership/:userId` | Dashboard, MembershipFragment |
| POST | `/api/membership` | MembershipFragment |
| POST | `/api/membership/:id/pay` | MembershipFragment |
| GET | `/api/bookings/availability` | BookingFragment |
| POST | `/api/bookings` | BookingFragment |
| POST | `/api/bookings/:id/pay` | BookingFragment |
| GET | `/api/bookings/user/:userId` | MyBookingsFragment |
| POST | `/api/bookings/:id/cancel` | MyBookingsFragment |
| POST | `/api/support/tickets` | SupportFragment |
| GET | `/api/user/messages` | SupportFragment |
| POST | `/api/support/tickets/:id/read` | SupportFragment |
| GET | `/api/user/notifications` | MainActivity (polling) |
| GET | `/api/employee/reservations` | EmployeeActivity |
| POST | `/api/employee/checkin` | EmployeeActivity |
| POST | `/api/employee/bookings/:id/confirm` | EmployeeActivity |
| GET | `/api/employee/tickets` | EmployeeActivity |
| POST | `/api/support/tickets/:id/reply` | EmployeeActivity |
| GET | `/api/manager/summary` | ManagerActivity |
| GET | `/api/manager/employees` | ManagerActivity |

---

## Test Coverage

Run with coverage report:
```bash
./gradlew test jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

Total: **60 test cases** across 4 test files, targeting **>90% coverage** on ViewModel and Repository layers.

---

