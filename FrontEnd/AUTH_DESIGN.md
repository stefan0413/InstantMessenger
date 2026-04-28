# Authentication Module Design

## Overview
This module provides authentication functionality (Login / Register / Logout) for the InstantMessenger frontend.

The design is built to be **backend-agnostic**, allowing easy integration with a real server when available.

---

## Architecture

The authentication flow follows a layered structure:
UI (LoginForm / RegisterForm)
↓
AuthContext (state management)
↓
authApi (API abstraction layer)
↓
Mock Service OR Backend


---

## Components

### 1. UI Layer
- `LoginForm.tsx`
- `RegisterForm.tsx`
- `UserStatus.tsx`

Responsibilities:
- Collect user input
- Trigger auth actions
- Display auth state

---

### 2. State Management
- `AuthContext.tsx`

Responsibilities:
- Store user and token
- Provide login, register, logout methods
- Persist session (localStorage)

---

### 3. API Layer
- `authApi.ts`

Responsibilities:
- Handle all communication with backend
- Abstract data source (mock vs real API)

---

### 4. Mock Service
- `mockAuthService.ts`

Used for development before backend is ready.

---

## Data Flow Example

Login flow:

User submits form
→ LoginForm calls login()
→ AuthContext calls loginRequest()
→ authApi calls mockLogin()
→ mock returns user + token
→ AuthContext updates state
→ UI re-renders


---

## Persistence

Authentication state is stored in:

- `localStorage.user`
- `localStorage.token`

On app load:
- Context restores session automatically

---

## Backend Integration

To connect to a real backend:

1. Update `authApi.ts`
2. Set:
```ts
const USE_MOCK = false;

3. Ensure backend endpoints:

POST /api/auth/login
POST /api/auth/register

Expected response:

{
  "user": {
    "id": 1,
    "username": "string",
    "email": "string"
  },
  "token": "jwt-token"
}

Design Principles
Separation of concerns
Feature-based structure
Scalable architecture
Replaceable data layer

Future Improvements
JWT validation
Refresh tokens
Protected routes
Role-based access