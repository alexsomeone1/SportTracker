# Sport Tracker - PWA (Progressive Web App)

## Поточна ситуація

Compose for Web має відому проблему з iOS targets на macOS, тому веб-модуль тимчасово виключено зі збірки.

## Рекомендований підхід для PWA

Для створення PWA версії додатку, яку можна встановити на iPhone, рекомендую створити **простий HTML/JS проєкт** з Firebase.

### Переваги:
- ✅ Стабільна робота без проблем з iOS targets
- ✅ Швидка розробка
- ✅ Легка інтеграція з Firebase
- ✅ Повна підтримка PWA
- ✅ Працює на всіх пристроях (iPhone, Android, Desktop)

### Структура проєкту:

```
web-pwa/
├── index.html          # Головна сторінка
├── manifest.json       # PWA маніфест
├── service-worker.js   # Service Worker для офлайн-режиму
├── firebase-config.js  # Конфігурація Firebase
├── app.js              # Основна логіка додатку
├── styles.css          # Стилі
└── icons/              # Іконки для PWA
```

### Наступні кроки:

1. **Створити простий HTML/JS проєкт** з Firebase
2. **Налаштувати PWA** (manifest.json, service-worker.js)
3. **Інтегрувати Firebase** для автентифікації та зберігання даних
4. **Розгорнути** на хостингу (Firebase Hosting, Vercel, Netlify)

### Встановлення на iPhone:

1. Відкрити Safari на iPhone
2. Перейти на URL веб-додатку
3. Натиснути "Поділитися" → "На екран «Домівка»"
4. Додаток з'явиться на головному екрані

---

**Примітка:** Якщо потрібно, можу створити базовий HTML/JS проєкт з Firebase для PWA.
