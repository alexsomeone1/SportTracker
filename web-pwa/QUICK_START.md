# Швидкий старт: Налаштування Firebase

## ⚡ Швидкий спосіб (5 хвилин)

### 1. Додайте Web app у Firebase Console

1. Відкрийте: https://console.firebase.google.com/project/sport-tracker-6011e/settings/general
2. Прокрутіть до "Your apps" → Натисніть `</>` (Web)
3. Назва: "Sport Tracker Web" → Register app
4. **Скопіюйте `appId`** з конфігурації (виглядає як `1:599835124344:web:xxxxx`)

### 2. Оновіть firebase-config.js

Відкрийте `web-pwa/firebase-config.js` та замініть `YOUR_WEB_APP_ID` на ваш `appId` з кроку 1.

### 3. Увімкніть Google Sign-In

1. Firebase Console → Authentication → Sign-in method
2. Натисніть на "Google" → Enable → Save

### 4. Налаштуйте Firestore Rules

1. Firebase Console → Firestore Database → Rules
2. Вставте:
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
3. Publish

### 5. Перевірте

Відкрийте `index.html` у браузері та спробуйте увійти!

---

## 📝 Детальна інструкція

Дивіться `FIREBASE_SETUP.md` для повної інструкції.
