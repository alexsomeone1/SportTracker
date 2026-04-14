package com.example.sporttracker.data

import com.example.sporttracker.db.AppDatabase
import com.example.sporttracker.db.builtinSportDefinitions
import com.example.sporttracker.db.SportDefinitionEntity
import com.example.sporttracker.db.TrainingEntity
import com.example.sporttracker.db.SportCountRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Головний репозиторій для роботи з тренуваннями
 * Комбінує Room (локальне зберігання) та Firestore (хмарна синхронізація)
 */
class TrainingRepository(
    private val database: AppDatabase,
    private val firestoreRepository: FirestoreRepository
) {
    private val dao = database.trainingDao()
    private val sportDao = database.sportDefinitionDao()
    private val scope = CoroutineScope(SupervisorJob())

    fun observeSportDefinitions(): Flow<List<SportDefinitionEntity>> = sportDao.observeAll()

    /**
     * Гарантує наявність вбудованих видів спорту в Room (через DAO, щоб Flow оновився).
     */
    suspend fun ensureBuiltinSportRows() = withContext(Dispatchers.IO) {
        builtinSportDefinitions().forEach { sportDao.insertIgnore(it) }
    }

    suspend fun allocateNextSportSortOrder(): Int = sportDao.maxSortOrder() + 1

    suspend fun saveSportDefinition(sport: SportDefinitionEntity) {
        sportDao.upsert(sport)
        if (firestoreRepository.isUserSignedIn()) {
            try {
                firestoreRepository.saveSportDefinition(sport)
            } catch (e: Exception) {
                Log.e("TrainingRepository", "Firestore saveSportDefinition", e)
                throw e
            }
        }
    }

    suspend fun deleteSportDefinition(sport: SportDefinitionEntity): DeleteSportDefinitionResult {
        if (sport.isBuiltIn) return DeleteSportDefinitionResult.IsBuiltIn
        val used = sportDao.countTrainingsUsingSport(sport.id)
        if (used > 0) return DeleteSportDefinitionResult.InUse(used)
        sportDao.delete(sport)
        if (firestoreRepository.isUserSignedIn()) {
            firestoreRepository.deleteSportDefinition(sport.id)
        }
        return DeleteSportDefinitionResult.Deleted
    }

    /**
     * Синхронізація каталогу видів спорту з Firestore (upsert + початковий upload якщо хмара порожня).
     */
    fun initSportDefinitionsSync() {
        if (!firestoreRepository.isUserSignedIn()) {
            Log.d("TrainingRepository", "sportDefinitions sync: користувач не авторизований")
            return
        }
        scope.launch {
            try {
                ensureBuiltinSportRows()
                firestoreRepository.observeSportDefinitions().collect { remote ->
                    if (remote.isEmpty()) {
                        val local = sportDao.getAll()
                        if (local.isNotEmpty()) {
                            local.forEach { def ->
                                try {
                                    firestoreRepository.saveSportDefinition(def)
                                } catch (e: Exception) {
                                    Log.e("TrainingRepository", "Не вдалося завантажити вид спорту ${def.id}", e)
                                }
                            }
                        }
                    } else {
                        remote.forEach { sportDao.upsert(it) }
                    }
                }
            } catch (e: Exception) {
                Log.e("TrainingRepository", "Помилка sportDefinitions sync", e)
            }
        }
    }

    /**
     * Ініціалізувати real-time синхронізацію з Firestore
     * Викликається при авторизації користувача
     */
    fun initRealTimeSync() {
        if (!firestoreRepository.isUserSignedIn()) {
            Log.w("TrainingRepository", "Користувач не авторизований, real-time sync не запущено")
            return
        }

        Log.d("TrainingRepository", "Запуск real-time sync...")
        scope.launch {
            try {
                firestoreRepository.observeTrainings().collect { firestoreTrainings ->
                    Log.d("TrainingRepository", "Real-time listener спрацював: отримано ${firestoreTrainings.size} тренувань")
                    
                    // Отримуємо локальні дані
                    val localTrainings = dao.observeAll().first()
                    val localIds = localTrainings.map { it.id }.toSet()
                    val firestoreIds = firestoreTrainings.map { it.id }.toSet()

                    Log.d("TrainingRepository", "Локальних: ${localIds.size}, З Firestore: ${firestoreIds.size}")

                    // Видаляємо локальні тренування, яких немає в Firestore
                    val toDelete = localIds - firestoreIds
                    toDelete.forEach { id ->
                        val trainingToDelete = localTrainings.firstOrNull { it.id == id }
                        if (trainingToDelete != null) {
                            dao.delete(trainingToDelete)
                            Log.d("TrainingRepository", "Видалено локальне тренування $id (синхронізація з Firestore)")
                        }
                    }

                    // Додаємо/оновлюємо тренування з Firestore
                    var newCount = 0
                    var updateCount = 0
                    firestoreTrainings.forEach { training ->
                        val existing = localTrainings.firstOrNull { it.id == training.id }
                        if (existing == null) {
                            newCount++
                            Log.d("TrainingRepository", "Додано нове тренування: ${training.id} (${training.sport}, ${training.dateText})")
                        } else {
                            updateCount++
                        }
                        dao.upsert(training)
                    }

                    Log.d("TrainingRepository", "Real-time sync завершено: додано $newCount нових, оновлено $updateCount, видалено ${toDelete.size}")
                }
            } catch (e: Exception) {
                Log.e("TrainingRepository", "Помилка real-time sync", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Зберегти тренування (спочатку в Firestore, потім оновлюється Room через listener)
     */
    suspend fun saveTraining(training: TrainingEntity) {
        try {
            // Спочатку зберігаємо в Firestore (основне джерело даних)
            if (firestoreRepository.isUserSignedIn()) {
                try {
                    firestoreRepository.saveTraining(training)
                    Log.d("TrainingRepository", "Тренування збережено в Firestore: ${training.id}")
                    // Room оновиться автоматично через real-time listener
                } catch (e: Exception) {
                    Log.e("TrainingRepository", "Помилка збереження тренування в Firestore", e)
                    throw e
                }
            } else {
                // Якщо користувач не авторизований, зберігаємо локально
                dao.upsert(training)
                Log.w("TrainingRepository", "Користувач не авторизований, тренування збережено тільки локально")
            }
        } catch (e: Exception) {
            Log.e("TrainingRepository", "Помилка збереження тренування", e)
            throw e
        }
    }

    /**
     * Видалити тренування (спочатку з Firestore, потім оновлюється Room через listener)
     */
    suspend fun deleteTraining(training: TrainingEntity) {
        try {
            // Спочатку видаляємо з Firestore (основне джерело даних)
            if (firestoreRepository.isUserSignedIn()) {
                try {
                    firestoreRepository.deleteTraining(training.id)
                    Log.d("TrainingRepository", "Тренування видалено з Firestore: ${training.id}")
                    // Room оновиться автоматично через real-time listener
                } catch (e: Exception) {
                    Log.e("TrainingRepository", "Помилка видалення тренування з Firestore", e)
                    throw e
                }
            } else {
                // Якщо користувач не авторизований, видаляємо локально
                dao.delete(training)
                Log.w("TrainingRepository", "Користувач не авторизований, тренування видалено тільки локально")
            }
        } catch (e: Exception) {
            Log.e("TrainingRepository", "Помилка видалення тренування", e)
            throw e
        }
    }

    /**
     * Отримати відфільтровані тренування (Flow)
     */
    fun getFilteredTrainingsFlow(
        sport: String?,
        fromDay: Long,
        toDay: Long
    ): Flow<List<TrainingEntity>> = dao.observeFiltered(sport, fromDay, toDay)
    
    /**
     * Отримати кількість тренувань для періоду (Flow)
     */
    fun getCountFlow(
        sport: String?,
        fromDay: Long,
        toDay: Long
    ): Flow<Int> = dao.observeCount(sport, fromDay, toDay)

    /**
     * Отримати статистику по видам спорту (Flow)
     */
    fun getCountBySportForRangeFlow(fromDay: Long, toDay: Long): Flow<List<SportCountRow>> {
        return dao.observeCountBySportForRange(fromDay, toDay)
    }
    
    /**
     * Отримати кількість тренувань для періоду (Flow)
     */
    fun getCountForRangeFlow(fromDay: Long, toDay: Long): Flow<Int> {
        return dao.observeCountForRange(fromDay, toDay)
    }

    /**
     * Синхронізувати дані з хмарою
     * Викликається після входу користувача
     */
    suspend fun syncWithCloud() {
        if (!firestoreRepository.isUserSignedIn()) {
            Log.d("TrainingRepository", "Користувач не авторизований, синхронізація не потрібна")
            return
        }

        try {
            // Отримуємо локальні дані
            val localTrainings = dao.observeAll().first()
            
            Log.d("TrainingRepository", "Початок синхронізації. Локальних тренувань: ${localTrainings.size}")
            
            // Синхронізуємо з хмарою
            val syncedTrainings = firestoreRepository.syncFromCloud(localTrainings)
            
            // Отримуємо ID синхронізованих тренувань
            val syncedIds = syncedTrainings.map { it.id }.toSet()
            
            // Видаляємо локальні тренування, яких немає в хмарі
            val localIds = localTrainings.map { it.id }.toSet()
            val toDelete = localIds - syncedIds
            toDelete.forEach { id ->
                try {
                    val trainingToDelete = localTrainings.firstOrNull { it.id == id }
                    if (trainingToDelete != null) {
                        dao.delete(trainingToDelete)
                        Log.d("TrainingRepository", "Видалено локальне тренування $id, якого немає в хмарі")
                    }
                } catch (e: Exception) {
                    Log.e("TrainingRepository", "Помилка видалення локального тренування $id", e)
                }
            }
            
            // Оновлюємо локальні дані синхронізованими
            syncedTrainings.forEach { training ->
                try {
                    dao.upsert(training)
                } catch (e: Exception) {
                    Log.e("TrainingRepository", "Помилка оновлення локального тренування ${training.id}", e)
                }
            }
            
            Log.d("TrainingRepository", "Синхронізація завершена успішно. Синхронізовано ${syncedTrainings.size} тренувань, видалено ${toDelete.size} локальних")

            syncSportDefinitionsOneShot()
        } catch (e: Exception) {
            Log.e("TrainingRepository", "Помилка синхронізації з хмарою", e)
            // Не кидаємо виняток, щоб додаток продовжував працювати з локальними даними
        }
    }

    private suspend fun syncSportDefinitionsOneShot() {
        if (!firestoreRepository.isUserSignedIn()) return
        try {
            ensureBuiltinSportRows()
            var remote = firestoreRepository.getAllSportDefinitions()
            val local = sportDao.getAll()
            if (remote.isEmpty() && local.isNotEmpty()) {
                local.forEach { firestoreRepository.saveSportDefinition(it) }
                remote = firestoreRepository.getAllSportDefinitions()
            }
            remote.forEach { sportDao.upsert(it) }
            local.filter { it.id !in remote.map { r -> r.id }.toSet() }.forEach { def ->
                try {
                    firestoreRepository.saveSportDefinition(def)
                } catch (e: Exception) {
                    Log.w("TrainingRepository", "Не вдалося вивантажити вид спорту ${def.id}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TrainingRepository", "Помилка syncSportDefinitionsOneShot", e)
        }
    }
}

sealed class DeleteSportDefinitionResult {
    data object Deleted : DeleteSportDefinitionResult()
    data object IsBuiltIn : DeleteSportDefinitionResult()
    data class InUse(val trainingCount: Int) : DeleteSportDefinitionResult()
}
