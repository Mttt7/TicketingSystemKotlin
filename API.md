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
6. [Moduł: Ticket](#6-moduł-ticket)
7. [Moduł: User](#7-moduł-user)
   - [PATCH /api/users/{id}/profile](#patch-apiusersidprofile)
   - [GET /api/agents](#get-apiagents)
8. [Moduł: CommonTable](#8-moduł-commontable)

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
  "firstName": "Jan",
  "lastName": "Kowalski",
  "password": "tajnehaslo123"
}
```

| Pole | Typ | Wymagane | Walidacja |
|---|---|---|---|
| `email` | `String` | ✅ | Poprawny format adresu email |
| `firstName` | `String` | ❌ | Max 80 znaków |
| `lastName` | `String` | ❌ | Max 80 znaków |
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
    "firstName": "Jan",
    "lastName": "Kowalski",
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

## 6. Moduł: Ticket

Endpointy modułu ticketów wymagają JWT (`Authorization: Bearer <token>`).

### GET /api/tickets/stats

Zwraca zagregowane statystyki zgłoszeń: liczba ticketów per status oraz liczba ticketów otwartych i zamkniętych dzisiaj (od północy).

**URL:** `GET /api/tickets/stats`

**Autoryzacja:** ✅ Wymagana

#### Odpowiedź — 200 OK

```json
{
  "byStatus": {
    "ZGLOSZONE": 14,
    "OTWARTE": 7,
    "DO_WERYFIKACJI": 2,
    "ZAMKNIETE": 31
  },
  "openedToday": 3,
  "closedToday": 5
}
```

| Pole | Typ | Opis |
|---|---|---|
| `byStatus` | `Map<String, Long>` | Liczba ticketów dla każdego statusu. Zawsze zwraca wszystkie statusy (0 gdy brak) |
| `openedToday` | `Long` | Liczba **unikalnych** ticketów, których status zmienił się na `OTWARTE` dzisiaj (od 00:00:00) |
| `closedToday` | `Long` | Liczba **unikalnych** ticketów, których status zmienił się na `ZAMKNIETE` dzisiaj (od 00:00:00) |

> `openedToday` / `closedToday` są liczone na podstawie historii zmian statusu (`TicketHistory`), nie na podstawie pola `createdAt`. Ticket może przejść OTWARTE → ZAMKNIETE → OTWARTE w ciągu jednego dnia — liczy się **unikalnie** (raz).

#### Przykład (curl)

```bash
curl http://localhost:8080/api/tickets/stats \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### POST /api/tickets

Tworzy nowe zgłoszenie.

**URL:** `POST /api/tickets`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "title": "Cannot login",
  "description": "User cannot login after password reset",
  "priority": "HIGH",
  "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "assigneeIds": ["7141d7ab-46de-47c4-b3f7-1f5b8b11d598"],
  "category": "AUTH",
  "dueAt": "2026-04-28T10:00:00",
  "comments": [
    {
      "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "content": "Issue started after deploy"
    }
  ]
}
```

| Pole | Typ | Wymagane | Walidacja |
|---|---|---|---|
| `title` | `String` | ✅ | Niepuste |
| `description` | `String` | ✅ | Niepuste |
| `priority` | `String` | ✅ | Jedna z wartości: `LOW`, `MEDIUM`, `HIGH` |
| `authorId` | `String` (UUID) | ✅ | Poprawny format UUID |
| `assigneeIds` | `Array<String>` (UUID) | ❌ | Może być pominięte, `null` lub lista UUID |
| `category` | `String` | ❌ | Maksymalnie 80 znaków |
| `dueAt` | `String` (ISO-8601) | ❌ | Data w formacie ISO-8601 |
| `comments` | `Array<Object>` | ❌ | Może być pominięte, `null` lub lista komentarzy |

#### Odpowiedź — 201 Created

```json
{
  "ticketId": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd"
}
```

| Pole | Typ | Opis |
|---|---|---|
| `ticketId` | `UUID` | ID nowo utworzonego zgłoszenia |

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | Błąd walidacji | `"title: Title is required, dueAt: Due date must be in the future"` |
| `401 Unauthorized` | Brak lub nieprawidłowy token | `"Unauthorized"` |
| `403 Forbidden` | Brak uprawnień do tworzenia zgłoszeń | `"Access denied"` |

#### Przykład (curl)

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "Cannot login",
    "description": "User cannot login after password reset",
    "priority": "HIGH",
    "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
    "assigneeIds": ["7141d7ab-46de-47c4-b3f7-1f5b8b11d598"],
    "category": "AUTH",
    "dueAt": "2026-04-28T10:00:00",
    "comments": [
      {
        "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
        "content": "Issue started after deploy"
      }
    ]
  }'
```

---

### GET /api/tickets/{id}

Pobiera pojedyncze zgłoszenie po ID.

**URL:** `GET /api/tickets/{id}`

**Autoryzacja:** ✅ Wymagana

#### Przykład odpowiedzi — 200 OK

```json
{
  "id": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
  "title": "Cannot login",
  "description": "User cannot login after password reset",
  "priority": "HIGH",
  "status": "ZGLOSZONE",
  "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "author": {
    "id": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
    "firstName": "Jan",
    "lastName": "Kowalski",
    "email": "jan.kowalski@example.com",
    "display": "Jan Kowalski <jan.kowalski@example.com>"
  },
  "assigneeIds": ["7141d7ab-46de-47c4-b3f7-1f5b8b11d598"],
  "category": "AUTH",
  "dueAt": "2026-04-28T10:00:00",
  "createdAt": "2026-04-25T14:00:00",
  "updatedAt": "2026-04-25T14:00:00",
  "comments": [
    {
      "id": "a7a86e79-4cc7-43c5-b86a-c4ea74c2f665",
      "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "content": "Issue started after deploy",
      "createdAt": "2026-04-25T14:00:00",
      "updatedAt": "2026-04-25T14:00:00"
    }
  ]
}
```

| Pole | Typ | Opis |
|---|---|---|
| `id` | `UUID` | ID zgłoszenia |
| `title` | `String` | Tytuł zgłoszenia |
| `description` | `String` | Opis zgłoszenia |
| `priority` | `String` | Priorytet zgłoszenia |
| `status` | `String` | Status zgłoszenia |
| `authorId` | `UUID` | ID autora zgłoszenia |
| `author` | `Object` | Dane autora: `id`, `firstName`, `lastName`, `email`, `display` |
| `assigneeIds` | `Array<UUID>` | ID przypisanych agentów |
| `category` | `String` | Kategoria zgłoszenia |
| `dueAt` | `String` (ISO-8601) | Termin realizacji |
| `createdAt` | `String` (ISO-8601) | Data utworzenia |
| `updatedAt` | `String` (ISO-8601) | Data ostatniej aktualizacji |
| `comments` | `Array<Object>` | Tablica komentarzy do zgłoszenia |

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `401 Unauthorized` | Brak lub nieprawidłowy token | `"Unauthorized"` |
| `403 Forbidden` | Brak uprawnień do przeglądania zgłoszenia | `"Access denied"` |
| `404 Not Found` | Zgłoszenie nie istnieje | `"Ticket not found"` |

#### Przykład (curl)

```bash
curl -X GET http://localhost:8080/api/tickets/0e0bf7f5-3f02-4872-b57f-890b59f5f2fd \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### PATCH /api/tickets/{id}

Edytuje wybrane pola zgłoszenia (partial update). Wysyłaj tylko te pola, które chcesz zmienić.  
Aby wyczyścić `category` lub `dueAt`, ustaw pole na `null` i odpowiednio `categoryExplicit: true` / `dueAtExplicit: true`.

**URL:** `PATCH /api/tickets/{id}`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "title": "Cannot login — updated",
  "description": "New description",
  "priority": "CRITICAL",
  "category": "AUTH",
  "categoryExplicit": false,
  "dueAt": "2026-05-01T12:00:00",
  "dueAtExplicit": false,
  "assigneeIds": [
    "7141d7ab-46de-47c4-b3f7-1f5b8b11d598",
    "99abc123-0000-0000-0000-000000000001"
  ]
}
```

| Pole | Typ | Wymagane | Opis |
|---|---|---|---|
| `actorId` | `UUID` | ✅ | Kto wykonuje edycję |
| `title` | `String` | ❌ | Nowy tytuł (max 200 znaków); pominięcie = bez zmian |
| `description` | `String` | ❌ | Nowy opis (max 4000 znaków); pominięcie = bez zmian |
| `priority` | `String` | ❌ | `VERY_LOW` \| `LOW` \| `MEDIUM` \| `HIGH` \| `CRITICAL` |
| `category` | `String?` | ❌ | Nowa kategoria; użyj z `categoryExplicit: true` by wyczyścić |
| `categoryExplicit` | `Boolean` | ❌ | Domyślnie `false`. Gdy `true` — aplikuje wartość `category` (nawet `null`) |
| `dueAt` | `String?` | ❌ | ISO-8601; użyj z `dueAtExplicit: true` by wyczyścić |
| `dueAtExplicit` | `Boolean` | ❌ | Domyślnie `false`. Gdy `true` — aplikuje wartość `dueAt` (nawet `null`) |
| `assigneeIds` | `Array<UUID>?` | ❌ | Gdy podane — **zastępuje** cały zestaw przypisanych użytkowników (multiselect). Pominięcie = bez zmian |

#### Odpowiedź — 204 No Content

#### Odpowiedzi błędów

| Status | Kiedy |
|---|---|
| `400 Bad Request` | Błąd walidacji |
| `404 Not Found` | Zgłoszenie nie istnieje |

---

### PATCH /api/tickets/{id}/status

Zmienia status zgłoszenia.

**URL:** `PATCH /api/tickets/{id}/status`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "newStatus": "OTWARTE"
}
```

| Pole | Typ | Wartości |
|---|---|---|
| `actorId` | `UUID` | ✅ Wymagane |
| `newStatus` | `String` | `ZGLOSZONE` \| `OTWARTE` \| `DO_WERYFIKACJI` \| `ZAMKNIETE` |

#### Odpowiedź — 204 No Content

---

### PATCH /api/tickets/{id}/priority

Zmienia priorytet zgłoszenia (np. z widoku podglądu).

**URL:** `PATCH /api/tickets/{id}/priority`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "newPriority": "CRITICAL"
}
```

| Pole | Typ | Wartości |
|---|---|---|
| `actorId` | `UUID` | ✅ Wymagane |
| `newPriority` | `String` | `VERY_LOW` \| `LOW` \| `MEDIUM` \| `HIGH` \| `CRITICAL` |

#### Odpowiedź — 204 No Content

---

### POST /api/tickets/{id}/comments

Dodaje komentarz do zgłoszenia.

**URL:** `POST /api/tickets/{id}/comments`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "authorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "content": "Sprawdziłem — błąd pojawia się tylko w Chrome."
}
```

| Pole | Typ | Wymagane | Opis |
|---|---|---|---|
| `authorId` | `UUID` | ✅ | Autor komentarza |
| `actorId` | `UUID` | ✅ | Kto wykonuje akcję (do historii) |
| `content` | `String` | ✅ | Treść komentarza (max 3000 znaków) |

#### Odpowiedź — 201 Created

```json
{
  "commentId": "a7a86e79-4cc7-43c5-b86a-c4ea74c2f665"
}
```

---

### PATCH /api/tickets/{id}/comments/{commentId}

Edytuje treść istniejącego komentarza.

**URL:** `PATCH /api/tickets/{id}/comments/{commentId}`

**Autoryzacja:** ✅ Wymagana

#### Request Body

```json
{
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
  "content": "Zaktualizowana treść komentarza."
}
```

#### Odpowiedź — 204 No Content

---

### DELETE /api/tickets/{id}/comments/{commentId}

Usuwa komentarz ze zgłoszenia.

**URL:** `DELETE /api/tickets/{id}/comments/{commentId}`

**Autoryzacja:** ✅ Wymagana

#### Request Body (opcjonalny)

```json
{
  "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8"
}
```

> Jeśli `actorId` zostanie pominięty, system użyje UUID zerowego (anonymous). Zalecane przekazywanie `actorId`.

#### Odpowiedź — 204 No Content

---

### GET /api/tickets/{id}/history

Pobiera pełną historię cyklu życia zgłoszenia posortowaną chronologicznie.

**URL:** `GET /api/tickets/{id}/history`

**Autoryzacja:** ✅ Wymagana

#### Odpowiedź — 200 OK

```json
{
  "entries": [
    {
      "id": "b1c2d3e4-0000-0000-0000-000000000001",
      "ticketId": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
      "eventType": "TICKET_CREATED",
      "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "fieldName": null,
      "oldValue": null,
      "newValue": null,
      "assigneesAdded": [],
      "assigneesRemoved": [],
      "commentId": null,
      "commentContent": null,
      "occurredAt": "2026-04-26T10:00:00"
    },
    {
      "id": "b1c2d3e4-0000-0000-0000-000000000002",
      "ticketId": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
      "eventType": "STATUS_CHANGED",
      "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "fieldName": "status",
      "oldValue": "ZGLOSZONE",
      "newValue": "OTWARTE",
      "assigneesAdded": [],
      "assigneesRemoved": [],
      "commentId": null,
      "commentContent": null,
      "occurredAt": "2026-04-26T10:05:00"
    },
    {
      "id": "b1c2d3e4-0000-0000-0000-000000000003",
      "ticketId": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
      "eventType": "ASSIGNEES_CHANGED",
      "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "fieldName": null,
      "oldValue": null,
      "newValue": null,
      "assigneesAdded": ["7141d7ab-46de-47c4-b3f7-1f5b8b11d598"],
      "assigneesRemoved": [],
      "commentId": null,
      "commentContent": null,
      "occurredAt": "2026-04-26T10:10:00"
    },
    {
      "id": "b1c2d3e4-0000-0000-0000-000000000004",
      "ticketId": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
      "eventType": "COMMENT_ADDED",
      "actorId": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "fieldName": null,
      "oldValue": null,
      "newValue": null,
      "assigneesAdded": [],
      "assigneesRemoved": [],
      "commentId": "a7a86e79-4cc7-43c5-b86a-c4ea74c2f665",
      "commentContent": "Sprawdziłem — błąd pojawia się tylko w Chrome.",
      "occurredAt": "2026-04-26T10:15:00"
    }
  ]
}
```

#### Typy zdarzeń (`eventType`)

| Wartość | Kiedy pojawia się |
|---|---|
| `TICKET_CREATED` | Zgłoszenie zostało utworzone |
| `TICKET_EDITED` | Edycja pola (jedno zdarzenie na zmienione pole, `fieldName` wskazuje które) |
| `STATUS_CHANGED` | Zmiana statusu |
| `PRIORITY_CHANGED` | Zmiana priorytetu |
| `ASSIGNEES_CHANGED` | Zmiana przypisanych użytkowników (listy `assigneesAdded` / `assigneesRemoved`) |
| `COMMENT_ADDED` | Nowy komentarz |
| `COMMENT_EDITED` | Edycja komentarza (`oldValue` = poprzednia treść, `newValue` = nowa treść) |
| `COMMENT_DELETED` | Usunięcie komentarza (snapshot w `commentContent`) |

---

## 7. Moduł: User

Endpointy zarządzania profilem użytkownika wymagają JWT (`Authorization: Bearer <token>`).

### PATCH /api/users/{id}/profile

Edytuje imię i nazwisko użytkownika. **Email nie może być zmieniany.**

**URL:** `PATCH /api/users/{id}/profile`

**Autoryzacja:** ✅ Wymagana

#### Path Parameters

| Parametr | Typ | Opis |
|---|---|---|
| `id` | `UUID` | ID użytkownika do edycji |

#### Request Body

```json
{
  "firstName": "Jan",
  "lastName": "Kowalski"
}
```

| Pole | Typ | Wymagane | Walidacja |
|---|---|---|---|
| `firstName` | `String` | ✅ | Niepuste, od **2 do 80 znaków** |
| `lastName` | `String` | ✅ | Niepuste, od **2 do 80 znaków** |

#### Odpowiedź — 204 No Content

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | Błąd walidacji | `"firstName: First name must be between 2 and 80 characters"` |
| `401 Unauthorized` | Brak lub nieprawidłowy token | `"Unauthorized"` |
| `404 Not Found` | Użytkownik nie istnieje | `"User not found: <uuid>"` |

#### Przykład (curl)

```bash
curl -X PATCH http://localhost:8080/api/users/8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "firstName": "Jan",
    "lastName": "Kowalski"
  }'
```

---

### GET /api/agents

Zwraca stronicowaną, przeszukiwalną listę użytkowników możliwych do przypisania do zgłoszenia. Używany przez `AssigneeMultiselectComponent`.

**URL:** `GET /api/agents`

**Autoryzacja:** ✅ Wymagana

#### Query Parameters

| Parametr | Typ | Wymagany | Domyślnie | Opis |
|---|---|---|---|---|
| `query` | `String` | ❌ | `""` | Fraza wyszukiwania (case-insensitive) po `firstName`, `lastName`, `email` |
| `page` | `Int` | ❌ | `0` | Numer strony (indeks od zera, ≥ 0) |
| `pageSize` | `Int` | ❌ | `20` | Rozmiar strony (1–50) |
| `includeSelf` | `Boolean` | ❌ | `true` | Gdy `false` — wyklucza aktualnie zalogowanego użytkownika z wyników |

#### Sortowanie wyników

1. Rekordy, w których `firstName`, `lastName` lub `email` **zaczyna się** od `query` — priorytet wyższy
2. Pozostałe dopasowania częściowe
3. W obrębie tej samej grupy: `firstName ASC`, `lastName ASC`, `email ASC`

#### Odpowiedź — 200 OK

```json
{
  "items": [
    {
      "id": "8b8221cd-2d8a-4f58-a0f4-b95af66f8ff8",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "email": "jan.kowalski@example.com",
      "display": "Jan Kowalski <jan.kowalski@example.com>",
      "role": "AGENT"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "pageSize": 20
}
```

| Pole | Typ | Opis |
|---|---|---|
| `items` | `Array<Object>` | Lista agentów na bieżącej stronie |
| `items[].id` | `UUID` | ID użytkownika |
| `items[].firstName` | `String` | Imię |
| `items[].lastName` | `String` | Nazwisko |
| `items[].email` | `String` | Email |
| `items[].display` | `String` | Format: `"Imię Nazwisko <email>"` — wartość do wyświetlenia w multiselect |
| `items[].role` | `String` | `USER` \| `AGENT` \| `ADMIN` |
| `totalElements` | `Long` | Łączna liczba pasujących wyników |
| `totalPages` | `Int` | Łączna liczba stron |
| `page` | `Int` | Bieżąca strona |
| `pageSize` | `Int` | Rozmiar strony |

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | `page < 0`, `pageSize < 1` lub `pageSize > 50` | `"getAgents.pageSize: must be less than or equal to 50"` |
| `401 Unauthorized` | Brak lub nieprawidłowy token | `"Unauthorized"` |

#### Przykłady (curl)

```bash
# Wszyscy — pierwsza strona
curl "http://localhost:8080/api/agents" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Wyszukiwanie po frazie
curl "http://localhost:8080/api/agents?query=jan&page=0&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Wyszukiwanie bez bieżącego użytkownika
curl "http://localhost:8080/api/agents?query=anna&includeSelf=false" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 8. Moduł: CommonTable

Backend zwraca gotową tabelę do renderowania na froncie. Front wysyła tylko `tableKey`, `page`, `pageSize` oraz opcjonalnie sortowanie/filtry.

### POST /api/common-table

**URL:** `POST /api/common-table`

**Autoryzacja:** ✅ Wymagana

#### Request Body (nowy format)

```json
{
  "tableKey": "tickets",
  "page": 0,
  "pageSize": 20,
  "sorts": [
    { "field": "createdAt", "direction": "DESC" },
    { "field": "title", "direction": "ASC" }
  ],
  "filterRules": [
    { "field": "title", "operator": "CONTAINS", "value": "login" },
    { "field": "priority", "operator": "IN", "values": ["HIGH", "CRITICAL"] }
  ],
  "filters": {}
}
```

> `filters`, `sorts`, `filterRules` mogą być pominięte lub ustawione na `null`.

#### Legacy kompatybilność (nadal wspierane)

```json
{
  "tableKey": "tickets",
  "page": 0,
  "pageSize": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC",
  "filters": {
    "title": "login",
    "status": "ZGLOSZONE"
  }
}
```

#### Wspierane operatory `filterRules`

| Operator | Znaczenie |
|---|---|
| `EQ` | równe |
| `CONTAINS` | contains / like (case-insensitive) |
| `IN` | wartość w liście |
| `BETWEEN` | zakres (`from`, `to`) |
| `GTE` | większe/równe (daty) |
| `LTE` | mniejsze/równe (daty) |

#### Response 200 (przykład)

```json
{
  "tableKey": "tickets",
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "columns": [
    {
      "key": "title",
      "displayName": "Title",
      "path": "title",
      "sortable": true,
      "filterable": true
    }
  ],
  "rows": [
    {
      "id": "0e0bf7f5-3f02-4872-b57f-890b59f5f2fd",
      "title": "Cannot login",
      "author": "Jan Kowalski <jan.kowalski@example.com>",
      "priority": "HIGH",
      "status": "ZGLOSZONE"
    }
  ]
}
```

#### Obsługiwane `tableKey`

- `tickets` — wszystkie zgłoszenia (z filtrowaniem po każdej kolumnie, w tym **ID**)
- `my-tickets` — tylko zgłoszenia, do których zalogowany użytkownik jest **przypisany** (`assigneeIds`)

#### Odpowiedzi błędów

| Status | Kiedy | Przykład `message` |
|---|---|---|
| `400 Bad Request` | Niepoprawna walidacja requestu | `"tableKey is required"` |
| `401 Unauthorized` | Brak lub nieprawidłowy token | `"Unauthorized"` |
| `404 Not Found` | Nieznany `tableKey` | `"Table definition not found for key: xyz"` |

---

*Ostatnia aktualizacja: 2026-04-26 — dodano: edycja ticketu, zmiana statusu/priorytetu, zarządzanie przypisanymi, komentarze CRUD, historia cyklu życia, edycja profilu użytkownika, GET /api/agents*
