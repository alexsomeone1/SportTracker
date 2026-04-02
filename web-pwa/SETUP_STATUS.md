# Статус налаштування Firebase

## ✅ Крок 1: Google Sign-In - ВИКОНАНО
- ✅ Enable перемикач увімкнено
- ✅ Support email налаштовано: sanya.com372@gmail.com
- ✅ Web client ID налаштовано: 599835124344-rmfqsu02co1teecugm9f0i7v71scejmc.apps.googleusercontent.com

## ⏳ Крок 2: Авторизовані домени - ПЕРЕВІРТЕ
Перевірте: https://console.firebase.google.com/project/sport-tracker-6011e/authentication/settings

Потрібно переконатися, що в списку "Authorized domains" є:
- ✅ `localhost` (для локальної розробки)
- ✅ `sport-tracker-6011e.firebaseapp.com`

## ⏳ Крок 3: Firestore Rules - ПЕРЕВІРТЕ
Перевірте: https://console.firebase.google.com/project/sport-tracker-6011e/firestore/rules

Потрібно налаштувати правила для доступу до даних.

## Наступні кроки:
1. Перевірте Крок 2 (Authorized domains)
2. Перевірте Крок 3 (Firestore Rules)
3. Спробуйте увійти знову
