# API.md
> Dokumentacja REST API — Helpdesk Ticketing System
> Aktualizowana przy każdej zmianie endpointów.

---

## Spis treści
1. [Informacje ogólne](#1-informacje-ogólne)
2. [Uwierzytelnianie](#2-uwierzytelnianie)
3. [Format odpowiedzi błędów](#3-format-odpowiedzi-błędów)
4. [Moduł: Auth](#4-moduł-auth)
   - [POST /api/auth/register](#post-apiauthregister)
   - [POST /api/auth/login](#post-apiauthlogin)
5. [Kody statusów HTTP](#5-kody-statusów-http)

---

## 1. Informacje ogólne

| Właściwość | Wartość |
|---|---|
| Base URL (dev) | `http://localhost:8080` |
| Format danych | JSON (`Content-Type: application/json`) |
| Autentykacja | JWT Bearer Token |
| Sesje | Brak — API jest bezstanowe (stateless) |

---

## 2. Uwierzytelnianie

Wszystkie endpointy **poza `/api/auth/**`** wymagają tokenu JWT w nagłówku:

```
Authorization: Bearer <token>
```

### Struktura JWT

Token jest podpisany algorytmem **HS256** i zawiera następujące dane (claims):

| Claim | Typ | Opis |
|---|---|---|
| `sub` | `String` | Email użytkownika |
| `userId` | `String` (UUID) | ID użytkownika w bazie |
| `role` | `String` | Rola: `USER`, `AGENT`, `ADMIN` |
| `iat` | `Long` | Data wystawienia (Unix timestamp) |
| `exp` | `Long` | Data wygaśnięcia (Unix timestamp) |

### Czas życia tokenu

Domyślnie **24 godziny** (`86400` sekund). Konfigurowalny przez `app.jwt.expiration-ms` w `application.yaml`.

Po wygaśnięciu tokenu należy zalogować się ponownie.

---

## 3. Format odpowiedzi błędów

Każda odpowiedź z błędem ma jednolity format:

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Contact support with errorId=4b2ac6a8-0ec9-4de6-95e3-cdbb7f2d4d65",
  "path": "/api/auth/register",
  "timestamp": "2026-04-25T13:40:00",
  "errorId": "4b2ac6a8-0ec9-4de6-95e3-cdbb7f2d4d65"
}
```

| Pole | Typ | Opis |
|---|---|---|
| `status` | `Int` | Kod HTTP |
| `error` | `String` | Nazwa statusu HTTP |
| `message` | `String` | Komunikat biznesowy / techniczny |
| `path` | `String` | Endpoint, który zwrócił błąd |
| `timestamp` | `String` (ISO-8601) | Czas błędu |
| `errorId` | `String?` | ID incydentu (tylko dla 500) do korelacji z logami |

---

## 4. Moduł: Auth

Endpointy publiczne — nie wymagają tokenu JWT.

---

### POST /api/auth/register

Rejestruje nowego użytkownika.

**URL:** `POST /api/auth/register`

**Autoryzacja:** ❌ Nie wymagana

#### Request Body

```json
{
  "email": "jan.kowalski@example.com",
  "password": "tajnehaslo123"
}
```

| Pole | Typ | Wymagane | Walidacja |
|---|---|---|---|
| `email` | `String` | ✅ | Poprawny format adresu email |
| `password` | `String` | ✅ | Min. 8 znaków |

#### Odpowiedź — 201 Created

```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

| Pole | Typ | Opis |
|---|---|---|
| `userId` | `UUID` | ID nowo zarejestrowanego użytkownika |

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | Błąd walidacji | `"email: Email must be valid, password: Password must be at least 8 characters"` |
| `409 Conflict` | Email już istnieje w systemie | `"Email is already in use: jan.kowalski@example.com"` |

#### Przykład (curl)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jan.kowalski@example.com",
    "password": "tajnehaslo123"
  }'
```

---

### POST /api/auth/login

Loguje użytkownika i zwraca token JWT.

**URL:** `POST /api/auth/login`

**Autoryzacja:** ❌ Nie wymagana

#### Request Body

```json
{
  "email": "jan.kowalski@example.com",
  "password": "tajnehaslo123"
}
```

| Pole | Typ | Wymagane | Walidacja |
|---|---|---|---|
| `email` | `String` | ✅ | Poprawny format adresu email |
| `password` | `String` | ✅ | Niepuste |

#### Odpowiedź — 200 OK

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqYW4ua293YWxza2lAZXhhbXBsZS5jb20iLCJ1c2VySWQiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc0NTU3OTYwMCwiZXhwIjoxNzQ1NjY2MDAwfQ.signature",
  "type": "Bearer",
  "expiresIn": 86400
}
```

| Pole | Typ | Opis |
|---|---|---|
| `token` | `String` | Token JWT do użycia w kolejnych żądaniach |
| `type` | `String` | Zawsze `"Bearer"` |
| `expiresIn` | `Long` | Czas życia w sekundach (domyślnie `86400` = 24h) |

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | Błąd walidacji | `"email: Email must be valid"` |
| `401 Unauthorized` | Błędny email lub hasło | `"Invalid email or password"` |

> ⚠️ **Uwaga bezpieczeństwa:** System celowo zwraca ten sam komunikat błędu zarówno dla nieistniejącego emaila, jak i błędnego hasła. Zapobiega to enumeracji kont użytkowników.

#### Przykład (curl)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jan.kowalski@example.com",
    "password": "tajnehaslo123"
  }'
```

#### Użycie tokenu w kolejnym żądaniu

```bash
curl -X GET http://localhost:8080/api/tickets \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 5. Kody statusów HTTP

| Status | Znaczenie |
|---|---|
| `200 OK` | Sukces — zwraca dane |
| `201 Created` | Zasób został utworzony |
| `204 No Content` | Sukces — brak treści w odpowiedzi |
| `400 Bad Request` | Błąd walidacji danych wejściowych |
| `401 Unauthorized` | Brak tokenu, token wygasł lub nieprawidłowe dane logowania |
| `403 Forbidden` | Brak uprawnień do zasobu |
| `404 Not Found` | Zasób nie istnieje |
| `409 Conflict` | Konflikt (np. duplikat emaila) |
| `500 Internal Server Error` | Nieoczekiwany błąd serwera |

---

*Ostatnia aktualizacja: 2026-04-25*
