# Як запустити PWA

## На Mac (для розробки та тестування):

### 1. Відкрийте Термінал на Mac:

**Спосіб 1:**
- Натисніть `Cmd + Space` (Spotlight)
- Введіть "Terminal"
- Натисніть Enter

**Спосіб 2:**
- Відкрийте Finder
- Перейдіть до Applications → Utilities → Terminal

### 2. Запустіть локальний сервер:

```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
python3 -m http.server 8000
```

### 3. Відкрийте в браузері на Mac:
- http://localhost:8000

---

## На iPhone (для тестування):

### Варіант 1: Через локальну мережу (якщо iPhone і Mac в одній Wi-Fi мережі)

1. **На Mac:**
   - Запустіть сервер (як вище)
   - Дізнайтеся IP адресу Mac:
     ```bash
     ifconfig | grep "inet " | grep -v 127.0.0.1
     ```
   - Ви побачите щось на кшталт: `inet 192.168.1.100`

2. **На iPhone:**
   - Відкрийте Safari
   - Перейдіть на: `http://192.168.1.100:8000` (замініть на ваш IP)

### Варіант 2: Розгорнути на хостингу (рекомендовано для продакшену)

1. **Firebase Hosting:**
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init hosting
   firebase deploy
   ```

2. **Vercel:**
   - Завантажте папку `web-pwa` на vercel.com

3. **Netlify:**
   - Перетягніть папку `web-pwa` на netlify.com

Після розгортання отримаєте URL, який можна відкрити на iPhone.

---

## Швидкий старт для Mac:

1. Відкрийте Terminal (Cmd + Space → "Terminal")
2. Скопіюйте та вставте:
   ```bash
   cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa && python3 -m http.server 8000
   ```
3. Відкрийте браузер: http://localhost:8000

---

**Примітка**: Для тестування на iPhone найкраще розгорнути на хостингу (Firebase Hosting, Vercel або Netlify).
