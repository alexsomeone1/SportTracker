# Sport Tracker - PWA (Progressive Web App)

Це веб-версія додатку Sport Tracker, яку можна встановити на iPhone як PWA.

## Налаштування

### 1. Встановіть необхідні залежності

Додайте до `gradle/libs.versions.toml`:
```toml
compose-web = "1.5.10"
```

### 2. Оновіть `web/build.gradle.kts`

Переконайтеся, що використовується правильна версія Compose for Web.

### 3. Налаштуйте Firebase для веб

1. У Firebase Console додайте Web app до вашого проєкту
2. Скопіюйте конфігурацію Firebase (JavaScript SDK)
3. Додайте Firebase JS SDK до `index.html`

### 4. Запустіть веб-версію

```bash
./gradlew :web:jsBrowserDevelopmentRun
```

Веб-додаток буде доступний за адресою: `http://localhost:8080`

### 5. Встановлення на iPhone

1. Відкрийте Safari на iPhone
2. Перейдіть на URL вашого веб-додатку
3. Натисніть кнопку "Поділитися" (Share)
4. Виберіть "На екран «Домівка»" (Add to Home Screen)
5. Додаток з'явиться на головному екрані як звичайний додаток

## Наступні кроки

Для повноцінної роботи потрібно:
- Адаптувати UI під веб (Compose for Web має інші компоненти)
- Налаштувати Firebase для веб (JavaScript SDK)
- Замінити Room на IndexedDB або localStorage для локального зберігання
- Адаптувати авторизацію під веб (Google Sign-In для веб)

## Структура файлів

- `index.html` - головна HTML сторінка
- `manifest.json` - маніфест PWA
- `service-worker.js` - Service Worker для офлайн-режиму
- `Main.kt` - точка входу веб-додатку
