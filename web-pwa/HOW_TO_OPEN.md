# Як відкрити index.html у браузері

## Спосіб 1: Просто відкрити файл (найпростіший)

### На macOS:
1. Відкрийте Finder
2. Перейдіть до папки `web-pwa`
3. Знайдіть файл `index.html`
4. **Подвійний клік** на файл
5. Він відкриється у вашому стандартному браузері

### Або:
1. Відкрийте браузер (Chrome, Safari, Firefox)
2. Натисніть `Cmd + O` (або File → Open File)
3. Виберіть файл `web-pwa/index.html`
4. Натисніть "Open"

## Спосіб 2: Через термінал (рекомендовано)

### Відкрити у браузері:
```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
open index.html
```

### Або вказати браузер:
```bash
# Chrome
open -a "Google Chrome" index.html

# Safari
open -a Safari index.html

# Firefox
open -a Firefox index.html
```

## Спосіб 3: Локальний сервер (найкращий для розробки)

### Python (якщо встановлено):
```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
python3 -m http.server 8000
```
Потім відкрийте: http://localhost:8000

### Node.js (якщо встановлено):
```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
npx http-server -p 8000
```
Потім відкрийте: http://localhost:8000

## ⚠️ Важливо!

Якщо ви просто відкриваєте файл (Спосіб 1), деякі функції можуть не працювати через обмеження CORS. 

**Рекомендую використовувати Спосіб 3 (локальний сервер)** для повної функціональності.

## Швидкий старт:

```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
python3 -m http.server 8000
```

Потім відкрийте браузер і перейдіть на: **http://localhost:8000**
