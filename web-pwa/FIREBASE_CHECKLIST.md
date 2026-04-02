# Чеклист налаштування Firebase для PWA

## ✅ Крок 1: Перевірте Firebase конфігурацію

1. Відкрийте `firebase-config.js`
2. Переконайтеся, що всі значення заповнені (не має бути `YOUR_...`)
3. Перевірте, що використовується **Compat версія** (не модульний синтаксис)

## ✅ Крок 2: Увімкніть Google Sign-In

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/providers
2. Натисніть на **Google**
3. **Увімкніть** перемикач (Enable)
4. Виберіть **Project support email** (ваш email)
5. Натисніть **Save**

## ✅ Крок 3: Додайте авторизовані домени

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/settings
2. Прокрутіть до розділу **"Authorized domains"**
3. Переконайтеся, що додані:
   - ✅ `localhost` (для локальної розробки)
   - ✅ `sport-tracker-6011e.firebaseapp.com`
   - ✅ Ваш домен (якщо вже розгорнуто)

Якщо `localhost` немає - натисніть **"Add domain"** та додайте `localhost`

## ✅ Крок 4: Налаштуйте Firestore Rules

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/firestore/rules
2. Замініть правила на:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/trainings/{trainingId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. Натисніть **Publish**

## ✅ Крок 5: Перевірте OAuth consent screen

1. Відкрийте: https://console.cloud.google.com/apis/credentials/consent?project=sport-tracker-6011e
2. Переконайтеся, що OAuth consent screen налаштовано
3. Якщо проєкт у **тестовому режимі**, додайте ваш email у **"Test users"**

## ✅ Крок 6: Перевірте код

1. Відкрийте `index.html` - переконайтеся, що підключені Firebase скрипти (Compat версія)
2. Відкрийте `firebase-config.js` - переконайтеся, що використовується `firebase.initializeApp()`
3. Відкрийте `app.js` - переконайтеся, що використовується `firebase.auth()` та `firebase.firestore()`

## 🔍 Якщо все ще не працює:

1. Відкрийте консоль браузера (F12)
2. Перевірте, чи немає помилок завантаження Firebase
3. Спробуйте увійти та подивіться, яка помилка з'являється
4. Надішліть текст помилки з консолі
