#  İlaç Asistanım

A mobile medication management application for patients and caregivers, built with React Native and a Node.js backend.

---

##  About

**İlaç Asistanım** is a cross-platform mobile app designed to help patients (especially elderly individuals) manage their medication schedules effectively. Caregivers (*Sorumlu*) can monitor and manage medications on behalf of patients (*Hasta*), receive reminders, and track adherence — all from a single app.

---

##  Features

-  **Dual Role System** — Separate flows for Patients (*Hasta*) and Caregivers (*Sorumlu*)
-  **Medication Reminders** — Push notifications via Firebase Cloud Messaging (FCM) and Apple Push Notification Service (APNs)
-  **Medication Scheduling** — Add, edit, and track daily/weekly medication plans
-  **Background Job Queue** — Reliable reminder delivery powered by BullMQ and Redis
-  **Subscription / Payment** — Premium features via Stripe integration
-  **OAuth Authentication** — Secure login flow with token-based session management
-  **Adherence Tracking** — Monitor medication intake history

---

## 🛠️ Tech Stack

### Frontend
| Technology | Purpose |
|---|---|
| React Native | Cross-platform mobile UI (iOS & Android) |

### Backend
| Technology | Purpose |
|---|---|
| Node.js | REST API server |
| PostgreSQL | Primary relational database |
| Redis | Caching & job queue store |
| BullMQ | Background job/reminder queue |
| FCM / APNs | Push notification delivery |
| Stripe | Payment processing |
| OAuth 2.0 | Authentication provider |

---

##  Architecture Overview

The application follows a client-server architecture:

- The **React Native** mobile client communicates with the **Node.js** REST API.
- Medication reminders are scheduled as background jobs via **BullMQ**, backed by **Redis**.
- Push notifications are dispatched through **FCM** (Android) and **APNs** (iOS).
- User and medication data are persisted in **PostgreSQL**.
- Payments and subscriptions are handled through the **Stripe** API.

---

##  Getting Started

### Prerequisites

- Node.js >= 18
- PostgreSQL
- Redis
- React Native development environment ([React Native CLI Quickstart](https://reactnative.dev/docs/environment-setup))
- iOS: Xcode + CocoaPods
- Android: Android Studio + JDK

### Installation

```bash
# Clone the repository
git clone https://github.com/mehmetakifdurann/IlacAsistanim.git
cd IlacAsistanim

# Install backend dependencies
cd backend
npm install

# Install mobile dependencies
cd ../mobile
npm install

# iOS only
cd ios && pod install && cd ..
```

### Environment Variables

Create a `.env` file in the backend directory:

```env
DATABASE_URL=postgresql://user:password@localhost:5432/ilacasistanim
REDIS_URL=redis://localhost:6379
JWT_SECRET=your_jwt_secret
STRIPE_SECRET_KEY=your_stripe_secret
FCM_SERVER_KEY=your_fcm_key
APNS_KEY_ID=your_apns_key_id
```

### Running the App

```bash
# Start the backend
cd backend
npm run dev

# Start the React Native app
cd mobile
npx react-native run-android
# or
npx react-native run-ios
```

---

##  User Roles

| Role | Description |
|---|---|
| **Hasta** (Patient) | Views personal medication schedule, receives reminders, marks doses as taken |
| **Sorumlu** (Caregiver) | Manages medications for assigned patients, monitors adherence, configures reminders |

---

##  License

This project is developed for academic purposes.

---

##  Contributors

- [mehmetakifdurann](https://github.com/mehmetakifdurann)
