package com.example.sporttracker

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.sporttracker.data.DeleteSportDefinitionResult
import com.example.sporttracker.data.FirestoreRepository
import com.example.sporttracker.data.TrainingRepository
import com.example.sporttracker.db.AppDatabase
import com.example.sporttracker.db.SportDefinitionEntity
import com.example.sporttracker.db.TrainingEntity
import com.example.sporttracker.ui.auth.AuthScreen
import com.example.sporttracker.ui.auth.AuthViewModel
import com.example.sporttracker.ui.theme.ButtonCyan
import com.example.sporttracker.ui.theme.ButtonCyanActive
import com.example.sporttracker.ui.theme.ButtonCyanDark
import com.example.sporttracker.ui.theme.SportTrackerTheme
import kotlinx.coroutines.launch
import java.util.UUID
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.graphics.toColorInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Виводимо SHA-1 fingerprint в логи для додавання в Firebase Console
        try {
            // Для Android 9 (API 28) та вище
            val info = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = info.signingInfo
            val apkContentsSigners = signingInfo?.apkContentsSigners
            if (apkContentsSigners != null && apkContentsSigners.isNotEmpty()) {
                for (signature in apkContentsSigners) {
                    val md = java.security.MessageDigest.getInstance("SHA-1")
                    md.update(signature.toByteArray())
                    val digest = md.digest()
                    val toRet = StringBuilder()
                    for (i in digest.indices) {
                        if (i != 0) toRet.append(":")
                        val b = digest[i].toInt() and 0xff
                        val hex = Integer.toHexString(b)
                        if (hex.length == 1) toRet.append("0")
                        toRet.append(hex)
                    }
                    android.util.Log.d("SHA-1", "SHA-1 Fingerprint: $toRet")
                    android.util.Log.d("SHA-1", "Додайте цей SHA-1 в Firebase Console -> Project Settings -> Your apps -> Sport Tracker -> SHA certificate fingerprints")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SHA-1", "Помилка отримання SHA-1", e)
        }
        
        setContent {
            SportTrackerTheme {
                AuthWrapper()
            }
        }
    }
}

@Composable
private fun AuthWrapper() {
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Логуємо зміни статусу входу для діагностики
    LaunchedEffect(isSignedIn) {
        android.util.Log.d("AuthWrapper", "isSignedIn changed to: $isSignedIn")
    }
    
    if (!isSignedIn) {
        android.util.Log.d("AuthWrapper", "Showing AuthScreen")
        // Показуємо екран авторизації
        AuthScreen(
            viewModel = authViewModel,
            onSignInSuccess = {
                android.util.Log.d("AuthWrapper", "onSignInSuccess called")
                // Оновлюємо статус перед синхронізацією
                authViewModel.checkSignInStatus()
                // Після входу ініціалізуємо real-time синхронізацію
                scope.launch {
                    try {
                        android.util.Log.d("AuthWrapper", "Starting real-time sync...")
                        val database = AppDatabase.get(context)
                        val firestoreRepo = FirestoreRepository()
                        val trainingRepo = TrainingRepository(database, firestoreRepo)
                        trainingRepo.ensureBuiltinSportRows()
                        trainingRepo.initRealTimeSync()
                        trainingRepo.initSportDefinitionsSync()
                        android.util.Log.d("AuthWrapper", "Real-time sync initialized")
                    } catch (e: Exception) {
                        android.util.Log.e("AuthWrapper", "Помилка ініціалізації real-time sync", e)
                    }
                }
            }
        )
    } else {
        android.util.Log.d("AuthWrapper", "Showing AppRoot")
        // Показуємо основний екран
        AppRoot()
    }
}

private enum class Tab {
    LIST,
    ADD,
    STATS
}

// Прямокутна форма для кнопок з помірними закругленими кутами
private val ButtonShape = RoundedCornerShape(12.dp)

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val firestoreRepo = remember { FirestoreRepository() }
    val trainingRepo = remember { TrainingRepository(database, firestoreRepo) }
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()

    // Ініціалізуємо real-time синхронізацію з Firestore при відкритті AppRoot
    LaunchedEffect(Unit) {
        trainingRepo.ensureBuiltinSportRows()
        if (firestoreRepo.isUserSignedIn()) {
            try {
                android.util.Log.d("AppRoot", "Користувач авторизований, запускаємо real-time sync...")
                trainingRepo.initRealTimeSync()
                trainingRepo.initSportDefinitionsSync()
                android.util.Log.d("AppRoot", "Real-time sync запущено")
            } catch (e: Exception) {
                android.util.Log.e("AppRoot", "Помилка ініціалізації real-time sync", e)
            }
        }
    }

    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPage = Tab.entries.indexOf(Tab.LIST)
    )
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = Tab.entries[pagerState.currentPage]

    Scaffold(
        bottomBar = {
            val isDark = isSystemInDarkTheme()
            val navBg = if (isDark) ButtonCyanDark else ButtonCyan
            val activeBg = ButtonCyanActive
            val activeIcon = Color.White
            val inactiveIcon = Color.White.copy(alpha = 0.7f)

            Surface(
                color = navBg,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == Tab.LIST,
                        onClick = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = Tab.entries.indexOf(Tab.LIST),
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        },
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Список") },
                        label = { Text("Список") },
                        alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeIcon,
                            selectedTextColor = activeIcon,
                        unselectedIconColor = inactiveIcon,
                            unselectedTextColor = inactiveIcon,
                        indicatorColor = activeBg
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.ADD,
                        onClick = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = Tab.entries.indexOf(Tab.ADD),
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Додати") },
                        label = { Text("Додати") },
                        alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeIcon,
                            selectedTextColor = activeIcon,
                        unselectedIconColor = inactiveIcon,
                            unselectedTextColor = inactiveIcon,
                        indicatorColor = activeBg
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.STATS,
                        onClick = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = Tab.entries.indexOf(Tab.STATS),
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Статистика") },
                        label = { Text("Статистика") },
                        alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeIcon,
                            selectedTextColor = activeIcon,
                        unselectedIconColor = inactiveIcon,
                            unselectedTextColor = inactiveIcon,
                        indicatorColor = activeBg
                    )
                )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (Tab.entries[page]) {
                Tab.LIST -> {
                    val layoutDir = LocalLayoutDirection.current
                    // Для вкладки "Список" прибираємо нижній відступ від Scaffold (щоб контент впирався в navbar)
                    val listPadding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDir),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDir),
                        bottom = 0.dp
                    )
                    ListScreen(trainingRepo = trainingRepo, modifier = Modifier.padding(listPadding))
                }
                Tab.ADD -> AddScreen(trainingRepo = trainingRepo, modifier = Modifier.padding(innerPadding))
                Tab.STATS -> {
                    val layoutDir = LocalLayoutDirection.current
                    // Для вкладки "Статистика" прибираємо нижній відступ від Scaffold (щоб контент впирався в navbar)
                    val statsPadding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDir),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDir),
                        bottom = 0.dp
                    )
                    StatsScreen(
                        trainingRepo = trainingRepo,
                        authViewModel = authViewModel,
                        modifier = Modifier.padding(statsPadding)
                    )
        }
    }
}
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ListScreen(
    trainingRepo: TrainingRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // --- state
    var selectedTrainingForDeletion by remember { mutableStateOf<TrainingEntity?>(null) }
    val scope = rememberCoroutineScope()
    var sportFilter by remember { mutableStateOf<String?>(null) } // null = усі

    val today = remember { LocalDate.now() }
    var fromDate by remember { mutableStateOf(today.withDayOfMonth(1)) }
    var toDate by remember { mutableStateOf(today) }

    val fmt = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    // Material Date Picker state для "Від"
    var showFromDatePicker by remember { mutableStateOf(false) }
    val fromDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Material Date Picker state для "До"
    var showToDatePicker by remember { mutableStateOf(false) }
    val toDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Оновлюємо state при зміні дат
    LaunchedEffect(fromDate) {
        // State автоматично оновлюється через initialSelectedDateMillis при пересозданні
        // Але оскільки state не можна змінити напряму, ми просто пересоздаємо діалог
    }
    LaunchedEffect(toDate) {
        // Аналогічно для toDate
    }

    fun rangeEpochDays(a: LocalDate, b: LocalDate): Pair<Long, Long> {
        val from = if (a <= b) a else b
        val to = if (a <= b) b else a
        return from.toEpochDay() to to.toEpochDay()
    }

    val (fromDay, toDay) = remember(fromDate, toDate) { rangeEpochDays(fromDate, toDate) }

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        isRefreshing = true
        scope.launch {
            try {
                trainingRepo.syncWithCloud()
            } catch (e: Exception) {
                android.util.Log.e("ListScreen", "Sync error", e)
                Toast.makeText(context, "Помилка синхронізації", Toast.LENGTH_SHORT).show()
            } finally {
                isRefreshing = false
            }
        }
    })

    // --- flows from Repository
    val items: List<TrainingEntity> by trainingRepo
        .getFilteredTrainingsFlow(sportFilter, fromDay, toDay)
        .collectAsState(initial = emptyList())
    val count: Int by trainingRepo
        .getCountFlow(sportFilter, fromDay, toDay)
        .collectAsState(initial = 0)

    val sports: List<SportDefinitionEntity> by trainingRepo
        .observeSportDefinitions()
        .collectAsState(initial = emptyList())
    val sportsById = remember(sports) { sports.associateBy { it.id } }

    // --- ui
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Список тренувань",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp),
            textAlign = TextAlign.Center
        )

        // ---- Sport dropdown (self-contained)
        var expanded by remember { mutableStateOf(false) }
        val selectedSportLabel = sportFilter?.let { fid ->
            sportLabelFor(sportsById[fid], fid)
        } ?: "Усі"

        var anchorWidthPx by remember { mutableIntStateOf(0) }
        val anchorWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }
        val menuBg = MaterialTheme.colorScheme.surface
        val isDark = isSystemInDarkTheme()
        val buttonTextColor = if (isDark) Color.White else Color.Black

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        anchorWidthPx = coords.size.width
                    },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ButtonCyan.copy(alpha = 0.1f),
                    contentColor = buttonTextColor
                ),
                border = BorderStroke(1.dp, ButtonCyan),
                shape = ButtonShape
            ) {
                Text(
                    text = "Вид спорту: $selectedSportLabel",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
            ) {
                Surface(
                    color = menuBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(anchorWidthDp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
            ) {
                        // Елемент "Усі" з галочкою
                DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓",
                                        color = if (sportFilter == null) ButtonCyan else Color.Unspecified,
                                        fontWeight = if (sportFilter == null) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.width(20.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text("Усі")
                                }
                            },
                    onClick = {
                        sportFilter = null
                        expanded = false
                            },
                            modifier = Modifier.background(
                                if (sportFilter == null) ButtonCyan.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                        // Елементи з видами спорту з емодзі
                        sports.forEach { sport ->
                            val isSelected = sportFilter == sport.id
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sport.emoji,
                                            modifier = Modifier.width(20.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(sport.nameUa)
                                    }
                                },
                                onClick = {
                                    sportFilter = sport.id
                                    expanded = false
                                },
                                modifier = Modifier.background(
                                    if (isSelected) ButtonCyan.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Період",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Кнопки вибору періоду (Від/До) - OutlinedButton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    showFromDatePicker = true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ButtonCyan.copy(alpha = 0.1f),
                    contentColor = buttonTextColor
                ),
                border = BorderStroke(1.dp, ButtonCyan),
                shape = ButtonShape
            ) {
                Text(
                    text = "Від: ${fromDate.format(fmt)}",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    showToDatePicker = true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ButtonCyan.copy(alpha = 0.1f),
                    contentColor = buttonTextColor
                ),
                border = BorderStroke(1.dp, ButtonCyan),
                shape = ButtonShape
            ) {
                Text(
                    text = "До: ${toDate.format(fmt)}",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }

        HorizontalDivider()

        // Material Date Picker Dialog для "Від"
        if (showFromDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showFromDatePicker = false },
                confirmButton = {
                    TextButton(
                onClick = {
                            fromDatePickerState.selectedDateMillis?.let { millis ->
                                fromDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                if (fromDate.isAfter(toDate)) {
                                    toDate = fromDate
                    }
                }
                            showFromDatePicker = false
                        }
                    ) {
                        Text("OK", color = ButtonCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFromDatePicker = false }) {
                        Text("Скасувати")
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = ButtonCyan
                )
            ) {
                DatePicker(
                    state = fromDatePickerState
                )
            }
        }

        // Material Date Picker Dialog для "До"
        if (showToDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showToDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            toDatePickerState.selectedDateMillis?.let { millis ->
                                toDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                if (fromDate.isAfter(toDate)) {
                                    fromDate = toDate
            }
        }
                            showToDatePicker = false
                        }
                    ) {
                        Text("OK", color = ButtonCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showToDatePicker = false }) {
                        Text("Скасувати")
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = ButtonCyan
                )
            ) {
                DatePicker(
                    state = toDatePickerState
                )
            }
        }

        if (count == 0) {
            Text(
                text = "Немає тренувань",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            return@Column
        }

        Text("Кількість: $count", style = MaterialTheme.typography.titleMedium)

        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .pullRefresh(pullRefreshState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                val def = sportsById[item.sport]
                val sportUa = sportLabelFor(def, item.sport)
                val badgeColor = sportColorForId(item.sport)
                val emoji = sportEmojiFor(def)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = badgeColor.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(badgeColor.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sportUa,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = item.dateText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Кнопка видалення
                        IconButton(
                            onClick = {
                                selectedTrainingForDeletion = item
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Видалити",
                                tint = badgeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            // Додаємо невидимий блок в кінці, щоб картки не ховалися за nav bar
            // Використовуємо фіксований відступ для надійності на всіх пристроях (особливо Samsung)
            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        }

        // Діалог підтвердження видалення
        selectedTrainingForDeletion?.let { training ->
            AlertDialog(
                onDismissRequest = { selectedTrainingForDeletion = null },
                title = { Text("Видалити тренування?") },
                text = { Text("Ви впевнені, що хочете видалити тренування?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Закриваємо діалог одразу
                        selectedTrainingForDeletion = null
                        // Показуємо Toast одразу після натискання кнопки
                        val toast = Toast(context)
                        val textView = TextView(context)
                        textView.text = "Видалено ✅"
                        textView.setTextColor(android.graphics.Color.WHITE)
                        textView.textSize = 14f
                        textView.setPadding(48, 24, 48, 24)
                        val background = GradientDrawable().apply {
                            setColor("#CC000000".toColorInt())
                            cornerRadius = 24f
                        }
                        textView.background = background
                        @Suppress("DEPRECATION")
                        toast.view = textView
                        toast.duration = Toast.LENGTH_SHORT
                        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                        toast.show()
                        // Видаляємо тренування асинхронно
                        scope.launch {
                            trainingRepo.deleteTraining(training)
                        }
                    }) { Text("Видалити") }
                    },
                dismissButton = {
                    TextButton(onClick = { selectedTrainingForDeletion = null }) { Text("Скасувати") }
                    }
                )
            }
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScreen(
    trainingRepo: TrainingRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var selectedSport by remember { mutableStateOf<SportDefinitionEntity?>(null) }
    var showSportCatalog by remember { mutableStateOf(false) }

    val sports: List<SportDefinitionEntity> by trainingRepo
        .observeSportDefinitions()
        .collectAsState(initial = emptyList())

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val fmt = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    // Material Date Picker state для "Додати"
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        yearRange = IntRange(2020, LocalDate.now().year),
        initialDisplayMode = DisplayMode.Picker
    )

    fun saveTraining() {
        val sportId = selectedSport?.id ?: run {
            Toast.makeText(context, "Спочатку оберіть вид спорту", Toast.LENGTH_SHORT).show()
            return
        }

        // Показуємо Toast одразу після натискання кнопки (без іконки застосунку)
        val toast = Toast(context)
        val textView = TextView(context)
        textView.text = "Збережено ✅"
        textView.setTextColor(android.graphics.Color.WHITE)
        textView.textSize = 14f
        textView.setPadding(48, 24, 48, 24)
        // Створюємо темний напівпрозорий фон для Toast
        val background = GradientDrawable().apply {
            setColor("#CC000000".toColorInt()) // Напівпрозорий чорний
            cornerRadius = 24f // Закруглені кути
        }
        textView.background = background
        @Suppress("DEPRECATION")
        toast.view = textView
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()

        // Зберігаємо тренування асинхронно
        scope.launch {
            trainingRepo.saveTraining(
                TrainingEntity(
                    sport = sportId,
                    dateEpochDay = selectedDate.toEpochDay(),
                    dateText = selectedDate.format(fmt)
                )
            )
        }
    }

    val isDark = isSystemInDarkTheme()
    val buttonTextColor = if (isDark) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(38.dp)
        ) {
            Text(
                text = "Додати тренування",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp),
                textAlign = TextAlign.Center
            )

            SportDropdown(
                sports = sports,
                selected = selectedSport,
                onSelected = { selectedSport = it },
                buttonTextColor = buttonTextColor
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ButtonCyan.copy(alpha = 0.1f),
                    contentColor = buttonTextColor
                ),
                border = BorderStroke(1.dp, ButtonCyan),
                shape = ButtonShape,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Дата: ${selectedDate.format(fmt)}",
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Button(
                onClick = { saveTraining() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonCyan,
                    contentColor = buttonTextColor
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                shape = ButtonShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Зберегти",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = { showSportCatalog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = ButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isDark) {
                        ButtonCyanDark.copy(alpha = 0.42f)
                    } else {
                        ButtonCyan.copy(alpha = 0.28f)
                    },
                    contentColor = buttonTextColor
                ),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Редагувати види спорту",
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }

        // Material Date Picker Dialog для "Додати"
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val pickedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                val today = LocalDate.now()
                                if (pickedDate.isAfter(today)) {
                                    Toast.makeText(context, "Не можна вибрати майбутню дату", Toast.LENGTH_SHORT).show()
                                } else {
                                    selectedDate = pickedDate
                                }
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", color = ButtonCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Скасувати")
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = ButtonCyan
                )
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }
    }

    if (showSportCatalog) {
        SportCatalogDialog(
            trainingRepo = trainingRepo,
            onDismiss = { showSportCatalog = false }
        )
    }
}

@Composable
private fun SportDropdown(
    sports: List<SportDefinitionEntity>,
    selected: SportDefinitionEntity?,
    onSelected: (SportDefinitionEntity) -> Unit,
    buttonTextColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val anchorWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }

    // Адаптивний колір фону для випадаючого списку
    val menuBg = MaterialTheme.colorScheme.surface

    val density = LocalDensity.current

    Column {
        var dropdownWidth by remember { mutableStateOf(0.dp) }
        
        Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
                onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        anchorWidthPx = coordinates.size.width
                        dropdownWidth = with(density) { coordinates.size.width.toDp() }
                },
            colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ButtonCyan.copy(alpha = 0.1f),
                    contentColor = buttonTextColor
            ),
                border = BorderStroke(1.dp, ButtonCyan),
                shape = ButtonShape,
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
                val displayText = selected?.nameUa ?: "Оберіть вид спорту"
                Text(
                    text = displayText,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
                modifier = Modifier.width(dropdownWidth.takeIf { it > 0.dp } ?: 200.dp)
        ) {
            Surface(
                color = menuBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    sports.forEach { sport ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sport.emoji,
                                        modifier = Modifier.width(24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(sport.nameUa)
                                }
                            },
                            onClick = {
                                onSelected(sport)
                                expanded = false
                            },
                            modifier = Modifier.background(
                                if (selected?.id == sport.id) ButtonCyan.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun StatsScreen(
    trainingRepo: TrainingRepository,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val sports: List<SportDefinitionEntity> by trainingRepo
        .observeSportDefinitions()
        .collectAsState(initial = emptyList())
    val sportsById = remember(sports) { sports.associateBy { it.id } }

    val today = remember { LocalDate.now() }
    // Встановлюємо "Тиждень" як активний фільтр за замовчуванням
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    var fromDate by remember { mutableStateOf(startOfWeek) }
    var toDate by remember { mutableStateOf(today) }
    var selectedPeriodFilter by remember { mutableStateOf<String?>("week") } // "week", "month", "year"

    // Функції для швидких фільтрів
    fun setWeekFilter() {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        fromDate = startOfWeek
        // Поточний тиждень: від понеділка до сьогодні (не до неділі)
        toDate = today
        selectedPeriodFilter = "week"
    }

    fun setMonthFilter() {
        val today = LocalDate.now()
        fromDate = today.withDayOfMonth(1)
        toDate = today
        selectedPeriodFilter = "month"
    }

    fun setYearFilter() {
        val today = LocalDate.now()
        fromDate = today.withDayOfYear(1)
        toDate = today
        selectedPeriodFilter = "year"
    }

    // Функція для перевірки, чи можна перейти вперед
    fun canNavigateForward(): Boolean {
        val today = LocalDate.now()
        if (toDate.isAfter(today)) return false // Вже в майбутньому
        
        return when (selectedPeriodFilter) {
            "week" -> {
                // Наступний тиждень: додаємо 7 днів до початку тижня (понеділок)
                val nextWeekStart = fromDate.plusDays(7)
                val nextWeekEnd = nextWeekStart.plusDays(6) // Неділя наступного тижня
                // Можна перейти вперед, якщо неділя наступного тижня не пізніша за сьогодні
                nextWeekEnd.isBefore(today) || nextWeekEnd.isEqual(today)
            }
            "month" -> {
                val nextFromDate = fromDate.plusMonths(1).withDayOfMonth(1)
                // Можна перейти вперед, якщо початок наступного місяця не пізніший за сьогодні
                // (кінець місяця може бути в майбутньому, але ми обріжемо його до сьогодні в navigatePeriod)
                nextFromDate.isBefore(today) || nextFromDate.isEqual(today)
            }
            "year" -> {
                val nextFromDate = fromDate.plusYears(1).withDayOfYear(1)
                // Можна перейти вперед, якщо початок наступного року не пізніший за сьогодні
                // (кінець року може бути в майбутньому, але ми обріжемо його до сьогодні в navigatePeriod)
                nextFromDate.isBefore(today) || nextFromDate.isEqual(today)
            }
            else -> false
        }
    }

    // Функції для навігації по періодах
    fun navigatePeriod(direction: Int) { // -1 для попереднього, +1 для наступного
        val today = LocalDate.now()
        
        // Блокуємо навігацію в майбутнє
        if (direction > 0 && !canNavigateForward()) {
            return // Не дозволяємо перехід в майбутнє
        }
        
        when (selectedPeriodFilter) {
            "week" -> {
                val daysToAdd = direction * 7L
                val newFromDate = fromDate.plusDays(daysToAdd)
                
                // Якщо це попередній тиждень (direction < 0) - від понеділка до неділі
                // Якщо це поточний тиждень (direction >= 0) - від понеділка до сьогодні
                if (direction < 0) {
                    // Попередні тижні: завжди від понеділка до неділі (7 днів)
                    fromDate = newFromDate
                    toDate = newFromDate.plusDays(6)
                } else {
                    // Поточний тиждень: від понеділка до сьогодні
                    val newEndOfWeek = newFromDate.plusDays(6)
                    toDate = if (newEndOfWeek.isAfter(today)) today else newEndOfWeek
                    // Переконаємося, що початок тижня - понеділок
                    if (toDate <= today) {
                        val dayOfWeek = toDate.dayOfWeek.value
                        val diff = if (dayOfWeek == 7) 6L else (dayOfWeek - 1).toLong()
                        fromDate = toDate.minusDays(diff)
                    } else {
                        fromDate = newFromDate
                    }
                }
            }
            "month" -> {
                val newFromDate = if (direction > 0) {
                    fromDate.plusMonths(1).withDayOfMonth(1)
                } else {
                    fromDate.minusMonths(1).withDayOfMonth(1)
                }
                val tempToDate = newFromDate.withDayOfMonth(newFromDate.lengthOfMonth())
                // Переконаємося, що не виходимо за межі сьогодні
                if (tempToDate.isAfter(today)) {
                    toDate = today
                    // Якщо сьогодні в цьому ж місяці, що й newFromDate, використовуємо newFromDate
                    if (today.withDayOfMonth(1) == newFromDate) {
                        fromDate = newFromDate
                    } else {
                        fromDate = today.withDayOfMonth(1)
                    }
                } else {
                    toDate = tempToDate
                    fromDate = newFromDate
                }
            }
            "year" -> {
                val newFromDate = if (direction > 0) {
                    fromDate.plusYears(1).withDayOfYear(1)
                } else {
                    fromDate.minusYears(1).withDayOfYear(1)
                }
                val tempToDate = newFromDate.withDayOfYear(newFromDate.lengthOfYear())
                // Переконаємося, що не виходимо за межі сьогодні
                if (tempToDate.isAfter(today)) {
                    toDate = today
                    // Якщо сьогодні в цьому ж році, що й newFromDate, використовуємо newFromDate
                    if (today.withDayOfYear(1) == newFromDate) {
                        fromDate = newFromDate
                    } else {
                        fromDate = today.withDayOfYear(1)
                    }
                } else {
                    toDate = tempToDate
                    fromDate = newFromDate
                }
            }
        }
    }

    fun rangeEpochDays(a: LocalDate, b: LocalDate): Pair<Long, Long> {
        val from = minOf(a, b)
        val to = maxOf(a, b)
        return from.toEpochDay() to to.toEpochDay()
    }

    val (fromDay, toDay) = remember(fromDate, toDate) {
        rangeEpochDays(fromDate, toDate)
        }

    val total by trainingRepo.getCountForRangeFlow(fromDay, toDay).collectAsState(initial = 0)
    val rows by trainingRepo.getCountBySportForRangeFlow(fromDay, toDay).collectAsState(initial = emptyList())
    val map = remember(rows) { rows.associate { it.sport to it.cnt } }

    val scrollState = rememberScrollState()

    // Діалог підтвердження виходу
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Вихід з акаунту") },
            text = { Text("Ви дійсно хочете вийти з акаунту?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                    }
                ) {
                    Text("Вийти", color = ButtonCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Скасувати")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
            modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // Заголовок
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                    .padding(top = 44.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(8.dp))

        // Кольори для кнопок режиму та стрілок
        val isDark = isSystemInDarkTheme()
        val buttonTextColor = if (isDark) Color.White else Color.Black
        val activeBg = ButtonCyan
        val inactiveBg = ButtonCyan.copy(alpha = 0.3f)

        // --- Швидкі фільтри (Тиждень, Місяць, Рік) - звичайні Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { setWeekFilter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPeriodFilter == "week") activeBg else inactiveBg,
                    contentColor = buttonTextColor
                ),
                shape = ButtonShape,
                        modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                    Text(
                    text = "Тиждень",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

                    Button(
                onClick = { setMonthFilter() },
                        colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPeriodFilter == "month") activeBg else inactiveBg,
                            contentColor = buttonTextColor
                        ),
                shape = ButtonShape,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                    Text(
                    text = "Місяць",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                    }

                    Button(
                onClick = { setYearFilter() },
                        colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPeriodFilter == "year") activeBg else inactiveBg,
                            contentColor = buttonTextColor
                        ),
                shape = ButtonShape,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                    Text(
                    text = "Рік",
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Кнопки навігації по періодах (стрілки вліво/вправо)
                Row(
                    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
            IconButton(
                onClick = { navigatePeriod(-1) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Попередній період",
                    tint = ButtonCyan
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Показуємо вибраний період
                    Text(
                text = "${fromDate.format(fmt)} — ${toDate.format(fmt)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.width(16.dp))
            
            val canNavigate = canNavigateForward()
            IconButton(
                onClick = { navigatePeriod(1) },
                modifier = Modifier.size(48.dp),
                enabled = canNavigate
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Наступний період",
                    tint = if (canNavigate) ButtonCyan else Color.Gray.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.width(18.dp))

        Spacer(Modifier.width(8.dp))

        // --- Результати
        Text("Всього: $total", style = MaterialTheme.typography.titleMedium)

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { row ->
                val count = row.cnt
                if (count <= 0) return@forEach
                val def = sportsById[row.sport]
                val badgeColor = sportColorForId(row.sport)
                val bg = badgeColor.copy(alpha = 0.15f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sportEmojiFor(def),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = sportLabelFor(def, row.sport),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
        
        // Додаємо невидимий блок в кінці, щоб картки не ховалися за nav bar
        // Використовуємо фіксований відступ для надійності на всіх пристроях (особливо Samsung)
        Spacer(modifier = Modifier.height(70.dp))
        }

        // Кнопка виходу в правому верхньому кутку (поверх контенту)
        IconButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(1f) // Розміщуємо поверх Column
                .padding(top = 8.dp, end = 8.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Вийти з акаунту",
                tint = ButtonCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    } // Закриваємо Box
}

private sealed class SportEditorState {
    data object Hidden : SportEditorState()
    data class Edit(val sport: SportDefinitionEntity) : SportEditorState()
    data object CreateNew : SportEditorState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportCatalogDialog(
    trainingRepo: TrainingRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val sports by trainingRepo.observeSportDefinitions().collectAsState(initial = emptyList())
    LaunchedEffect(Unit) {
        trainingRepo.ensureBuiltinSportRows()
    }
    var editorState by remember { mutableStateOf<SportEditorState>(SportEditorState.Hidden) }
    var pendingDelete by remember { mutableStateOf<SportDefinitionEntity?>(null) }
    val listMaxHeight = remember(configuration.screenHeightDp) {
        (configuration.screenHeightDp * 0.58f).dp.coerceAtMost(520.dp).coerceAtLeast(220.dp)
    }
    val isDarkTheme = isSystemInDarkTheme()
    val catalogCloseTint = if (isDarkTheme) Color.White else Color.Black
    val catalogEditTint = if (isDarkTheme) Color.White else Color(0xFF424242)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp)
        ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Види спорту", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Закрити",
                                tint = catalogCloseTint
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(sports, key = { it.id }) { sport ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .background(ButtonCyan.copy(alpha = 0.08f))
                                    .padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sport.emoji, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    sport.nameUa,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { editorState = SportEditorState.Edit(sport) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Змінити",
                                        tint = catalogEditTint
                                    )
                                }
                                if (!sport.isBuiltIn) {
                                    IconButton(onClick = { pendingDelete = sport }) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = "Видалити",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { editorState = SportEditorState.CreateNew },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonCyan,
                            contentColor = Color.Black
                        ),
                        shape = ButtonShape
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Додати вид спорту")
                    }
                }
            }
    }

    when (val st = editorState) {
        is SportEditorState.Edit -> {
            SportDefinitionEditDialog(
                title = "Редагувати вид",
                initialName = st.sport.nameUa,
                initialEmoji = st.sport.emoji,
                confirmLabel = "Зберегти",
                onDismiss = { editorState = SportEditorState.Hidden },
                onConfirm = { name, emoji ->
                    scope.launch {
                        try {
                            trainingRepo.saveSportDefinition(
                                st.sport.copy(
                                    nameUa = name.trim(),
                                    emoji = emoji.trim().ifBlank { "🏅" }
                                )
                            )
                            Toast.makeText(context, "Збережено", Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {
                            Toast.makeText(context, "Помилка збереження", Toast.LENGTH_SHORT).show()
                        }
                        editorState = SportEditorState.Hidden
                    }
                }
            )
        }
        SportEditorState.CreateNew -> {
            SportDefinitionEditDialog(
                title = "Новий вид спорту",
                initialName = "",
                initialEmoji = "🏅",
                confirmLabel = "Додати",
                onDismiss = { editorState = SportEditorState.Hidden },
                onConfirm = { name, emoji ->
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введіть назву", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            try {
                                val order = trainingRepo.allocateNextSportSortOrder()
                                trainingRepo.saveSportDefinition(
                                    SportDefinitionEntity(
                                        id = UUID.randomUUID().toString(),
                                        nameUa = name.trim(),
                                        emoji = emoji.trim().ifBlank { "🏅" },
                                        sortOrder = order,
                                        isBuiltIn = false
                                    )
                                )
                                Toast.makeText(context, "Додано", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Помилка збереження", Toast.LENGTH_SHORT).show()
                            }
                            editorState = SportEditorState.Hidden
                        }
                    }
                }
            )
        }
        SportEditorState.Hidden -> Unit
    }

    pendingDelete?.let { toDel ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Видалити «${toDel.nameUa}»?") },
            text = {
                Text("Вид буде прибрано зі списку. Якщо в журналі ще є тренування з цим видом, видалення буде заблоковано.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            when (val r = trainingRepo.deleteSportDefinition(toDel)) {
                                DeleteSportDefinitionResult.Deleted ->
                                    Toast.makeText(context, "Видалено", Toast.LENGTH_SHORT).show()
                                is DeleteSportDefinitionResult.InUse ->
                                    Toast.makeText(
                                        context,
                                        "Є ${r.trainingCount} тренувань з цим видом — спочатку видаліть їх",
                                        Toast.LENGTH_LONG
                                    ).show()
                                DeleteSportDefinitionResult.IsBuiltIn ->
                                    Toast.makeText(context, "Неможливо видалити вбудований вид", Toast.LENGTH_SHORT).show()
                            }
                            pendingDelete = null
                        }
                    }
                ) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Скасувати") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportDefinitionEditDialog(
    title: String,
    initialName: String,
    initialEmoji: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    var name by remember(title, initialName) { mutableStateOf(initialName) }
    var emoji by remember(title, initialEmoji) { mutableStateOf(initialEmoji) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Назва") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Емодзі") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, emoji) }) {
                Text(confirmLabel, color = ButtonCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}