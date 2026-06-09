# InstantMessenger

Уеб приложение за незабавни съобщения в реално време, изградено с React frontend и Spring Boot backend.

## Функционалности

- **Потребителска автентикация** — Регистрация и вход с JWT сесии
- **Канали** — Създаване на публични канали или директни/групови чатове с избрани потребители
- **Съобщения в реално време** — Чат базиран на WebSocket (STOMP) с незабавна доставка
- **Индикатор за писане** — Вижда се кога друг потребител пише
- **Проследяване на присъствие** — Онлайн/офлайн статус на свързаните потребители
- **Прикачени файлове** — Изпращане на файлове чрез AWS S3 (конфигурируемо)
- **Пагинация** — Лениво зареждане на историята на съобщенията

## Технологичен стек

| Слой | Технология |
|---|---|
| Frontend | React 19, TypeScript, Vite |
| Backend | Spring Boot, Java 25 |
| База данни | PostgreSQL (продукция), H2 (разработка) |
| Миграции | Flyway |
| Автентикация | Custom JWT (HMAC-SHA256) + BCrypt |
| Реално време | WebSocket, STOMP |
| Файлово съхранение | AWS S3 |
| API документация | OpenAPI / Swagger UI |

## Структура на проекта

```
InstantMessenger/
├── BackEnd/                  # Spring Boot приложение
│   └── src/
│       └── main/
│           ├── java/org/instantmessenger/backend/
│           │   ├── controller/   # REST и WebSocket контролери
│           │   ├── service/      # Бизнес логика
│           │   ├── repository/   # JDBC достъп до данни
│           │   ├── model/        # Домейн модели
│           │   ├── dto/          # API заявки/отговори
│           │   └── config/       # Spring конфигурация
│           └── resources/
│               └── db/migration/ # Flyway SQL миграции
└── FrontEnd/                 # React приложение
    └── src/
        ├── features/auth/    # Модул за автентикация
        ├── components/       # UI компоненти за чат
        ├── services/         # API и WebSocket клиенти
        └── types/            # TypeScript типове
```

## Начало на работа

### Изисквания

- **JDK 25**
- **Node.js** (v18+) и npm
- **PostgreSQL 12+** (или използвайте H2 dev профила, за да го пропуснете)
- **Maven** (или използвайте `mvnw` wrapper-а)

### 1. Клониране на хранилището

```bash
git clone <repository-url>
cd InstantMessenger
```

### 2. Настройка на backend-а

#### Вариант А — Разработка (H2 база данни, без нужда от PostgreSQL)

```bash
cd BackEnd
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

H2 конзолата ще бъде достъпна на `http://localhost:8080/h2-console`.

#### Вариант Б — Продукция (PostgreSQL)

Създайте базата данни:

```sql
CREATE DATABASE messenger;
```

Обновете `BackEnd/src/main/resources/application.yaml` с вашите данни за база данни и JWT таен ключ:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/messenger
    username: postgres
    password: your_password

app:
  jwt:
    secret: replace-with-a-long-random-secret
    expiration-seconds: 86400
```

След това стартирайте:

```bash
cd BackEnd
mvn spring-boot:run
```

### 3. Настройка на frontend-а

```bash
cd FrontEnd
npm install
npm run dev
```

### 4. Отворете приложението

Отидете на `http://localhost:5173`. Vite dev сървърът пренасочва всички API и WebSocket заявки към backend-а на `http://localhost:8080`.

## Конфигурация

### Backend (`application.yaml`)

| Ключ | Описание | По подразбиране |
|---|---|---|
| `spring.datasource.url` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/messenger` |
| `spring.datasource.username` | Потребител на базата данни | `postgres` |
| `spring.datasource.password` | Парола на базата данни | `root` |
| `app.jwt.secret` | HMAC-SHA256 таен ключ | *(сменете преди продукция)* |
| `app.jwt.expiration-seconds` | Валидност на токена | `86400` (24 ч) |
| `aws.s3.bucket` | Име на S3 bucket | `$AWS_S3_BUCKET` |
| `aws.s3.region` | AWS регион | `$AWS_REGION` |
| `aws.s3.access-key` | AWS access key ID | `$AWS_ACCESS_KEY_ID` |
| `aws.s3.secret-key` | AWS secret access key | `$AWS_SECRET_ACCESS_KEY` |

### AWS S3 (по избор)

Задайте следните променливи на средата, за да активирате прикачването на файлове:

```bash
AWS_S3_BUCKET=your-bucket-name
AWS_REGION=eu-central-1
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
```

## Преглед на API-то

### REST

| Метод | Endpoint | Автентикация | Описание |
|---|---|---|---|
| `POST` | `/api/auth/register` | Не | Регистрация на нов потребител |
| `POST` | `/api/auth/login` | Не | Вход, връща JWT токен |
| `GET` | `/channels` | Да | Списък с каналите на потребителя |
| `POST` | `/channels` | Да | Създаване на нов канал |
| `GET` | `/messages` | Да | Пагинирани съобщения за канал |
| `GET` | `/users` | Да | Търсене на потребители |

Защитените endpoints изискват `Authorization: Bearer <token>`.

Пълен Swagger UI: `http://localhost:8080/swagger-ui.html`

### WebSocket (STOMP)

Свържете се с `/ws` или `/ws-native`.

| Дестинация | Посока | Описание |
|---|---|---|
| `/app/user.connect` | Изпращане | Регистриране на присъствие при свързване |
| `/app/chat.send` | Изпращане | Изпращане на съобщение |
| `/app/chat.edit` | Изпращане | Редактиране на съобщение |
| `/app/chat.delete` | Изпращане | Изтриване на съобщение |
| `/app/chat.typing` | Изпращане | Излъчване на индикатор за писане |
| `/topic/channel/<id>` | Абониране | Получаване на съобщения за канал |
| `/topic/presence` | Абониране | Получаване на обновления за присъствие |

## Миграции на базата данни

Flyway изпълнява миграциите автоматично при стартиране от `BackEnd/src/main/resources/db/migration/`.

| Версия | Описание |
|---|---|
| V1 | Начална схема: users, channels, messages |
| V2 | Индекси и подобрения на таблиците |
| V3 | Добавени `email` и `password_hash` към users |
| V4 | Добавена junction таблица `channel_members` |
| V5 | Премахнато уникалното ограничение на името на канала |
| V6 | Добавени `file_url` и `file_name` към messages |
| V7 | `content` е направено nullable (съобщения само с файл) |

## Разработка

```bash
# Lint на frontend-а
cd FrontEnd && npm run lint

# Build на frontend-а за продукция
cd FrontEnd && npm run build

# Тестове на backend-а
cd BackEnd && mvn test
```
