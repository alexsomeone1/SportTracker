# Діагностика помилки входу

## Крок 1: Перевірте консоль браузера

1. Відкрийте `index.html` у браузері
2. Натисніть `F12` або `Cmd+Option+I` (Mac) для відкриття консолі
3. Перейдіть на вкладку **"Console"**
4. Спробуйте увійти через Google
5. **Скопіюйте всі помилки** з консолі та надішліть мені

## Крок 2: Перевірте налаштування Firebase

### ✅ Крок 1: Google Sign-In увімкнено?
- Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/providers
- Перевірте, чи перемикач "Enable" увімкнено для Google

### ✅ Крок 2: localhost додано?
- Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/settings
- Перевірте, чи є `localhost` в списку "Authorized domains"

### ✅ Крок 3: База даних Firestore створена?
- Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/firestore
- Перевірте, чи база даних створена (не має бути кнопки "Create database")

### ✅ Крок 4: Firestore Rules налаштовані?
- Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/firestore/rules
- Перевірте, чи правила опубліковані

## Крок 3: Типові помилки та рішення

### "auth/unauthorized-domain"
**Рішення**: Додайте `localhost` у Authorized domains

### "auth/operation-not-allowed"
**Рішення**: Увімкніть Google Sign-In у Firebase Console

### "auth/popup-blocked"
**Рішення**: Дозвольте спливаючі вікна для localhost

### "auth/popup-closed-by-user"
**Рішення**: Спробуйте увійти знову, не закриваючи вікно

### "Firebase: Error (auth/network-request-failed)"
**Рішення**: Перевірте інтернет-з'єднання

## Крок 4: Перевірте код

Переконайтеся, що:
- ✅ `firebase-config.js` містить правильну конфігурацію
- ✅ `index.html` підключає Firebase скрипти
- ✅ `app.js` правильно використовує Firebase auth

---

**Найважливіше**: Надішліть текст помилки з консолі браузера - це допоможе точно визначити проблему!
