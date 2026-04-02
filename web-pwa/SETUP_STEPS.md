# Покрокова інструкція: Налаштування Firebase для PWA

## ✅ Крок 1: Увімкніть Google Sign-In у Firebase Console

1. **Відкрийте**: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/providers
2. **Натисніть** на **"Google"**
3. **Увімкніть** перемикач (Enable)
4. **Виберіть** Project support email (ваш email)
5. **Натисніть** "Save"

**Важливо!** Без цього кроку вхід через Google не працюватиме.

---

## ✅ Крок 2: Додайте localhost у авторизовані домени

1. **Відкрийте**: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/settings
2. **Прокрутіть** до розділу **"Authorized domains"**
3. **Перевірте**, чи є `localhost` в списку
4. **Якщо немає** - натисніть **"Add domain"** та додайте `localhost`
5. **Натисніть** "Add"

**Важливо!** Без цього кроку Firebase не дозволить вхід з localhost.

---

## ✅ Крок 3: Налаштуйте Firestore Rules

1. **Відкрийте**: https://console.firebase.google.com/project/sport-tracker-6011e/firestore/rules
2. **Замініть** правила на:

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

3. **Натисніть** "Publish"

**Важливо!** Без цього кроку дані не зберігатимуться.

---

## ✅ Крок 4: Перевірте OAuth consent screen (якщо потрібно)

1. **Відкрийте**: https://console.cloud.google.com/apis/credentials/consent?project=sport-tracker-6011e
2. **Перевірте**, чи OAuth consent screen налаштовано
3. **Якщо проєкт у тестовому режимі**, додайте ваш email у **"Test users"**

---

## ✅ Крок 5: Перевірте код

### Перевірте `firebase-config.js`:
- ✅ Всі значення заповнені (не має бути `YOUR_...`)
- ✅ Використовується `firebase.initializeApp(firebaseConfig)`

### Перевірте `index.html`:
- ✅ Підключені Firebase скрипти (Compat версія)
- ✅ `firebase-config.js` завантажується перед `app.js`

### Перевірте `app.js`:
- ✅ Використовується `firebase.auth()` та `firebase.firestore()`

---

## 🔍 Перевірка після налаштування

1. **Відкрийте** `index.html` у браузері
2. **Відкрийте** консоль браузера (F12)
3. **Спробуйте** увійти через Google
4. **Подивіться**, чи є помилки в консолі

---

## ❌ Типові помилки:

### "auth/unauthorized-domain"
→ **Рішення**: Додайте `localhost` у Authorized domains (Крок 2)

### "auth/operation-not-allowed"
→ **Рішення**: Увімкніть Google Sign-In (Крок 1)

### "Missing or insufficient permissions"
→ **Рішення**: Налаштуйте Firestore Rules (Крок 3)

---

**Після виконання всіх кроків спробуйте увійти знову!**
