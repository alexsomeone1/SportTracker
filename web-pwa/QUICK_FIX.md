# Швидке виправлення: Помилка "operation-not-supported-in-this-environment"

## Проблема:
Ви відкриваєте `index.html` напряму через `file://` протокол. Firebase не працює з `file://` - потрібен локальний сервер.

## Рішення: Запустіть локальний сервер

### Варіант 1: Python (найпростіший)

1. Відкрийте термінал
2. Виконайте:
```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
python3 -m http.server 8000
```

3. Відкрийте браузер і перейдіть на:
   **http://localhost:8000**

### Варіант 2: Node.js

```bash
cd /Users/alex/AndroidStudioProjects/SportTracker/web-pwa
npx http-server -p 8000
```

Потім відкрийте: **http://localhost:8000**

### Варіант 3: VS Code Live Server

Якщо у вас встановлено VS Code:
1. Встановіть розширення "Live Server"
2. Клікніть правою кнопкою на `index.html`
3. Виберіть "Open with Live Server"

## Після запуску сервера:

1. Відкрийте **http://localhost:8000** у браузері
2. Спробуйте увійти через Google
3. Все має працювати! ✅

---

**Важливо**: Не відкривайте файл напряму через `file://` - завжди використовуйте локальний сервер!
