# Sport Tracker - PWA (Progressive Web App)

Веб-версія додатку Sport Tracker, яку можна встановити на iPhone як PWA.

## Налаштування

### 1. Налаштуйте Firebase

1. Відкрийте `firebase-config.js`
2. Додайте вашу конфігурацію Firebase з Firebase Console:
   - Firebase Console → Project Settings → Your apps → Web app
   - Скопіюйте конфігурацію та вставте в `firebaseConfig`

### 2. Створіть іконки

Створіть папку `icons/` та додайте:
- `icon-192.png` (192x192 пікселів)
- `icon-512.png` (512x512 пікселів)

Можна використати іконку з Android додатку або створити нову.

### 3. Налаштуйте правила Firestore

У Firebase Console → Firestore Database → Rules додайте:

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

### 4. Запустіть локально

Відкрийте `index.html` у браузері або використайте локальний сервер:

```bash
# Python
python -m http.server 8000

# Node.js
npx http-server
```

### 5. Розгорніть на хостингу

**Firebase Hosting:**
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
firebase deploy
```

**Vercel:**
```bash
npm install -g vercel
vercel
```

**Netlify:**
Просто перетягніть папку `web-pwa` на Netlify Drop.

## Встановлення на iPhone

1. Відкрийте Safari на iPhone
2. Перейдіть на URL вашого веб-додатку
3. Натисніть кнопку "Поділитися" (Share) внизу
4. Виберіть "На екран «Домівка»" (Add to Home Screen)
5. Додаток з'явиться на головному екрані як звичайний додаток

## Структура файлів

- `index.html` - головна HTML сторінка
- `manifest.json` - PWA маніфест
- `service-worker.js` - Service Worker для офлайн-режиму
- `firebase-config.js` - конфігурація Firebase
- `app.js` - основна логіка додатку
- `styles.css` - стилі
- `icons/` - іконки для PWA

## Функціонал

- ✅ Авторизація через Google
- ✅ Додавання тренувань
- ✅ Перегляд списку тренувань
- ✅ Видалення тренувань
- ✅ Статистика по видах спорту
- ✅ Офлайн-режим (Service Worker)
- ✅ Встановлення на головний екран (PWA)

## Наступні кроки

- Додати фільтрацію по датах
- Додати фільтрацію по видах спорту
- Покращити UI/UX
- Додати графіки статистики
