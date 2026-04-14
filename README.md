# Sport Tracker

Android app for logging workouts, viewing statistics, and syncing data with the cloud after Google sign-in.

---

## English

### Features

- **Google Sign-In** via Firebase Authentication  
- **Workouts** — add entries with sport type and date, browse a list, delete with confirmation  
- **Statistics** — aggregated counts per sport and overview totals  
- **Storage** — Room for offline/local data; **Cloud Firestore** for per-user sync (`users/{uid}/trainings`)  
- **UI** — Jetpack Compose, Material 3, light/dark theme, bottom navigation (list / add / stats), pull-to-refresh  

### Requirements

- **Android Studio** (recent stable; project uses AGP 8.13.x, Kotlin 2.0.x)  
- **JDK 11**  
- Device or emulator with **API 29+** (Android 10; `minSdk = 29`)  

### Setup

1. Clone the repository.  
2. Create a Firebase project and add an Android app with package name `com.example.sporttracker`.  
3. Enable **Authentication → Google** and **Cloud Firestore**.  
4. Download **`google-services.json`** and place it in the `app/` folder (same level as `app/build.gradle.kts`).  
5. In Firebase Console → Project settings → your Android app, add your **SHA-1** signing certificate (debug/release as needed). The app logs the debug SHA-1 tag `SHA-1` on launch to help with this.  
6. Open the project in Android Studio and sync Gradle, then **Run** the `app` configuration.  

### Tech stack

- Kotlin, Jetpack Compose, Material 3  
- Firebase Auth, Firestore  
- Room, Kotlin Coroutines / Flow  
- Google Play services (Sign-In)  

### License

Not specified by the author; add a `LICENSE` file if you need one.

---

## Українською

### Можливості

- **Вхід через Google** (Firebase Authentication)  
- **Тренування** — додавання з видом спорту та датою, список, видалення з підтвердженням  
- **Статистика** — підрахунки за видами спорту та загальні підсумки  
- **Зберігання** — **Room** локально; **Cloud Firestore** для синхронізації даних користувача (`users/{uid}/trainings`)  
- **Інтерфейс** — Jetpack Compose, Material 3, світла/темна тема, нижня навігація (список / додати / статистика), оновлення списку «потягни вниз»  

### Вимоги

- **Android Studio** (актуальна стабільна версія; у проєкті AGP 8.13.x, Kotlin 2.0.x)  
- **JDK 11**  
- Пристрій або емулятор з **API 29+** (Android 10; `minSdk = 29`)  

### Налаштування

1. Клонуйте репозиторій.  
2. Створіть проєкт у Firebase та додайте Android-додаток з ідентифікатором пакета `com.example.sporttracker`.  
3. Увімкніть **Authentication → Google** та **Cloud Firestore**.  
4. Завантажте **`google-services.json`** і покладіть у каталог `app/` (поряд із `app/build.gradle.kts`).  
5. У Firebase Console → налаштування проєкту → ваш Android-додаток додайте **SHA-1** сертифіката підпису (debug/release за потреби). Застосунок виводить debug SHA-1 у лог з тегом `SHA-1` при запуску.  
6. Відкрийте проєкт у Android Studio, синхронізуйте Gradle і запустіть конфігурацію **Run** для модуля `app`.  

### Стек технологій

- Kotlin, Jetpack Compose, Material 3  
- Firebase Auth, Firestore  
- Room, Kotlin Coroutines / Flow  
- Google Play services (Sign-In)  

### Ліцензія

Автором не вказано; за потреби додайте файл `LICENSE`.
