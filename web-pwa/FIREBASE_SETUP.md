# Покрокова інструкція: Налаштування Firebase для PWA

## Крок 1: Відкрийте Firebase Console

1. Перейдіть на https://console.firebase.google.com/
2. Увійдіть у свій Google акаунт
3. Виберіть ваш проєкт **Sport Tracker** (той самий, що використовується для Android додатку)

## Крок 2: Додайте Web app до проєкту

1. У Firebase Console натисніть на **⚙️ (Settings)** → **Project settings**
2. Прокрутіть вниз до розділу **"Your apps"**
3. Натисніть на іконку **`</>` (Web)** або кнопку **"Add app"** → **Web**
4. Введіть назву додатку (наприклад: "Sport Tracker Web")
5. **Не встановлюйте** Firebase Hosting зараз (можна додати пізніше)
6. Натисніть **"Register app"**

## Крок 3: Скопіюйте конфігурацію

Після реєстрації ви побачите код конфігурації, який виглядає так:

```javascript
const firebaseConfig = {
  apiKey: "AIza...",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abcdef"
};
```

## Крок 4: Вставте конфігурацію в firebase-config.js

1. Відкрийте файл `web-pwa/firebase-config.js`
2. Замініть значення в об'єкті `firebaseConfig` на ваші з Firebase Console
3. Збережіть файл

**Приклад:**
```javascript
const firebaseConfig = {
  apiKey: "AIzaSyC...",  // Ваш API ключ
  authDomain: "sport-tracker-xxxxx.firebaseapp.com",  // Ваш домен
  projectId: "sport-tracker-xxxxx",  // Ваш Project ID
  storageBucket: "sport-tracker-xxxxx.appspot.com",  // Ваш Storage Bucket
  messagingSenderId: "123456789012",  // Ваш Sender ID
  appId: "1:123456789012:web:abcdef123456"  // Ваш App ID
};
```

## Крок 5: Налаштуйте Authentication

1. У Firebase Console перейдіть до **Authentication** → **Sign-in method**
2. Увімкніть **Google** як метод входу:
   - Натисніть на **Google**
   - Увімкніть перемикач
   - Виберіть підтримуваний email
   - Натисніть **Save**

## Крок 6: Налаштуйте Firestore Rules

1. У Firebase Console перейдіть до **Firestore Database** → **Rules**
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

## Крок 7: Перевірте налаштування

1. Відкрийте `index.html` у браузері
2. Спробуйте увійти через Google
3. Якщо все працює - Firebase налаштовано правильно! ✅

## Важливо!

- **Не публікуйте** ваш `firebase-config.js` у публічних репозиторіях без обмежень доступу
- API ключ у конфігурації безпечний для використання на клієнті (Firebase має вбудовані обмеження)
- Для продакшену налаштуйте обмеження API ключа в Google Cloud Console
