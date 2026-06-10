# InstantMessenger

Уеб приложение за незабавни съобщения в реално време. Изградено с React 19 (TypeScript) за frontend и Spring Boot 4 (Java 25) за backend, комуникацията е чрез WebSocket/STOMP протокол.

---

## Функционалности

- Регистрация и вход с JWT автентикация
- Директни чатове и групови канали
- Съобщения в реално време чрез WebSocket
- Индикатор за писане (typing indicator)
- Онлайн/офлайн статус на потребителите
- Прикачване на файлове чрез AWS S3 *(вижте бележката по-долу)*
- Пагинирано зареждане на история на съобщенията
- Търсене в съобщенията на канал

---

## Бързо стартиране

### Изисквания

| Инструмент | Версия |
|---|---|
| JDK | **25** |
| Maven | 3.9+ (или използвайте включения `mvnw`) |
| Node.js | 18+ |
| PostgreSQL | 12+ |

### 1. База данни

Създайте база данни в PostgreSQL:

```sql
CREATE DATABASE messenger;
```

Стандартната конфигурация очаква потребител `postgres` с парола `root` на `localhost:5432`. При нужда редактирайте `BackEnd/src/main/resources/application.yaml`.

### 2. Backend

```bash
cd BackEnd
./mvnw spring-boot:run
```

При първо стартиране Flyway автоматично създава всички таблици. Backend-ът слуша на `http://localhost:8080`.

### 3. Frontend

```bash
cd FrontEnd
npm install
npm run dev
```

Приложението е достъпно на `http://localhost:5173`. Vite проксира всички API и WebSocket заявки към backend-а.

---

## Бележка за качване на файлове (AWS S3)

Функцията за прикачване на файлове е **реализирана** и работи — файловете се качват директно в AWS S3, a чрез presigned URL-и се визуализират в чата.

> **Важно:** AWS данните за достъп са лични и не са включени в хранилището, затова бутонът за прикачване ще върне грешка на друга машина. Останалата функционалност (съобщения, канали, търсене, присъствие) работи изцяло без S3.

---

## Архитектура

```
InstantMessenger/
├── BackEnd/                        # Spring Boot приложение
│   └── src/main/
│       ├── java/.../backend/
│       │   ├── Controller/         # REST и WebSocket контролери
│       │   ├── service/            # Бизнес логика
│       │   ├── Repository/         # JDBC достъп до данни (NamedParameterJdbcTemplate)
│       │   ├── Model/              # Домейн модели (Java records)
│       │   ├── DTO/                # API заявки и отговори
│       │   └── config/             # Security, WebSocket, CORS конфигурация
│       └── resources/db/migration/ # Flyway SQL миграции (V1–V8)
└── FrontEnd/                       # React приложение
    └── src/
        ├── features/auth/          # Автентикация (контекст, форми, API)
        ├── components/             # UI компоненти
        ├── services/               # REST и WebSocket клиенти
        └── types/                  # TypeScript типове
```

### Как работи реалното време

Клиентът се свързва към `/ws-native` като основен WebSocket и говори по STOMP протокол (без допълнителна библиотека). При свързване изпраща JWT токена в `Authorization` header-а на `CONNECT` frame-а. Backend-ът валидира токена и съхранява `userId` в сесията. Всеки канал има собствен topic `/topic/channel/{id}` — съобщения, редакции и typing индикатори пристигат там. Присъствието се разпраща по `/topic/presence`.

### Автентикация

JWT токенът се генерира при вход/регистрация (HMAC-SHA256, 24 часа валидност). Всяка REST заявка и WebSocket конекция го изисква в `Authorization: Bearer <token>` header.

---

## API

### REST endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/api/auth/register` | Регистрация |
| `POST` | `/api/auth/login` | Вход, връща JWT |
| `GET` | `/channels` | Каналите на потребителя |
| `POST` | `/channels` | Създаване на канал |
| `GET` | `/messages?channelId=&limit=&before=` | Пагинирани съобщения |
| `GET` | `/users?query=` | Търсене на потребители |
| `GET` | `/search?query=&channelId=` | Търсене в съобщения |
| `POST` | `/files/presigned-url` | Генериране на S3 presigned URL |

Всички endpoints с изключение на `/api/auth/**` изискват валиден JWT токен.

Swagger UI: `http://localhost:8080/swagger-ui.html`

### WebSocket (STOMP)

| Дестинация | Посока | Описание |
|---|---|---|
| `/app/user.connect` | изпращане | Регистрира присъствие при свързване |
| `/app/chat.send` | изпращане | Изпраща съобщение |
| `/app/chat.typing` | изпращане | Излъчва typing индикатор |
| `/topic/channel/{id}` | абониране | Съобщения и events за канал |
| `/topic/user/{id}` | абониране | Уведомления за нови канали |
| `/topic/presence` | абониране | Онлайн/офлайн статус |

---

## Тестове

```bash
cd BackEnd
./mvnw test
```

Покритите компоненти: `MessageService`, `MessagingService`, `PresenceService`, `ChatController`, `WebSocketEventListener`, `UserRepository`.

---

## Технологичен стек

| Слой | Технология |
|---|---|
| Frontend | React 19, TypeScript, Vite 8 |
| Backend | Spring Boot 4, Java 25 |
| База данни | PostgreSQL + Flyway |
| Автентикация | Custom JWT (JJWT) + BCrypt |
| Реално време | WebSocket, STOMP |
| Файлово съхранение | AWS S3 (presigned URLs) |
| API документация | SpringDoc OpenAPI 3 / Swagger UI |
