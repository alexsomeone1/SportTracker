# Вирішення проблем: Помилка входу

## Крок 1: Перевірте консоль браузера

1. Відкрийте консоль браузера:
   - **Chrome/Edge**: `F12` або `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
   - **Safari**: `Cmd+Option+C` (Mac)
   - **Firefox**: `F12` або `Cmd+Option+K` (Mac)

2. Спробуйте увійти знову
3. Подивіться, яка помилка з'являється в консолі

## Крок 2: Налаштуйте Google Sign-In у Firebase

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/providers
2. Натисніть на **Google**
3. Увімкніть перемикач **Enable**
4. Виберіть підтримуваний email (ваш email)
5. Натисніть **Save**

## Крок 3: Додайте авторизовані домени

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/settings
2. Прокрутіть до розділу **"Authorized domains"**
3. Переконайтеся, що додані:
   - `localhost` (для локальної розробки)
   - `sport-tracker-6011e.firebaseapp.com`
   - Ваш домен (якщо вже розгорнуто)

## Крок 4: Перевірте OAuth consent screen

1. Відкрийте: https://console.cloud.google.com/apis/credentials/consent?project=sport-tracker-6011e
2. Переконайтеся, що OAuth consent screen налаштовано
3. Додайте ваш email у "Test users" (якщо проєкт у тестовому режимі)

## Типові помилки та рішення:

### "auth/popup-blocked"
- **Рішення**: Дозвольте спливаючі вікна для вашого сайту в налаштуваннях браузера

### "auth/unauthorized-domain"
- **Рішення**: Додайте ваш домен у Firebase Console → Authentication → Settings → Authorized domains

### "auth/popup-closed-by-user"
- **Рішення**: Просто спробуйте увійти знову, не закриваючи вікно

### "auth/operation-not-allowed"
- **Рішення**: Увімкніть Google Sign-In у Firebase Console → Authentication → Sign-in method

## Якщо нічого не допомагає:

1. Перевірте, чи правильно завантажено Firebase скрипти в `index.html`
2. Перевірте, чи правильно ініціалізовано Firebase в `firebase-config.js`
3. Перевірте консоль браузера на наявність помилок JavaScript
