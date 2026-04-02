# Як створити Cloud Firestore Database

## Крок 1: Створіть базу даних

1. На сторінці Cloud Firestore натисніть кнопку **"Create database"** (жовта кнопка)
2. Виберіть режим:
   - **Production mode** (рекомендовано) - з правилами безпеки
   - **Test mode** - для тестування (менш безпечно)
3. Натисніть **"Next"**

## Крок 2: Виберіть локацію

1. Виберіть локацію для бази даних (найближчу до вас)
   - Для України: `europe-west` або `europe-central`
2. Натисніть **"Enable"**

## Крок 3: Налаштуйте Rules (після створення)

Після створення бази даних:
1. Перейдіть до вкладки **"Rules"**
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

3. Натисніть **"Publish"**

---

**Важливо!** Без створеної бази даних Firestore не працюватиме, і додаток не зможе зберігати дані.
