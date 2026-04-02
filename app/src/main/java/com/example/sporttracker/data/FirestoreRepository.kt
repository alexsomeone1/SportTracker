package com.example.sporttracker.data

import com.example.sporttracker.db.TrainingEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Репозиторій для роботи з Firestore
 * Зберігає тренування в хмарі під userId користувача
 */
class FirestoreRepository {
    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Отримати колекцію тренувань для поточного користувача
     */
    private fun getUserTrainingsCollection() =
        firestore.collection("users").document(getCurrentUserId())
            .collection("trainings")

    /**
     * Отримати ID поточного користувача
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Користувач не авторизований")
    }

    /**
     * Перевірити, чи користувач авторизований
     */
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    /**
     * Зберегти тренування в Firestore
     */
    suspend fun saveTraining(training: TrainingEntity) {
        if (!isUserSignedIn()) {
            Log.w("FirestoreRepository", "Користувач не авторизований, пропускаємо збереження")
            return
        }

        try {
            val userId = getCurrentUserId()
            Log.d("FirestoreRepository", "Збереження тренування для користувача: $userId")
            Log.d("FirestoreRepository", "Тренування: id=${training.id}, sport=${training.sport}, dateText=${training.dateText}, dateEpochDay=${training.dateEpochDay}")
            
            val data = hashMapOf(
                "id" to training.id,
                "sport" to training.sport,
                "dateEpochDay" to training.dateEpochDay,
                "dateText" to training.dateText
            )

            val collectionPath = "users/$userId/trainings"
            Log.d("FirestoreRepository", "Шлях до колекції: $collectionPath")
            
            getUserTrainingsCollection()
                .document(training.id)
                .set(data)
                .await()
            
            Log.d("FirestoreRepository", "Тренування успішно збережено в Firestore: ${training.id} у колекції $collectionPath")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Помилка збереження тренування", e)
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Отримати всі тренування користувача
     */
    suspend fun getAllTrainings(): List<TrainingEntity> {
        if (!isUserSignedIn()) {
            Log.w("FirestoreRepository", "Користувач не авторизований, повертаємо порожній список")
            return emptyList()
        }

        try {
            val snapshot = getUserTrainingsCollection()
                .get()
                .await()

            val trainings = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        TrainingEntity(
                            id = (data["id"] as? String) ?: doc.id,
                            sport = (data["sport"] as? String) ?: "",
                            dateEpochDay = (data["dateEpochDay"] as? Long) ?: 0L,
                            dateText = (data["dateText"] as? String) ?: ""
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e("FirestoreRepository", "Помилка парсингу документа ${doc.id}", e)
                    null
                }
            }
            
            Log.d("FirestoreRepository", "Отримано ${trainings.size} тренувань з хмари")
            return trainings
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Помилка отримання тренувань", e)
            return emptyList()
        }
    }

    /**
     * Видалити тренування з Firestore
     */
    suspend fun deleteTraining(trainingId: String) {
        if (!isUserSignedIn()) {
            Log.w("FirestoreRepository", "Користувач не авторизований, пропускаємо видалення")
            return
        }

        try {
            getUserTrainingsCollection()
                .document(trainingId)
                .delete()
                .await()
            
            Log.d("FirestoreRepository", "Тренування видалено: $trainingId")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Помилка видалення тренування", e)
            // Не кидаємо виняток, щоб не порушити роботу додатку
        }
    }

    /**
     * Слухати зміни в Firestore в реальному часі
     * Повертає Flow зі списком тренувань, який оновлюється автоматично
     */
    fun observeTrainings(): Flow<List<TrainingEntity>> = callbackFlow {
        if (!isUserSignedIn()) {
            Log.w("FirestoreRepository", "Користувач не авторизований, повертаємо порожній Flow")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val userId = getCurrentUserId()
        val collectionPath = "users/$userId/trainings"
        Log.d("FirestoreRepository", "Запуск real-time listener для користувача: $userId")
        Log.d("FirestoreRepository", "Шлях до колекції: $collectionPath")

        val listenerRegistration = getUserTrainingsCollection()
            .orderBy("dateEpochDay", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Помилка слухача Firestore", error)
                    Log.e("FirestoreRepository", "Код помилки: ${error.code}, повідомлення: ${error.message}")
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("FirestoreRepository", "Snapshot отримано: кількість документів = ${snapshot.documents.size}")
                    
                    val trainings = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data
                            if (data != null) {
                                TrainingEntity(
                                    id = (data["id"] as? String) ?: doc.id,
                                    sport = (data["sport"] as? String) ?: "",
                                    dateEpochDay = (data["dateEpochDay"] as? Long) ?: 0L,
                                    dateText = (data["dateText"] as? String) ?: ""
                                )
                            } else {
                                Log.w("FirestoreRepository", "Документ ${doc.id} має null дані")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("FirestoreRepository", "Помилка парсингу документа ${doc.id}", e)
                            null
                        }
                    }
                    Log.d("FirestoreRepository", "Отримано ${trainings.size} тренувань з Firestore (real-time) для користувача $userId")
                    if (trainings.isNotEmpty()) {
                        trainings.forEach { t ->
                            Log.d("FirestoreRepository", "  - ${t.id}: ${t.sport} (${t.dateText})")
                        }
                    }
                    trySend(trainings)
                } else {
                    Log.w("FirestoreRepository", "Snapshot null - можливо, колекція порожня або немає доступу")
                }
            }

        awaitClose {
            Log.d("FirestoreRepository", "Зупинка real-time listener для користувача: $userId")
            listenerRegistration.remove()
        }
    }

    /**
     * Синхронізувати локальні дані з хмарою
     * Використовується при вході користувача
     */
    suspend fun syncFromCloud(localTrainings: List<TrainingEntity>): List<TrainingEntity> {
        if (!isUserSignedIn()) {
            Log.w("FirestoreRepository", "Користувач не авторизований, синхронізація неможлива")
            return localTrainings
        }

        try {
            val cloudTrainings = getAllTrainings()
            
            // Об'єднуємо локальні та хмарні дані (приоритет хмарі)
            val cloudIds = cloudTrainings.map { it.id }.toSet()
            val localOnly = localTrainings.filter { it.id !in cloudIds }
            
            // Зберігаємо локальні тренування, яких немає в хмарі
            localOnly.forEach { training ->
                try {
                    saveTraining(training)
                } catch (e: Exception) {
                    Log.w("FirestoreRepository", "Не вдалося зберегти локальне тренування ${training.id}", e)
                }
            }
            
            Log.d("FirestoreRepository", "Синхронізація завершена. Хмарних: ${cloudTrainings.size}, Локальних: ${localOnly.size}")
            return cloudTrainings + localOnly
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Помилка синхронізації", e)
            return localTrainings
        }
    }
}
