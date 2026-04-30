# InstantMessenger - подробна документация на промените

Документът описва развитието на проекта InstantMessenger от началното състояние на repository-то до текущото състояние след последните промени по frontend, backend, authentication, локално стартиране и debugging. 

Дата на документа: 30 април 2026 г.

Основен branch по време на работата: `feature/auth-live-backend`

Последен потвърден commit преди текущите незакомитнати auth/backend промени: `d01749a Polish auth screens`

---

## Съдържание

1. Контекст и начално състояние
2. История на repository-то
3. Frontend конфликтите
4. Обединяване на auth и chat UI
5. Vite/React структура
6. Auth UI polish
7. Login форма
8. Register форма
9. Auth context
10. Mock auth преди backend интеграцията
11. Преминаване към реален backend auth
12. Backend структура преди auth
13. Database миграции
14. User модел
15. Auth DTO layer
16. AuthController
17. AuthService
18. Password hashing
19. UserRepository
20. Token стратегия
21. JwtService
22. AuthInterceptor
23. WebMvcConfig
24. CORS и proxy
25. Vite config проблемът
26. Dev H2 database
27. PostgreSQL configuration
28. Стартиране на backend
29. Стартиране на frontend
30. Тестване на register
31. Тестване на login
32. LocalStorage behavior
33. Protected endpoints
34. Channels и messages
35. MessageRepository fix
36. WebSocket контекст
37. Frontend chat shell
38. Chat components
39. Mock chat data
40. Styling и visual language
41. Проверки и команди
42. Debugging timeline
43. Known issues
44. Security limitations
45. Какво е production-ready и какво не
46. Git status и незакомитнати промени
47. Препоръчани следващи commits
48. Как да demo-неш проекта
49. Речник на важните файлове
50. Заключение и roadmap

<div style="page-break-after: always;"></div>

## Страница 1 - Контекст и начално състояние

Проектът InstantMessenger е web messenger приложение с отделни `FrontEnd` и `BackEnd` директории. Frontend частта е React приложение, което в текущото си състояние работи през Vite. Backend частта е Spring Boot приложение с JDBC repository слой, Flyway миграции, PostgreSQL конфигурация и допълнителен dev profile за H2 база.

В началото на разглежданата работа repository-то беше в състояние след merge конфликт. Имаше комбинация от две посоки на развитие: auth frontend, който съдържаше `LoginForm`, `RegisterForm`, `AuthContext` и mock auth service, и messenger/chat frontend, който съдържаше channel list, chat window, message bubbles, group modal и mock channel data. Тези две посоки бяха попаднали в конфликт, най-видимо в `FrontEnd/src/App.tsx`, `FrontEnd/package-lock.json`, `FrontEnd/package.json`, `FrontEnd/index.html`, `FrontEnd/src/main.tsx` и config файловете.

Основната цел на първите промени беше приложението отново да се build-ва и да има единна логика: потребителят вижда login/register екран, когато не е authenticated, и messenger UI след успешна автентикация. След това целта се разшири към реална backend защита: register, login, password hashing, token, protected endpoints и локална dev база за тестване без PostgreSQL.

Важно е да се отбележи, че текущият документ описва както вече commit-натите промени, така и последните незакомитнати промени по backend auth и dev H2 конфигурацията. Той не е само changelog, а пълна техническа документация на посоката, архитектурата, причините и начина на стартиране.

<div style="page-break-after: always;"></div>

## Страница 2 - История на repository-то

Git историята показва няколко основни етапа. Началните commits включват setup на проекта, Flyway и database setup, repository layer, DTO/mapper layer, websocket infrastructure и core functionality. След това има frontend auth branch, merge conflict resolution и polish на auth screens.

Последните релевантни commits са:

```text
d01749a Polish auth screens
da5e892 Resolve frontend merge conflicts
ff5ef92 Merge pull request #6 from stefan0413/IM-35-core-functionallity
492de1f Merge pull request #5 from stefan0413/IM-34-websocket-infrastructure
775d9b6 Add logging
580889f Implement some repository changes + websocket setup + broadcasting
a8b7cb0 Initial setup
723b6f1 Merge pull request #2 from stefan0413/IM-29-database-setup
6c6f80d Merge pull request #3 from stefan0413/IM-30-repository-layer
36837e9 Merge pull request #4 from stefan0413/IM-31-DTO-MAPPER-LAYER
63617bb feat: add auth module with login/register, context, API layer and documentation
```


Документираните промени по-долу трябва да се четат като надграждане върху тази история.

<div style="page-break-after: always;"></div>

## Страница 3 - Frontend конфликтите



Решението беше:

- да се премахнат всички conflict markers;
- да се запази auth gate логиката;
- да се запази chat shell логиката;
- да се покаже login/register само когато потребителят не е authenticated;
- да се покаже chat interface след successful authentication;
- да се регенерира `package-lock.json` от resolved `package.json`;
- да се маркират файловете като resolved чрез `git add`.

След това `git ls-files -u` беше празен, което означава, че няма unmerged files. Това беше ключов milestone, защото преди него repository-то не беше в работно състояние.

<div style="page-break-after: always;"></div>

## Страница 4 - Обединяване на auth и chat UI

В обединения `App.tsx` бяха комбинирани два различни потока. Auth потокът използва `useAuth`, `LoginForm`, `RegisterForm` и `UserStatus`. Chat потокът използва `ChannelList`, `ChatWindow`, `NewGroupModal`, `mockUsers`, `getChannels`, `Channel` и `Message`.

Крайната логика е:

1. App зарежда channels чрез `getChannels()`.
2. App пази `activeChannelId`, `isGroupModalOpen`, `searchQuery` и `loading`.
3. Ако `isAuthenticated` е false, се показва auth screen.
4. Ако `isAuthenticated` е true и `loading` е true, се показва loading view.
5. Ако `isAuthenticated` е true и channels са заредени, се показва messenger shell.

Това решение е важно, защото поставя authentication като entry point към приложението. Дори chat data все още да е mock във frontend-а, UI потокът вече е реалистичен: user не влиза директно в chat, а първо минава през login/register.

Също така беше добавена логика за създаване на нова група в UI чрез `handleCreateGroup`, както и логика за изпращане на mock съобщение чрез `handleSendMessage`. Тези функции работят client-side върху `channels` state и не са още вързани към backend messages API.

<div style="page-break-after: always;"></div>

## Страница 5 - Vite/React структура

Frontend проектът е Vite/React setup. В `package.json` има scripts:

```json
{
  "dev": "vite",
  "build": "tsc -b && vite build",
  "lint": "eslint .",
  "preview": "vite preview"
}
```

Важен Windows-specific детайл е, че PowerShell блокира директното изпълнение на `npm.ps1` заради execution policy. Затова всички команди се изпълняват с `npm.cmd`, например:

```powershell
npm.cmd run dev
npm.cmd run build
npm.cmd run lint
```

Frontend използва React 19 dependencies и Vite 8. TypeScript config-ът включва `verbatimModuleSyntax`, което наложи type-only imports за типове като `Channel`, `Message`, `User` и `FormEvent`. Това беше поправено с `import type`.

След resolving на конфликтите и type-only import поправките, frontend build мина успешно. Това потвърди, че обединяването на `App.tsx`, component imports и Vite config не е счупило TypeScript слоя.

<div style="page-break-after: always;"></div>

## Страница 6 - Auth UI polish

След като приложението започна да build-ва, auth screen-ът беше визуално подобрен. Първоначалните `LoginForm` и `RegisterForm` бяха базови HTML форми без класове, без визуална интеграция с messenger UI и с browser-like усещане.

Целта беше auth screen-ът да съвпадне с visual language на chat UI:

- бели панели;
- меки borders;
- indigo accent цвят;
- компактни input fields;
- soft shadows;
- rounded controls;
- conversational preview вместо празен background;
- toggle между Login и Register;
- inline error messages;
- submit loading text.

В `App.css` беше добавен auth layout с класове като:

```css
.auth-shell
.auth-panel
.auth-preview
.auth-card
.auth-toggle
.auth-form
.auth-form__field
.auth-form__submit
.user-status
```

Това даде на login/register screen-а по-завършен вид и го направи част от същия продукт, а не отделна упражнителна форма.

<div style="page-break-after: always;"></div>

## Страница 7 - Login форма

`LoginForm.tsx` беше променен от проста форма с default email/password към по-структурирана форма. Първоначално login формата имаше hardcoded стойности:

```ts
test@test.com
123456
```

След преминаване към реален backend auth тези стойности вече не бяха подходящи, защото backend validation изисква парола минимум 8 символа. Затова default стойностите бяха изчистени.

Добавени са:

- `isSubmitting` state;
- basic frontend validation за email;
- basic frontend validation за password length;
- trim на email преди submit;
- inline error handling;
- показване на реалното error message от API слоя;
- disabled submit state по време на заявка.

Формата вече не използва `alert("Login successful")`. Това беше премахнато, защото successful login трябва да промени app state и да покаже messenger UI, не да прекъсва user flow с popup.

Текущата login форма е UX слой върху реалния backend login endpoint. Тя не прави security сама по себе си; security се случва в backend-а чрез password hash проверка и token generation.

<div style="page-break-after: always;"></div>

## Страница 8 - Register форма

`RegisterForm.tsx` беше променен по подобен начин. Първоначално формата приемаше username, email и password и при грешка показваше generic alert. След промените тя има structured class names, inline errors и по-добри frontend validations.

Frontend checks:

- username поне 3 символа;
- email да съдържа `@`;
- password поне 8 символа;
- trim на username и email преди submit;
- loading state при submit.

Важно е да се разбере, че тези проверки не са достатъчни за security. Те са UX слой, който пази потребителя от очевидни грешки преди заявката. Backend-ът пак валидира същите неща чрез Jakarta Validation annotations:

```java
@Size(min = 3, max = 50)
@Email
@Size(min = 8, max = 128)
```

Това е правилният модел: frontend помага, backend гарантира.

Register формата вече показва error message от API слоя. Ако backend върне duplicate или validation error, frontend трябва да покаже по-точен текст вместо старото generic `Registration failed. Please try again.`

<div style="page-break-after: always;"></div>

## Страница 9 - Auth context

`AuthContext.tsx` е централното място във frontend-а за auth state. Той държи:

```ts
user
token
isAuthenticated
login()
register()
logout()
```

Преди поправките register сетваше `user` и `token` само в React state, но не ги записваше в `localStorage`. Това означаваше, че след register потребителят е authenticated само до refresh. Това беше поправено и register вече записва:

```ts
localStorage.setItem("user", JSON.stringify(data.user));
localStorage.setItem("token", data.token);
```

Auth state се възстановява от `localStorage` чрез lazy initial state:

```ts
useState(() => localStorage.getItem(...))
```

Това беше направено вместо `useEffect` със synchronous `setState`, защото lint правилото на React hooks предупреди, че директен setState в effect може да доведе до cascading renders.

`isAuthenticated` е изчислено като:

```ts
Boolean(user && token)
```

Това е frontend gating, не security guarantee. Истинската защита е backend interceptor-ът.

<div style="page-break-after: always;"></div>

## Страница 10 - Mock auth преди backend интеграцията

Преди backend интеграцията auth flow използваше `mockAuthService.ts`. Там имаше един mock user:

```ts
email: "test@test.com"
password: "123456"
username: "stefan"
```

Mock login проверяваше exact match и връщаше:

```ts
token: "mock-jwt-token"
```

Mock register приемаше всякакви данни и връщаше fake user с `Date.now()` id. Това беше достатъчно за frontend UI testing, но не беше реална защита. Нямаше:

- database;
- password hashing;
- duplicate email check;
- duplicate username check;
- real token;
- protected backend endpoints.

След backend интеграцията `authApi.ts` беше променен:

```ts
const USE_MOCK = false;
```

Mock service остава в проекта като fallback/legacy/dev tool, но активният поток вече е backend-based. Това означава, че за да работят login/register, backend server трябва да е пуснат и reachable през Vite proxy.

<div style="page-break-after: always;"></div>

## Страница 11 - Преминаване към реален backend auth

Преминаването към backend auth включи промяна на `authApi.ts`. Вместо mock functions, `loginRequest` и `registerRequest` вече правят HTTP requests:

```ts
POST /api/auth/login
POST /api/auth/register
```

Заявките се изпращат с:

```ts
Content-Type: application/json
```

и body:

```json
{
  "email": "...",
  "password": "..."
}
```

или:

```json
{
  "username": "...",
  "email": "...",
  "password": "..."
}
```

Добавена беше helper функция `authFetch`, която централизира fetch логиката и превежда network/backend грешките в user-facing error messages.

Първоначално frontend показваше `Authentication failed`, защото Vite proxy не беше активен. След debug се установи, че Vite зарежда `vite.config.js`, а proxy настройката е била само в `vite.config.ts`. Това беше поправено чрез добавяне на proxy и в `vite.config.js`.

<div style="page-break-after: always;"></div>

## Страница 12 - Backend структура преди auth

Backend-ът вече имаше:

- `BackEndApplication.java`;
- `ChannelController`;
- `MessageController`;
- `ChatController`;
- `ChannelRepository`;
- `MessageRepository`;
- row mappers;
- `MessageService`;
- `MessagingService`;
- `WebSocketConfig`;
- Flyway migrations `V1` и `V2`;
- PostgreSQL datasource config.

Съществуващата database схема имаше `users` таблица само с:

```sql
id SERIAL PRIMARY KEY
username VARCHAR(50) NOT NULL UNIQUE
```

Това не беше достатъчно за login/register. За реален auth са нужни поне email и password hash. Затова беше добавена нова migration `V3__add_user_auth_fields.sql`.

Backend-ът следваше JDBC style, не JPA. Затова новият auth layer също беше направен с `JdbcTemplate`, за да се впише в съществуващата архитектура.

<div style="page-break-after: always;"></div>

## Страница 13 - Database миграции

Съществуващите migration файлове:

```text
V1__create_tables.sql
V2__improve_messages_table.sql
```

бяха допълнени с:

```text
V3__add_user_auth_fields.sql
```

Новата migration добавя:

- `email`;
- `password_hash`;
- unique constraint върху email;
- placeholder update за вече съществуващи users.

Първоначално migration-ът използваше PostgreSQL syntax с няколко операции в един `ALTER TABLE`. Това работи в PostgreSQL, но H2 не го прие. За да работи dev H2 profile, SQL statements бяха разделени:

```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);
```

Тази промяна направи migration-а по-съвместим между PostgreSQL и H2.

<div style="page-break-after: always;"></div>

## Страница 14 - User модел

Добавен беше нов backend model:

```java
public record User(long id, String username, String email, String passwordHash) {
}
```

Това е internal model, който включва `passwordHash`. Той не трябва директно да се връща към frontend-а, защото hash-ът не е user-facing data. Затова беше добавен отделен DTO `UserView`, който съдържа само:

```java
id
username
email
```

Това разделение е важно. Дори password hash да не е plain password, той не трябва да излиза от backend-а. Правилната практика е sensitive fields да не присъстват в response DTO-та.

User model-ът е record, което означава immutable data carrier. Това пасва на текущия backend стил, където `Message` и `Channel` също са records.

<div style="page-break-after: always;"></div>

## Страница 15 - Auth DTO layer

Добавени бяха DTO класове:

```text
LoginRequest.java
RegisterRequest.java
AuthResponse.java
UserView.java
```

`LoginRequest` съдържа:

```java
@Email @NotBlank String email
@NotBlank String password
```

`RegisterRequest` съдържа:

```java
@Size(min = 3, max = 50) String username
@Email String email
@Size(min = 8, max = 128) String password
```

`AuthResponse` връща:

```java
UserView user
String token
```

Това DTO разделение прави auth API contract ясен:

- frontend изпраща credentials;
- backend валидира и обработва;
- backend връща sanitized user view и token;
- password никога не се връща обратно.

Добавянето на validation dependency в `pom.xml` беше нужно, защото се използват Jakarta Validation annotations.

<div style="page-break-after: always;"></div>

## Страница 16 - AuthController

Добавен беше `AuthController`:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController
```

Той дефинира два endpoint-а:

```text
POST /api/auth/register
POST /api/auth/login
```

Controller-ът не съдържа business logic. Той приема request DTO, валидира го чрез `@Valid` и делегира към `AuthService`.

Това е правилно разделение:

- Controller: HTTP contract;
- Service: business logic;
- Repository: database access.

При register controller-ът връща `AuthResponse`. Това позволява след успешна регистрация frontend-ът веднага да счита потребителя за logged in, без да изисква отделен login call.

При login controller-ът също връща `AuthResponse`, ако credentials са валидни.

<div style="page-break-after: always;"></div>

## Страница 17 - AuthService

`AuthService` съдържа основната auth логика:

- normalizes email чрез trim и lowercase;
- проверява дали username вече съществува;
- проверява дали email вече съществува;
- хешира password чрез `PasswordEncoder`;
- създава user през `UserRepository`;
- генерира token чрез `JwtService`;
- валидира login password чрез BCrypt matches.

При duplicate username или email service-ът хвърля:

```java
ResponseStatusException(HttpStatus.CONFLICT, ...)
```

При invalid login хвърля:

```java
ResponseStatusException(HttpStatus.UNAUTHORIZED, ...)
```

Това означава, че frontend може да различи conflict, unauthorized и validation грешки. На практика това беше важно при debugging, защото generic `Authentication failed` не даваше достатъчно информация.

<div style="page-break-after: always;"></div>

## Страница 18 - Password hashing

Паролите вече не трябва да се пазят като plain text. Добавен беше `spring-security-crypto` dependency и `AuthConfig`:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

При register:

```java
passwordEncoder.encode(request.password())
```

При login:

```java
passwordEncoder.matches(request.password(), user.passwordHash())
```

Това е фундаментален security слой. Ако database бъде прочетена, атакуващият не вижда реалните пароли, а BCrypt hashes. BCrypt е подходящ за password hashing, защото е intentionally slow и включва salt.

Важно: frontend validation за password length не е security. Истинската защита е backend hashing + backend validation + database storage на hash, не на password.

<div style="page-break-after: always;"></div>

## Страница 19 - UserRepository

Добавен беше `UserRepository`, който използва `JdbcTemplate`. Той има методи:

```java
findByEmail(String email)
findById(long id)
existsByUsername(String username)
existsByEmail(String email)
create(String username, String email, String passwordHash)
```

Repository-то използва case-insensitive checks чрез:

```sql
LOWER(email) = LOWER(?)
LOWER(username) = LOWER(?)
```

Това е важно, защото `Test@Example.com` и `test@example.com` логически трябва да са един и същ email за login/register.

`create` използва `GeneratedKeyHolder`, за да вземе database-generated user id. Това е consistent със стила на проекта, който използва JDBC вместо JPA entities.

<div style="page-break-after: always;"></div>

## Страница 20 - Token стратегия

Добавен беше token layer, който връща token при successful login/register. Token-ът се записва във frontend `localStorage` и служи за protected requests към backend.

Текущият token е JWT-like HMAC signed token, имплементиран ръчно в `JwtService`, а не чрез външна JWT library. Той има:

- header;
- payload;
- signature.

Payload съдържа:

```json
{
  "sub": "userId",
  "email": "...",
  "exp": timestamp
}
```

Token-ът се подписва с HMAC SHA-256 secret от config:

```yaml
app.jwt.secret
```

Текущият dev secret е само за local development. За production трябва да е дълъг random secret, подаден през environment variable или secret manager, не commit-нат в repository.

<div style="page-break-after: always;"></div>

## Страница 21 - JwtService

`JwtService` има две основни функции:

```java
createToken(long userId, String email)
validateAndGetUserId(String token)
```

При създаване на token:

1. Прави header JSON.
2. Прави payload JSON с `sub`, `email`, `exp`.
3. Base64URL encode-ва header и payload.
4. Подписва `header.payload` с HMAC SHA-256.
5. Връща `header.payload.signature`.

При validation:

1. Разделя token-а по `.`.
2. Проверява дали има 3 части.
3. Пресмята signature наново.
4. Сравнява signature-ите constant-time style.
5. Проверява `exp`.
6. Връща user id от `sub`.

Това е работещ dev-level token approach. За production е препоръчително да се използва поддържана JWT библиотека, защото parsing/claims/algorithms/error handling са чувствителни към edge cases.

<div style="page-break-after: always;"></div>

## Страница 22 - AuthInterceptor

`AuthInterceptor` пази backend endpoint-ите. Той е Spring MVC interceptor, който проверява `Authorization` header:

```text
Authorization: Bearer <token>
```

Ако header липсва или не започва с `Bearer `, backend връща:

```text
401 Unauthorized
```

Ако token-ът е невалиден, изтекъл или user id вече не съществува, също връща `401`.

При валиден token interceptor-ът записва:

```java
request.setAttribute("authenticatedUserId", userId)
```

Това може да се използва по-късно от controllers/services, за да се гарантира, че user не изпраща message от чуждо име. В текущото състояние това attribute още не е fully integrated в message send flow.

<div style="page-break-after: always;"></div>

## Страница 23 - WebMvcConfig

`WebMvcConfig` регистрира interceptor-а и CORS mapping. Protected paths:

```java
"/channels/**"
"/messages/**"
```

Auth endpoints `/api/auth/**` не са protected, защото login/register трябва да са достъпни преди token.

CORS е позволен за:

```text
http://localhost:5173
```

Това позволява frontend dev server да говори с backend, ако заявките са директни към `localhost:8080`. В нашия setup обаче frontend използва Vite proxy, така че browser-ът вижда same-origin requests към `localhost:5173/api/...`. Въпреки това CORS mapping остава полезен.

`WebMvcConfig` е мястото, където в бъдеще могат да се добавят още protected routes, например `/users/**`, `/profile/**`, `/friends/**`.

<div style="page-break-after: always;"></div>

## Страница 24 - CORS и proxy

Frontend-ът изпраща auth заявки към relative paths:

```text
/api/auth/login
/api/auth/register
```

Това е правилно за Vite dev setup, защото Vite може да proxy-ва `/api` към backend:

```js
server: {
  proxy: {
    "/api": "http://localhost:8080",
    "/channels": "http://localhost:8080",
    "/messages": "http://localhost:8080"
  }
}
```

Така browser-ът не прави cross-origin request към `8080`; той говори с `5173`, а Vite server forward-ва към backend. Това намалява CORS friction по време на development.

Проблемът беше, че proxy настройката беше добавена в `vite.config.ts`, но активният config беше `vite.config.js`. След добавяне на същия proxy в `vite.config.js`, login през:

```text
http://localhost:5173/api/auth/login
```

започна да връща user + token.

<div style="page-break-after: always;"></div>

## Страница 25 - Vite config проблемът

Един от най-важните debugging моменти беше “Authentication failed” при login, въпреки че backend login работеше директно. Директният request към:

```text
http://localhost:8080/api/auth/login
```

връщаше token. Но request към:

```text
http://localhost:5173/api/auth/login
```

връщаше `404 Not Found`.

Това доказа, че frontend dev server не proxy-ва `/api` към backend. Проверка на files показа:

```text
FrontEnd/vite.config.js
FrontEnd/vite.config.ts
```

Vite зареждаше `.js` файла, в който липсваше proxy. Поправката беше да се добави proxy config и в `vite.config.js`.

След рестарт на frontend dev server-а, request през `5173/api/auth/login` започна да връща successful response. Това реши UI login проблема.

<div style="page-break-after: always;"></div>

## Страница 26 - Dev H2 database

Понеже на локалната машина нямаше Docker, PostgreSQL service или `psql`, беше добавена dev H2 database конфигурация. Това позволява backend-ът да се стартира и тества без локален PostgreSQL.

Добавен файл:

```text
BackEnd/src/main/resources/application-dev.yaml
```

Съдържа:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/messenger-dev;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
```

Добавена dependency:

```xml
<groupId>com.h2database</groupId>
<artifactId>h2</artifactId>
```

Dev базата се записва в:

```text
BackEnd/data/messenger-dev.mv.db
```

Този файл е runtime artifact и не трябва да се commit-ва. Препоръчително е да се добави `BackEnd/data/` в `.gitignore`, ако още не е.

<div style="page-break-after: always;"></div>

## Страница 27 - PostgreSQL configuration

Основният `application.yaml` все още сочи към PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/messenger
    username: postgres
    password: root
```

Това е нормалният non-dev profile. При него backend изисква:

- PostgreSQL да слуша на `localhost:5432`;
- database `messenger` да съществува;
- user `postgres` с password `root`;
- Flyway да може да изпълни миграциите.

При debugging беше установено:

```text
localhost:5432 -> false
```

Тоест PostgreSQL не беше пуснат и backend падаше при startup с:

```text
Connection to localhost:5432 refused
```

Затова беше добавен H2 dev profile. За production или real local Postgres testing може да се използва основният profile, но за бърз frontend/backend auth test dev profile е по-удобен.

<div style="page-break-after: always;"></div>

## Страница 28 - Стартиране на backend

Backend трябва да се стартира с JDK 25, защото проектът е настроен с:

```xml
<java.version>25</java.version>
```

и използва Java records. На машината default `java` сочеше към Java 8:

```text
java version "1.8.0_401"
```

Затова backend командите трябва да set-нат `JAVA_HOME`:

```powershell
cd BackEnd
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
C:\Users\kaloy\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

За dev H2 database трябва да се използва:

```text
-Dspring-boot.run.profiles=dev
```

Когато backend е пуснат успешно:

```text
http://localhost:8080
```

трябва да отговаря, а `Test-NetConnection localhost -Port 8080` трябва да показва `True`.

<div style="page-break-after: always;"></div>

## Страница 29 - Стартиране на frontend

Frontend се стартира от `FrontEnd` директорията:

```powershell
cd FrontEnd
npm.cmd run dev
```

Важно е да се използва `npm.cmd`, не `npm`, защото PowerShell execution policy може да блокира `npm.ps1`.

Frontend dev URL:

```text
http://localhost:5173
```

При тестове трябва да се отваря точно този URL, защото localStorage е origin-specific. Ако потребителят отвори `127.0.0.1:5173`, това е различен origin от `localhost:5173`, с различен localStorage.

След промяна на Vite config трябва да се рестартира dev server-ът. Vite не винаги reload-ва server config коректно без restart.

При проблеми с кеширане:

```text
Ctrl + F5
```

изчиства browser cache за текущата страница.

<div style="page-break-after: always;"></div>

## Страница 30 - Тестване на register

Register endpoint:

```text
POST http://localhost:8080/api/auth/register
```

Примерен body:

```json
{
  "username": "ivan123",
  "email": "ivan123@example.com",
  "password": "password123"
}
```

Successful response:

```json
{
  "user": {
    "id": 1,
    "username": "ivan123",
    "email": "ivan123@example.com"
  },
  "token": "..."
}
```

Register може да fail-не ако:

- username е под 3 символа;
- email не е валиден;
- password е под 8 символа;
- username вече съществува;
- email вече съществува.

В dev H2 базата вече бяха създадени няколко тестови потребители по време на debugging. Затова за нов register трябва да се използва нов уникален username и email.

<div style="page-break-after: always;"></div>

## Страница 31 - Тестване на login

Login endpoint:

```text
POST http://localhost:8080/api/auth/login
```

Примерен body:

```json
{
  "email": "test1777552349@example.com",
  "password": "password123"
}
```

Потвърдени валидни dev credentials:

```text
Email: test1777552349@example.com
Password: password123
```

```text
Email: stefan@example.com
Password: password123
```

```text
Email: stefan1777552487@example.com
Password: password123
```

Login беше тестван както директно към backend (`8080`), така и през frontend proxy (`5173/api/auth/login`). И двата пътя върнаха user + token след поправката на `vite.config.js`.

Ако UI показва authentication error, първо трябва да се провери:

```text
http://localhost:5173/api/auth/login
```

дали минава през proxy.

<div style="page-break-after: always;"></div>

## Страница 32 - LocalStorage behavior

Frontend записва auth state в `localStorage`:

```text
user
token
```

При login и register:

```ts
localStorage.setItem("user", JSON.stringify(data.user));
localStorage.setItem("token", data.token);
```

При logout:

```ts
localStorage.removeItem("user");
localStorage.removeItem("token");
```

При app load `AuthContext` се опитва да възстанови user/token от localStorage. Ако там има стар или невалиден token, frontend може да счита user-а за authenticated, докато backend отказва protected requests.

При странни auth проблеми е полезно да се изчисти:

DevTools -> Application -> Local Storage -> `http://localhost:5173` -> delete `user` и `token`.

След това page refresh.

<div style="page-break-after: always;"></div>

## Страница 33 - Protected endpoints

Backend protected endpoints в момента са:

```text
/channels/**
/messages/**
```

Те изискват:

```text
Authorization: Bearer <token>
```

Ако token липсва или е невалиден, backend връща `401 Unauthorized`.

Auth endpoints не са protected:

```text
/api/auth/login
/api/auth/register
```

Това е правилно, защото потребителят още няма token преди login/register.

Важна следваща стъпка е frontend services за channels/messages да започнат да изпращат token header. В момента chat UI все още използва mock data, така че protected API integration не е напълно завършена. Security layer-ът е подготвен, но трябва да се върже към реалните chat API calls.

<div style="page-break-after: always;"></div>

## Страница 34 - Channels и messages

Съществуващи backend controllers:

```text
ChannelController
MessageController
ChatController
```

`ChannelController` има:

```text
GET /channels
```

`MessageController` има:

```text
GET /messages?channelId=...
POST /messages
```

След `AuthInterceptor`, тези endpoints вече изискват token. Това е правилно за реален messenger, защото channels и messages не трябва да са публично достъпни.

Все още има архитектурен gap: `MessageRequest` съдържа `userId`, което позволява client да каже кой user изпраща съобщението. По-сигурният вариант е backend да вземе user id от token-а, а не от body-то. Това е важно future improvement.

<div style="page-break-after: always;"></div>

## Страница 35 - MessageRepository fix

В `MessageRepository.save` имаше bug. Първоначално insert statement имаше три placeholders:

```sql
INSERT INTO messages (content, user_id, channel_id) VALUES (?, ?, ?)
```

но параметрите бяха подадени грешно и `keyholder` беше поставен като трети argument вместо channel id. Това би счупило message save.

Поправката използва `PreparedStatement` с `Statement.RETURN_GENERATED_KEYS`, сетва content, userId и channelId и после връща generated key.

Това е важно не само за auth, а и за бъдеща реална chat интеграция. Ако frontend започне да праща real messages към backend, repository save трябва да работи коректно.

<div style="page-break-after: always;"></div>

## Страница 36 - WebSocket контекст

Проектът вече има websocket infrastructure чрез:

```text
WebSocketConfig
ChatController
MessageService
MessagingService
```

`ChatController` слуша STOMP message:

```java
@MessageMapping("/chat.send")
```

и делегира към `MessageService`. Това подсказва посоката на проекта: real-time messaging.

Auth промените не интегрират напълно websocket authentication. Това е отделна security тема. HTTP endpoints вече имат interceptor, но WebSocket/STOMP authentication изисква отделна стратегия: token при connect, handshake interceptor или STOMP channel interceptor.

За production messenger това е задължително, защото иначе user може да bypass-не HTTP protected routes чрез websocket path.

<div style="page-break-after: always;"></div>

## Страница 37 - Frontend chat shell

Chat shell-ът се намира в `App.tsx`. Той съдържа:

- `ChannelList`;
- `ChatWindow`;
- `NewGroupModal`;
- channels state;
- active channel state;
- group creation state;
- search query;
- loading state.

След successful authentication user вижда `UserStatus` и chat shell. Chat shell-ът е responsive чрез CSS media queries.

В момента channels са frontend mock data, зареждани чрез:

```ts
getChannels()
```

от `FrontEnd/src/services/channelsService.ts`. Тази функция връща `mockChannels` след timeout. Това е добро за UI prototyping, но следващата архитектурна стъпка е да се замени с real backend fetch към `/channels` с Authorization header.

<div style="page-break-after: always;"></div>

## Страница 38 - Chat components

Добавени и използвани frontend components:

```text
ChannelItem
ChannelList
ChatWindow
MessageBubble
NewGroupModal
UserPicker
```

Тези components оформят messenger experience:

- channel list със search;
- active channel;
- message list;
- composer input;
- group creation modal;
- participant picker.

Type-only imports бяха поправени, защото TypeScript config изисква:

```ts
import type { User } from "../../types/user";
```

вместо runtime import за типове.

Това намалява generated JS imports и удовлетворява `verbatimModuleSyntax`.

<div style="page-break-after: always;"></div>

## Страница 39 - Mock chat data

Frontend все още има:

```text
mockChannels.ts
mockUsers.ts
```

Те позволяват UI-то да изглежда пълноценно без реален backend channel/message integration. Това беше полезно по време на frontend development и visual polish.

Mock users включват `currentUserId` и sample users с avatar URLs. Mock channels съдържат direct и group conversations, last messages, timestamps и embedded messages.

След auth backend интеграцията тези mock данни все още са валидни за UI demo, но не са source of truth. Реалният source of truth трябва да стане backend database.

Следваща стъпка:

- `/channels` да връща channels за текущия authenticated user;
- `/messages?channelId=` да връща messages;
- send message да използва backend вместо локален state update.

<div style="page-break-after: always;"></div>

## Страница 40 - Styling и visual language

Frontend styling е централен в `App.css` и component CSS files. Основният visual language:

- светъл background `#f5f6fa`;
- бели cards/panels;
- border `#e5e7eb`;
- indigo action color `#4f46e5`;
- muted gray text;
- rounded elements;
- compact UI.

Auth screen-ът беше преработен, за да използва същата система. Това включва:

- `auth-panel`;
- dark preview panel;
- conversation bubbles;
- segmented toggle;
- styled input fields;
- inline error panel;
- submit button style;
- responsive mobile layout.

`UserStatus` също беше променен от plain text в top pill с avatar initial, username/email и logout button.

<div style="page-break-after: always;"></div>

## Страница 41 - Проверки и команди

Frontend проверките, които минаха:

```powershell
npm.cmd run lint
npm.cmd run build
```

Backend compile мина с JDK 25 и Maven:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn.cmd -q -DskipTests compile
```

Backend startup с dev profile беше потвърден след H2 migration fixes:

```text
localhost:8080 -> True
```

Frontend startup:

```text
localhost:5173 -> True
```

Login през proxy:

```text
http://localhost:5173/api/auth/login -> user + token
```

Това потвърди, че frontend proxy и backend auth endpoint работят заедно.

<div style="page-break-after: always;"></div>

## Страница 42 - Debugging timeline

Основните debugging моменти:

1. Build fail заради merge conflict markers.
2. PowerShell блокира `npm`; workaround `npm.cmd`.
3. TypeScript поиска `import type`.
4. Vite build имаше sandbox spawn EPERM; rerun с elevated permission.
5. Backend не стартираше заради PostgreSQL connection refused.
6. Default Java беше Java 8, но проектът иска Java 25.
7. Добавен H2 dev profile.
8. H2 не прие multi-operation `ALTER TABLE`; migration беше split-ната.
9. Failed H2 migration state наложи изтриване на dev DB file.
10. Backend започна да работи на `8080`.
11. Frontend login пак fail-ваше.
12. Директният backend login работеше.
13. Proxy login през `5173` връщаше 404.
14. Открито беше, че Vite зарежда `vite.config.js`, не `vite.config.ts`.
15. Добавен proxy в `vite.config.js`.
16. Login през UI path започна да връща token.

<div style="page-break-after: always;"></div>

## Страница 43 - Known issues

Текущи известни ограничения:

- Chat data все още е mock във frontend-а.
- Protected `/channels` и `/messages` още не са свързани с frontend services.
- WebSocket authentication не е имплементиран.
- JWT implementation е custom и dev-level.
- Dev H2 database file е untracked runtime artifact.
- `application.yaml` съдържа dev-like secrets и credentials.
- `vite.config.js` и `vite.config.ts` са дублирани; това може да доведе до бъдещи обърквания.
- Error messages от backend все още могат да са прекалено generic, защото Spring default error body не винаги включва reason по начина, по който frontend очаква.

Препоръчително е да се избере един Vite config файл и другият да се премахне или да се държи generated/ignored. Най-чисто е да остане `vite.config.ts`, ако проектът е TypeScript-first, но трябва да се уверим, че Vite зарежда правилния файл.

<div style="page-break-after: always;"></div>

## Страница 44 - Security limitations

Добавената защита е добра dev основа, но не е production-ready в пълния смисъл.

Ограничения:

- token implementation е custom;
- няма refresh tokens;
- няма token revocation;
- няма rate limiting;
- няма account lockout;
- няма email verification;
- няма password reset flow;
- няма CSRF strategy, ако някога се мине към cookies;
- няма HTTPS enforcement;
- няма secret management;
- няма audit logging за auth events;
- няма ownership checks върху channels/messages.

Най-важният оставащ security gap е, че `MessageRequest` приема `userId` от client. Backend трябва да използва authenticated user id от token-а, а не да вярва на body-то.

За production трябва да се използва official JWT library или Spring Security resource server setup.

<div style="page-break-after: always;"></div>

## Страница 45 - Какво е production-ready и какво не

Production-ready или почти готово:

- basic frontend auth flow;
- backend register/login endpoints;
- BCrypt password hashing;
- DB-level unique email;
- DTO separation без password hash в response;
- protected HTTP routes чрез token;
- dev H2 profile за local testing;
- Vite proxy setup.

Не е production-ready:

- custom JWT parser/signature code;
- secret в YAML;
- липса на refresh/revocation;
- липса на websocket auth;
- mock chat data;
- липса на real user-channel relation;
- липса на full integration tests;
- липса на Docker/dev environment standardization;
- duplicate Vite config files.

Текущото състояние е отлично за учебен/прототипен milestone: вече има реална auth линия и може да се тества login/register без PostgreSQL. Но преди deployment трябва да се направи hardening.

<div style="page-break-after: always;"></div>

## Страница 46 - Git status и незакомитнати промени

Към момента на документацията има незакомитнати промени в:

```text
BackEnd/pom.xml
BackEnd/src/main/java/.../MessageRepository.java
BackEnd/src/main/resources/application.yaml
FrontEnd/src/features/auth/api/authApi.ts
FrontEnd/src/features/auth/components/LoginForm.tsx
FrontEnd/src/features/auth/components/RegisterForm.tsx
FrontEnd/src/features/auth/context/AuthContext.tsx
FrontEnd/vite.config.js
FrontEnd/vite.config.ts
```

И нови файлове:

```text
AuthController.java
AuthResponse.java
LoginRequest.java
RegisterRequest.java
UserView.java
User.java
UserRepository.java
AuthConfig.java
AuthInterceptor.java
WebMvcConfig.java
AuthService.java
JwtService.java
application-dev.yaml
V3__add_user_auth_fields.sql
```

Също има runtime artifact:

```text
BackEnd/data/
```

Той трябва да се игнорира, не commit-ва.

<div style="page-break-after: always;"></div>

## Страница 47 - Препоръчани следващи commits

Препоръчително разделение на commits:

1. Backend auth foundation
   - DTOs;
   - model;
   - repository;
   - service;
   - controller;
   - password hashing;
   - token service.

2. Backend protection and dev database
   - AuthInterceptor;
   - WebMvcConfig;
   - H2 dependency;
   - application-dev.yaml;
   - V3 migration.

3. Frontend backend auth integration
   - `authApi.ts`;
   - register localStorage fix;
   - validation messages;
   - Vite proxy configs.

4. Documentation
   - този файл.

5. Cleanup
   - ignore `BackEnd/data/`;
   - resolve duplicate Vite config strategy;
   - remove or document generated `vite.config.js` if needed.

Това разделение прави code review по-лесен и намалява риска голям commit да смеси различни теми.

<div style="page-break-after: always;"></div>

## Страница 48 - Как да demo-неш проекта

За demo:

1. Стартирай backend:

```powershell
cd BackEnd
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
C:\Users\kaloy\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

2. Стартирай frontend:

```powershell
cd FrontEnd
npm.cmd run dev
```

3. Отвори:

```text
http://localhost:5173
```

4. Login с:

```text
Email: test1777552349@example.com
Password: password123
```

5. След login трябва да се покаже messenger UI.

6. За register използвай уникален username/email и password поне 8 символа.

<div style="page-break-after: always;"></div>

## Страница 49 - Речник на важните файлове

Frontend:

```text
FrontEnd/src/App.tsx
FrontEnd/src/App.css
FrontEnd/src/features/auth/api/authApi.ts
FrontEnd/src/features/auth/context/AuthContext.tsx
FrontEnd/src/features/auth/components/LoginForm.tsx
FrontEnd/src/features/auth/components/RegisterForm.tsx
FrontEnd/vite.config.js
FrontEnd/vite.config.ts
```

Backend:

```text
BackEnd/src/main/java/org/instantmessenger/backend/Controller/AuthController.java
BackEnd/src/main/java/org/instantmessenger/backend/service/AuthService.java
BackEnd/src/main/java/org/instantmessenger/backend/service/JwtService.java
BackEnd/src/main/java/org/instantmessenger/backend/Repository/UserRepository.java
BackEnd/src/main/java/org/instantmessenger/backend/config/AuthInterceptor.java
BackEnd/src/main/java/org/instantmessenger/backend/config/WebMvcConfig.java
BackEnd/src/main/resources/application-dev.yaml
BackEnd/src/main/resources/db/migration/V3__add_user_auth_fields.sql
```

Runtime/dev:

```text
BackEnd/data/messenger-dev.mv.db
BackEnd/logs/
FrontEnd/logs/
```

Runtime files не трябва да се commit-ват.

<div style="page-break-after: always;"></div>

## Страница 50 - Заключение и roadmap

Проектът премина от merge-conflicted frontend и mock auth към работеща dev auth архитектура с backend register/login, BCrypt password hashing, token generation, protected HTTP endpoints и локален H2 profile за тестване без PostgreSQL.

Най-важните постигнати резултати:

- frontend конфликтите са решени;
- auth UI е визуално подобрен;
- register/login вече говорят с backend;
- backend има реален auth слой;
- password-и се хешират;
- token се връща и пази;
- protected endpoints са подготвени;
- Vite proxy проблемът е открит и поправен;
- проектът може да се стартира локално за demo.

Следващ roadmap:

1. Да се commit-нат текущите auth/backend промени.
2. Да се добави `.gitignore` за `BackEnd/data/` и logs.
3. Да се премахне config duplication между `vite.config.js` и `vite.config.ts`.
4. Да се върже frontend chat към backend `/channels` и `/messages`.
5. Да се използва token при protected requests.
6. Да се направи websocket auth.
7. Да се замени custom JWT с battle-tested библиотека.
8. Да се добавят integration tests.
9. Да се добави Docker Compose за Postgres.
10. Да се документира production setup.

Текущото състояние е добър работещ milestone за тестване и развитие.

