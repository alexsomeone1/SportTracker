// Вбудовані види спорту (як у Android: db/BuiltinSportSeeds.kt + міграція 3→4)
const BUILTIN_SPORT_DEFINITIONS = [
  { id: 'GYM', nameUa: 'Силове тренування', emoji: '🏋️', sortOrder: 0, isBuiltIn: true },
  { id: 'FOOTBALL', nameUa: 'Футбол', emoji: '⚽', sortOrder: 1, isBuiltIn: true },
  { id: 'RUNNING', nameUa: 'Біг', emoji: '🏃', sortOrder: 2, isBuiltIn: true },
  { id: 'TABLE_TENNIS', nameUa: 'Настільний теніс', emoji: '🏓', sortOrder: 3, isBuiltIn: true },
  { id: 'TENNIS', nameUa: 'Теніс', emoji: '🎾', sortOrder: 4, isBuiltIn: true },
  { id: 'SWIMMING', nameUa: 'Плавання', emoji: '🏊', sortOrder: 5, isBuiltIn: true },
  { id: 'CYCLING', nameUa: 'Велосипед', emoji: '🚴', sortOrder: 6, isBuiltIn: true }
];

// Кольори бейджів для вбудованих id (як у Android: SportType.kt)
const BUILTIN_SPORT_COLORS = {
  GYM: '#7C4DFF',
  FOOTBALL: '#2E7D32',
  RUNNING: '#FF6D00',
  TABLE_TENNIS: '#00838F',
  TENNIS: '#43A047',
  SWIMMING: '#1565C0',
  CYCLING: '#6D4C41'
};

const FALLBACK_SPORT_COLORS = ['#5C6BC0', '#00897B', '#E65100', '#6A1B9A', '#00695C', '#C62828'];

function sportColorForId(sportId) {
  if (!sportId) return '#546E7A';
  const fixed = BUILTIN_SPORT_COLORS[sportId];
  if (fixed) return fixed;
  let h = 0;
  for (let i = 0; i < sportId.length; i++) {
    h = (h * 31 + sportId.charCodeAt(i)) | 0;
  }
  return FALLBACK_SPORT_COLORS[Math.abs(h) % FALLBACK_SPORT_COLORS.length];
}

function sortSportDefinitions(list) {
  return [...list].sort((a, b) => {
    if (a.sortOrder !== b.sortOrder) return a.sortOrder - b.sortOrder;
    return (a.nameUa || '').localeCompare(b.nameUa || '', 'uk');
  });
}

function parseSportDefinitionDoc(docId, data) {
  if (!data) return null;
  try {
    const emojiRaw = data.emoji != null ? String(data.emoji) : '';
    return {
      id: data.id || docId,
      nameUa: data.nameUa || '',
      emoji: emojiRaw.trim() ? emojiRaw : '🏅',
      sortOrder: typeof data.sortOrder === 'number' ? data.sortOrder : parseInt(data.sortOrder, 10) || 0,
      isBuiltIn: data.isBuiltIn === true
    };
  } catch (e) {
    console.error('Помилка парсингу sportDefinition', docId, e);
    return null;
  }
}

/** Каталог з Firestore (users/uid/sportDefinitions), оновлюється listener-ом */
let sportDefinitions = [];

function sportDisplayInfo(sportId) {
  const def = sportDefinitions.find((d) => d.id === sportId);
  const name = def && def.nameUa ? def.nameUa : sportId;
  const emoji = def && def.emoji ? def.emoji : '🏅';
  const color = sportColorForId(sportId);
  return { name, emoji, color };
}

function escapeHtmlAttr(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function rebuildSportDropdowns() {
  const filterDropdown = document.getElementById('sport-filter-dropdown');
  const selectDropdown = document.getElementById('sport-select-dropdown');
  if (!filterDropdown || !selectDropdown) return;

  const list = sortSportDefinitions(sportDefinitions);
  const filterRows = [
    '<div class="sport-filter-item active" data-sport=""><span class="sport-filter-check">✓</span><span>Усі</span></div>',
    ...list.map(
      (s) =>
        `<div class="sport-filter-item" data-sport="${escapeHtmlAttr(s.id)}"><span class="sport-filter-emoji">${s.emoji}</span><span>${escapeHtmlAttr(s.nameUa)}</span></div>`
    )
  ].join('');

  const selectRows = [
    '<div class="sport-filter-item active" data-sport=""><span class="sport-filter-check">✓</span><span>Оберіть вид спорту</span></div>',
    ...list.map(
      (s) =>
        `<div class="sport-filter-item" data-sport="${escapeHtmlAttr(s.id)}"><span class="sport-filter-emoji">${s.emoji}</span><span>${escapeHtmlAttr(s.nameUa)}</span></div>`
    )
  ].join('');

  filterDropdown.innerHTML = filterRows;
  selectDropdown.innerHTML = selectRows;

  const sportFilterText = document.getElementById('sport-filter-text');
  if (listSportFilter) {
    const info = sportDisplayInfo(listSportFilter);
    if (sportFilterText) sportFilterText.textContent = `Вид спорту: ${info.name}`;
    filterDropdown.querySelectorAll('.sport-filter-item').forEach((el) => {
      el.classList.toggle('active', (el.dataset.sport || '') === listSportFilter);
    });
  } else {
    if (sportFilterText) sportFilterText.textContent = 'Вид спорту: Усі';
    filterDropdown.querySelectorAll('.sport-filter-item').forEach((el) => {
      el.classList.toggle('active', (el.dataset.sport || '') === '');
    });
  }

  const sportSelectText = document.getElementById('sport-select-text');
  if (selectedSport) {
    const info = sportDisplayInfo(selectedSport);
    if (sportSelectText) sportSelectText.textContent = info.name;
    selectDropdown.querySelectorAll('.sport-filter-item').forEach((el) => {
      el.classList.toggle('active', (el.dataset.sport || '') === selectedSport);
    });
  } else {
    if (sportSelectText) sportSelectText.textContent = 'Оберіть вид спорту';
    selectDropdown.querySelectorAll('.sport-filter-item').forEach((el) => {
      el.classList.toggle('active', (el.dataset.sport || '') === '');
    });
  }
}

function seedBuiltinSportDefinitions(uid) {
  const col = db.collection('users').doc(uid).collection('sportDefinitions');
  const batch = db.batch();
  BUILTIN_SPORT_DEFINITIONS.forEach((def) => {
    batch.set(col.doc(def.id), {
      id: def.id,
      nameUa: def.nameUa,
      emoji: def.emoji,
      sortOrder: def.sortOrder,
      isBuiltIn: def.isBuiltIn
    });
  });
  return batch.commit();
}

let sportDefinitionsListener = null;
let sportDefinitionsSeedAttempted = false;

function initSportDefinitionsListener() {
  if (!currentUser) return;
  if (sportDefinitionsListener) {
    sportDefinitionsListener();
    sportDefinitionsListener = null;
  }
  sportDefinitionsSeedAttempted = false;

  const col = db.collection('users').doc(currentUser.uid).collection('sportDefinitions');
  sportDefinitionsListener = col.onSnapshot(
    (snapshot) => {
      if (snapshot.empty) {
        sportDefinitions = sortSportDefinitions(BUILTIN_SPORT_DEFINITIONS.map((d) => ({ ...d })));
        rebuildSportDropdowns();
        renderTrainings();
        updateStats();
        if (!sportDefinitionsSeedAttempted) {
          sportDefinitionsSeedAttempted = true;
          seedBuiltinSportDefinitions(currentUser.uid).catch((err) => {
            console.error('Помилка сиду sportDefinitions', err);
            showToast('Не вдалося зберегти каталог видів спорту в хмару');
          });
        }
        refreshSportCatalogIfOpen();
        return;
      }
      sportDefinitionsSeedAttempted = false;
      const defs = [];
      snapshot.forEach((doc) => {
        const parsed = parseSportDefinitionDoc(doc.id, doc.data());
        if (parsed) defs.push(parsed);
      });
      sportDefinitions = sortSportDefinitions(defs);
      rebuildSportDropdowns();
      renderTrainings();
      updateStats();
      refreshSportCatalogIfOpen();
    },
    (error) => {
      console.error('Помилка listener sportDefinitions:', error);
      sportDefinitions = sortSportDefinitions(BUILTIN_SPORT_DEFINITIONS.map((d) => ({ ...d })));
      rebuildSportDropdowns();
      renderTrainings();
      updateStats();
      refreshSportCatalogIfOpen();
    }
  );
}

/** Каталог видів спорту в модалці (як SportCatalogDialog у Android) */
let sportEditorContext = null; // null | { mode: 'edit', sport } | { mode: 'create' }
let sportPendingDelete = null;

function refreshSportCatalogIfOpen() {
  const m = document.getElementById('sport-catalog-modal');
  if (m && !m.hidden) renderSportCatalogList();
}

function newSportId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 's' + Date.now().toString(36) + Math.random().toString(36).slice(2, 10);
}

function allocateNextSportSortOrder() {
  let max = -1;
  sportDefinitions.forEach((d) => {
    if (typeof d.sortOrder === 'number' && d.sortOrder > max) max = d.sortOrder;
  });
  return max + 1;
}

function countTrainingsUsingSport(sportId) {
  return db
    .collection('users')
    .doc(currentUser.uid)
    .collection('trainings')
    .where('sport', '==', sportId)
    .get()
    .then((snap) => snap.size);
}

function saveSportDefinitionRemote(def) {
  return db
    .collection('users')
    .doc(currentUser.uid)
    .collection('sportDefinitions')
    .doc(def.id)
    .set({
      id: def.id,
      nameUa: def.nameUa,
      emoji: def.emoji,
      sortOrder: def.sortOrder,
      isBuiltIn: def.isBuiltIn
    });
}

function setModalVisible(overlay, visible) {
  if (!overlay) return;
  overlay.hidden = !visible;
  overlay.setAttribute('aria-hidden', visible ? 'false' : 'true');
}

function openSportCatalogModal() {
  if (!currentUser) {
    showToast('Увійдіть у акаунт');
    return;
  }
  const overlay = document.getElementById('sport-catalog-modal');
  setModalVisible(overlay, true);
  renderSportCatalogList();
}

function closeSportCatalogModal() {
  setModalVisible(document.getElementById('sport-catalog-modal'), false);
}

function renderSportCatalogList() {
  const el = document.getElementById('sport-catalog-list');
  if (!el) return;
  const list = sortSportDefinitions(sportDefinitions);
  el.innerHTML = list
    .map((s) => {
      const delBtn = s.isBuiltIn
        ? ''
        : `<button type="button" class="sport-catalog-icon-btn material-icons sport-catalog-delete" data-sport-del="${escapeHtmlAttr(s.id)}" aria-label="Видалити">delete_outline</button>`;
      return `<div class="sport-catalog-row" data-sport-id="${escapeHtmlAttr(s.id)}">
        <span class="sport-catalog-emoji">${s.emoji}</span>
        <span class="sport-catalog-name">${escapeHtmlAttr(s.nameUa)}</span>
        <button type="button" class="sport-catalog-icon-btn material-icons" data-sport-edit="${escapeHtmlAttr(s.id)}" aria-label="Змінити">edit</button>
        ${delBtn}
      </div>`;
    })
    .join('');
}

function openSportEditorCreate() {
  sportEditorContext = { mode: 'create' };
  document.getElementById('sport-editor-title').textContent = 'Новий вид спорту';
  document.getElementById('sport-editor-name').value = '';
  document.getElementById('sport-editor-emoji').value = '🏅';
  const saveBtn = document.getElementById('sport-editor-save');
  if (saveBtn) saveBtn.textContent = 'Додати';
  setModalVisible(document.getElementById('sport-editor-modal'), true);
  document.getElementById('sport-editor-name').focus();
}

function openSportEditorEdit(sport) {
  sportEditorContext = { mode: 'edit', sport: { ...sport } };
  document.getElementById('sport-editor-title').textContent = 'Редагувати вид';
  document.getElementById('sport-editor-name').value = sport.nameUa || '';
  document.getElementById('sport-editor-emoji').value = sport.emoji || '🏅';
  const saveBtn = document.getElementById('sport-editor-save');
  if (saveBtn) saveBtn.textContent = 'Зберегти';
  setModalVisible(document.getElementById('sport-editor-modal'), true);
  document.getElementById('sport-editor-name').focus();
}

function closeSportEditorModal() {
  sportEditorContext = null;
  setModalVisible(document.getElementById('sport-editor-modal'), false);
}

function confirmSportEditor() {
  const nameEl = document.getElementById('sport-editor-name');
  const emojiEl = document.getElementById('sport-editor-emoji');
  const name = (nameEl && nameEl.value.trim()) || '';
  const emojiRaw = emojiEl && emojiEl.value != null ? emojiEl.value.trim() : '';
  const emoji = emojiRaw || '🏅';

  if (!sportEditorContext) return;

  if (sportEditorContext.mode === 'create') {
    if (!name) {
      showToast('Введіть назву');
      return;
    }
    const def = {
      id: newSportId(),
      nameUa: name,
      emoji,
      sortOrder: allocateNextSportSortOrder(),
      isBuiltIn: false
    };
    saveSportDefinitionRemote(def)
      .then(() => {
        showToast('Додано');
        closeSportEditorModal();
      })
      .catch((err) => {
        console.error(err);
        showToast('Помилка збереження');
      });
    return;
  }

  if (sportEditorContext.mode === 'edit') {
    const base = sportEditorContext.sport;
    const def = {
      ...base,
      nameUa: name || base.nameUa,
      emoji,
      sortOrder: base.sortOrder,
      isBuiltIn: base.isBuiltIn
    };
    saveSportDefinitionRemote(def)
      .then(() => {
        showToast('Збережено');
        closeSportEditorModal();
      })
      .catch((err) => {
        console.error(err);
        showToast('Помилка збереження');
      });
  }
}

function openSportDeleteConfirm(sport) {
  sportPendingDelete = sport;
  document.getElementById('sport-delete-title').textContent = `Видалити «${sport.nameUa}»?`;
  document.getElementById('sport-delete-text').textContent =
    'Вид буде прибрано зі списку. Якщо в журналі ще є тренування з цим видом, видалення буде заблоковано.';
  setModalVisible(document.getElementById('sport-delete-modal'), true);
}

function closeSportDeleteModal() {
  sportPendingDelete = null;
  setModalVisible(document.getElementById('sport-delete-modal'), false);
}

function confirmSportDelete() {
  const sport = sportPendingDelete;
  if (!sport || !currentUser) {
    closeSportDeleteModal();
    return;
  }
  if (sport.isBuiltIn) {
    showToast('Неможливо видалити вбудований вид');
    closeSportDeleteModal();
    return;
  }
  countTrainingsUsingSport(sport.id)
    .then((n) => {
      if (n > 0) {
        showToast(`Є ${n} тренувань з цим видом — спочатку видаліть їх`);
        closeSportDeleteModal();
        return Promise.reject(new Error('sport-delete-skip'));
      }
      return db
        .collection('users')
        .doc(currentUser.uid)
        .collection('sportDefinitions')
        .doc(sport.id)
        .delete();
    })
    .then(() => {
      showToast('Видалено');
      closeSportDeleteModal();
    })
    .catch((err) => {
      if (err && err.message === 'sport-delete-skip') return;
      console.error(err);
      showToast('Помилка видалення');
      closeSportDeleteModal();
    });
}

function initSportCatalogModals() {
  const catalog = document.getElementById('sport-catalog-modal');
  const editor = document.getElementById('sport-editor-modal');
  const delModal = document.getElementById('sport-delete-modal');
  const editSportsBtn = document.getElementById('edit-sports-btn');

  if (catalog) {
    catalog.addEventListener('click', (e) => {
      if (e.target === catalog) closeSportCatalogModal();
    });
    document.getElementById('sport-catalog-close')?.addEventListener('click', closeSportCatalogModal);
    document.getElementById('sport-catalog-add-btn')?.addEventListener('click', () => {
      openSportEditorCreate();
    });
    document.getElementById('sport-catalog-list')?.addEventListener('click', (e) => {
      const editBtn = e.target.closest('[data-sport-edit]');
      if (editBtn) {
        const id = editBtn.getAttribute('data-sport-edit');
        const sport = sportDefinitions.find((x) => x.id === id);
        if (sport) openSportEditorEdit(sport);
        return;
      }
      const delBtn = e.target.closest('[data-sport-del]');
      if (delBtn) {
        const id = delBtn.getAttribute('data-sport-del');
        const sport = sportDefinitions.find((x) => x.id === id);
        if (sport) openSportDeleteConfirm(sport);
      }
    });
  }

  if (editor) {
    editor.addEventListener('click', (e) => {
      if (e.target === editor) closeSportEditorModal();
    });
    document.getElementById('sport-editor-cancel')?.addEventListener('click', closeSportEditorModal);
    document.getElementById('sport-editor-save')?.addEventListener('click', confirmSportEditor);
  }

  if (delModal) {
    delModal.addEventListener('click', (e) => {
      if (e.target === delModal) closeSportDeleteModal();
    });
    document.getElementById('sport-delete-cancel')?.addEventListener('click', closeSportDeleteModal);
    document.getElementById('sport-delete-confirm')?.addEventListener('click', confirmSportDelete);
  }

  editSportsBtn?.addEventListener('click', () => openSportCatalogModal());
}

let currentUser = null;
let trainings = [];
let currentPeriodFilter = null; // Фільтр періоду для статистики
let statsFromDate = null; // Дата "Від" для статистики
let statsToDate = null; // Дата "До" для статистики
let listFromDate = null; // Дата "Від" для списку
let listToDate = null; // Дата "До" для списку
let listSportFilter = null; // Фільтр виду спорту для списку
let selectedSport = null; // Вибраний вид спорту для додавання
let trainingsListener = null; // Real-time listener для тренувань

// Функція для форматування дати з Date об'єкта (dd.MM.yyyy)
function formatDateUTC(date) {
  const day = String(date.getUTCDate()).padStart(2, '0');
  const month = String(date.getUTCMonth() + 1).padStart(2, '0');
  const year = date.getUTCFullYear();
  return `${day}.${month}.${year}`;
}

// Ініціалізація
document.addEventListener('DOMContentLoaded', () => {
  // Спочатку приховуємо обидва екрани, щоб уникнути flash при завантаженні
  const authScreen = document.getElementById('auth-screen');
  const mainScreen = document.getElementById('main-screen');
  if (authScreen) authScreen.style.display = 'none';
  if (mainScreen) mainScreen.style.display = 'none';
  
  // Приховуємо попередження COOP (Cross-Origin-Opener-Policy) від Firebase Authentication
  // Ці попередження не впливають на функціональність, але можуть засмічувати консоль
  const originalError = console.error;
  console.error = function(...args) {
    const message = args.join(' ');
    // Фільтруємо попередження COOP
    if (message.includes('Cross-Origin-Opener-Policy') && 
        (message.includes('window.closed') || message.includes('window.close'))) {
      return; // Ігноруємо ці попередження
    }
    originalError.apply(console, args);
  };
  
  // Перевіряємо, чи Firebase завантажено
  if (typeof firebase === 'undefined') {
    console.error('Firebase не завантажено! Перевірте підключення скриптів.');
    showToast('Помилка завантаження Firebase');
    // Показуємо екран авторизації навіть при помилці
    if (authScreen) {
      authScreen.style.display = 'flex';
      authScreen.classList.add('active');
    }
    return;
  }
  
  // Перевіряємо, чи auth та db ініціалізовані
  if (!auth || !db) {
    console.error('Firebase auth або db не ініціалізовані!');
    showToast('Помилка ініціалізації Firebase');
    // Показуємо екран авторизації навіть при помилці
    if (authScreen) {
      authScreen.style.display = 'flex';
      authScreen.classList.add('active');
    }
    return;
  }
  
  // Перевіряємо, чи логотип завантажився
  const logo = document.getElementById('app-logo');
  if (logo) {
    logo.onerror = function() {
      // Якщо іконка не знайдена, ховаємо її
      this.style.display = 'none';
    };
  }
  
  sportDefinitions = sortSportDefinitions(BUILTIN_SPORT_DEFINITIONS.map((d) => ({ ...d })));
  rebuildSportDropdowns();
  initSportDropdownDelegation();

  initAuth();
  initNavigation();
  initForms();
  initListFilters();
  initSportCatalogModals();
  registerServiceWorker();
  
  // Відновлюємо збережену вкладку буде виконано в showMainScreen
  // Тут тільки встановлюємо початковий стан для анімації
  const savedTab = localStorage.getItem('sportTrackerActiveTab') || 'list';
  const tabsContainer = document.getElementById('tabs-container');
  if (tabsContainer) {
    tabsContainer.setAttribute('data-current-tab', savedTab);
  }
});

// Реєстрація Service Worker
function registerServiceWorker() {
  // Service Worker працює тільки з http/https, не з file://
  if ('serviceWorker' in navigator && (location.protocol === 'http:' || location.protocol === 'https:')) {
    navigator.serviceWorker.register('/service-worker.js')
      .then(reg => console.log('Service Worker registered', reg))
      .catch(err => console.log('Service Worker registration failed', err));
  } else {
    console.log('Service Worker не підтримується або використовується file:// протокол');
  }
}

// Авторизація
function initSportDropdownDelegation() {
  const sportFilterBtn = document.getElementById('sport-filter-btn');
  const sportFilterDropdown = document.getElementById('sport-filter-dropdown');
  const sportSelectBtn = document.getElementById('sport-select-btn');
  const sportSelectDropdown = document.getElementById('sport-select-dropdown');

  if (sportFilterBtn && sportFilterDropdown) {
    sportFilterBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      sportFilterDropdown.classList.toggle('active');
    });
  }
  if (sportSelectBtn && sportSelectDropdown) {
    sportSelectBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      sportSelectDropdown.classList.toggle('active');
    });
  }

  document.addEventListener('click', (e) => {
    const fd = document.getElementById('sport-filter-dropdown');
    const sd = document.getElementById('sport-select-dropdown');
    const fb = document.getElementById('sport-filter-btn');
    const sb = document.getElementById('sport-select-btn');

    const filterItem = e.target.closest('#sport-filter-dropdown .sport-filter-item');
    if (filterItem && fd && fd.contains(filterItem)) {
      const sport = filterItem.dataset.sport || null;
      listSportFilter = sport || null;
      const sportFilterText = document.getElementById('sport-filter-text');
      if (!sport) {
        if (sportFilterText) sportFilterText.textContent = 'Вид спорту: Усі';
      } else {
        const info = sportDisplayInfo(sport);
        if (sportFilterText) sportFilterText.textContent = `Вид спорту: ${info.name}`;
      }
      fd.querySelectorAll('.sport-filter-item').forEach((i) => i.classList.remove('active'));
      filterItem.classList.add('active');
      fd.classList.remove('active');
      renderTrainings();
      return;
    }

    const selectItem = e.target.closest('#sport-select-dropdown .sport-filter-item');
    if (selectItem && sd && sd.contains(selectItem)) {
      const sport = selectItem.dataset.sport || null;
      selectedSport = sport || null;
      const sportSelectText = document.getElementById('sport-select-text');
      if (!sport) {
        if (sportSelectText) sportSelectText.textContent = 'Оберіть вид спорту';
      } else {
        const info = sportDisplayInfo(sport);
        if (sportSelectText) sportSelectText.textContent = info.name;
      }
      sd.querySelectorAll('.sport-filter-item').forEach((i) => i.classList.remove('active'));
      selectItem.classList.add('active');
      sd.classList.remove('active');
      return;
    }

    if (fb && fd && !fb.contains(e.target) && !fd.contains(e.target)) {
      fd.classList.remove('active');
    }
    if (sb && sd && !sb.contains(e.target) && !sd.contains(e.target)) {
      sd.classList.remove('active');
    }
  });
}

function initAuth() {
  auth.onAuthStateChanged((user) => {
    if (user) {
      currentUser = user;
      showMainScreen();
      // Ініціалізуємо real-time listener замість одноразового завантаження
      initTrainingsListener();
      initSportDefinitionsListener();
    } else {
      currentUser = null;
      trainings = [];
      // Видаляємо listener при виході
      if (trainingsListener) {
        trainingsListener();
        trainingsListener = null;
      }
      if (sportDefinitionsListener) {
        sportDefinitionsListener();
        sportDefinitionsListener = null;
      }
      listSportFilter = null;
      selectedSport = null;
      sportDefinitions = sortSportDefinitions(BUILTIN_SPORT_DEFINITIONS.map((d) => ({ ...d })));
      rebuildSportDropdowns();
      showAuthScreen();
    }
  });

  document.getElementById('google-sign-in-btn').addEventListener('click', signInWithGoogle);
  
  // Кнопка виходу - використовуємо делегування подій, оскільки кнопка може бути не створена одразу
  document.addEventListener('click', (e) => {
    if (e.target.id === 'logout-btn' || e.target.closest('#logout-btn')) {
      if (confirm('Ви дійсно хочете вийти з акаунту?')) {
        signOut();
      }
    }
  });
}

// Флаг для запобігання одночасному відкриттю кількох popup
let isSigningIn = false;

function signInWithGoogle() {
  // Перевіряємо, чи вже не відкритий popup
  if (isSigningIn) {
    console.log('Вхід вже в процесі...');
    return;
  }
  
  // Перевіряємо, чи користувач вже залогінений
  if (auth.currentUser) {
    console.log('Користувач вже залогінений');
    return;
  }
  
  console.log('Спроба входу через Google...');
  console.log('Firebase auth:', auth);
  console.log('Firebase config:', firebaseConfig);
  
  isSigningIn = true;
  
  try {
    const provider = new firebase.auth.GoogleAuthProvider();
    console.log('GoogleAuthProvider створено');
    
    auth.signInWithPopup(provider)
      .then((result) => {
        isSigningIn = false;
        console.log('Успішний вхід:', result.user);
        console.log('User email:', result.user.email);
        showToast('Успішний вхід ✅');
      })
      .catch((error) => {
        isSigningIn = false;
        console.error('=== ПОМИЛКА ВХОДУ ===');
        console.error('Код помилки:', error.code);
        console.error('Повідомлення:', error.message);
        console.error('Повна помилка:', error);
        console.error('===================');
        
        let errorMessage = 'Помилка входу';
        // Не показуємо помилку для cancelled-popup-request, оскільки це не критична помилка
        if (error.code === 'auth/cancelled-popup-request') {
          console.log('Popup авторизації скасовано (можливо, вже відкритий інший)');
          return; // Просто виходимо, не показуючи помилку
        } else if (error.code === 'auth/popup-blocked') {
          errorMessage = 'Дозвольте спливаючі вікна для цього сайту';
        } else if (error.code === 'auth/popup-closed-by-user') {
          errorMessage = 'Вікно входу закрито';
          return; // Не показуємо помилку, якщо користувач сам закрив вікно
        } else if (error.code === 'auth/unauthorized-domain') {
          errorMessage = 'Домен не авторизований. Додайте домен у Firebase Console → Authentication → Settings → Authorized domains';
        } else if (error.code === 'auth/operation-not-allowed') {
          errorMessage = 'Google Sign-In не увімкнено. Увімкніть у Firebase Console';
        } else if (error.code === 'auth/network-request-failed') {
          errorMessage = 'Помилка мережі. Перевірте інтернет';
        } else {
          errorMessage = `Помилка: ${error.code} - ${error.message}`;
        }
        
        showToast(errorMessage);
      });
  } catch (error) {
    console.error('Критична помилка:', error);
    showToast('Помилка ініціалізації Firebase');
  }
}

function signOut() {
  // Видаляємо real-time listener перед виходом
  if (trainingsListener) {
    trainingsListener();
    trainingsListener = null;
  }
  if (sportDefinitionsListener) {
    sportDefinitionsListener();
    sportDefinitionsListener = null;
  }

  auth.signOut()
    .then(() => {
      showToast('Вихід виконано');
      trainings = [];
    })
    .catch((error) => {
      console.error('Помилка виходу:', error);
    });
}

function showAuthScreen() {
  const authScreen = document.getElementById('auth-screen');
  const mainScreen = document.getElementById('main-screen');
  if (authScreen) {
    authScreen.classList.add('active');
    authScreen.style.display = 'flex';
  }
  if (mainScreen) {
    mainScreen.classList.remove('active');
    mainScreen.style.display = 'none';
  }
}

function showMainScreen() {
  const authScreen = document.getElementById('auth-screen');
  const mainScreen = document.getElementById('main-screen');
  if (authScreen) {
    authScreen.classList.remove('active');
    authScreen.style.display = 'none';
  }
  if (mainScreen) {
    mainScreen.classList.add('active');
    mainScreen.style.display = 'flex';
  }
  
  // Відновлюємо збережену вкладку після показу головного екрану
  const savedTab = localStorage.getItem('sportTrackerActiveTab') || 'list';
  setTimeout(() => {
    switchTab(savedTab);
  }, 50); // Невелика затримка, щоб DOM точно був готовий
}

// Навігація
function initNavigation() {
  const navButtons = document.querySelectorAll('.nav-btn');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.dataset.tab;
      switchTab(tab);
    });
  });

  // Фільтри статистики - використовуємо делегування подій для динамічних елементів
  document.addEventListener('click', (e) => {
    if (e.target.classList.contains('filter-btn') || e.target.closest('.filter-btn')) {
      const btn = e.target.classList.contains('filter-btn') ? e.target : e.target.closest('.filter-btn');
      const period = btn.dataset.period;
      if (period) {
        setPeriodFilter(period);
        // Оновлюємо активну кнопку
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        // Оновлюємо статистику
        updateStats();
      }
    }
  });

  // Кнопки навігації по періодах
  const prevPeriodBtn = document.getElementById('prev-period-btn');
  const nextPeriodBtn = document.getElementById('next-period-btn');
  
  if (prevPeriodBtn) {
    prevPeriodBtn.addEventListener('click', () => {
      navigatePeriod(-1);
      updateNavButtons();
    });
  }
  
  if (nextPeriodBtn) {
    nextPeriodBtn.addEventListener('click', () => {
      navigatePeriod(1);
      updateNavButtons();
    });
  }

  // Ініціалізація свайпу між вкладками
  initSwipeNavigation();
}

// Функція для оновлення стану кнопок навігації (блокування, якщо досягнуто поточної дати)
function updateNavButtons() {
  const now = new Date();
  const today = new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate()));
  const nextPeriodBtn = document.getElementById('next-period-btn');
  
  if (nextPeriodBtn && statsToDate) {
    const toDateUTC = new Date(Date.UTC(
      statsToDate.getUTCFullYear(),
      statsToDate.getUTCMonth(),
      statsToDate.getUTCDate()
    ));
    const todayUTC = new Date(Date.UTC(
      today.getUTCFullYear(),
      today.getUTCMonth(),
      today.getUTCDate()
    ));
    
    // Блокуємо кнопку "вперед", якщо досягнуто сьогоднішньої дати
    if (toDateUTC.getTime() >= todayUTC.getTime()) {
      nextPeriodBtn.disabled = true;
      nextPeriodBtn.style.opacity = '0.5';
      nextPeriodBtn.style.cursor = 'not-allowed';
    } else {
      nextPeriodBtn.disabled = false;
      nextPeriodBtn.style.opacity = '1';
      nextPeriodBtn.style.cursor = 'pointer';
    }
  }
}

function switchTab(tabName) {
  // Зберігаємо поточну вкладку в localStorage
  localStorage.setItem('sportTrackerActiveTab', tabName);
  
  // Оновлюємо кнопки навігації
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tabName);
  });

  // Оновлюємо контейнер вкладок для анімації
  const tabsContainer = document.getElementById('tabs-container');
  if (tabsContainer) {
    tabsContainer.setAttribute('data-current-tab', tabName);
  }

  // Показуємо відповідну вкладку
  document.querySelectorAll('.tab-content').forEach(tab => {
    tab.classList.toggle('active', tab.id === `${tabName}-tab`);
  });

  // Оновлюємо статистику, якщо відкрили вкладку статистики
  if (tabName === 'stats') {
    // Встановлюємо "Тиждень" як активний фільтр за замовчуванням
    if (!currentPeriodFilter) {
      currentPeriodFilter = 'week';
      // Оновлюємо активну кнопку
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.period === 'week');
      });
    }
    // Оновлюємо статистику з невеликою затримкою, щоб переконатися, що DOM готовий
    setTimeout(() => {
      if (trainings && trainings.length > 0) {
        updateStats();
      }
    }, 100);
  }
  // Оновлюємо список тренувань, якщо відкрили вкладку списку
  if (tabName === 'list') {
    renderTrainings();
  }
}

// Функція для отримання поточної активної вкладки
function getCurrentTab() {
  const activeTab = document.querySelector('.tab-content.active');
  if (activeTab) {
    if (activeTab.id === 'list-tab') return 'list';
    if (activeTab.id === 'add-tab') return 'add';
    if (activeTab.id === 'stats-tab') return 'stats';
  }
  return 'list';
}

// Функція для отримання наступної/попередньої вкладки
function getTabOrder() {
  return ['list', 'add', 'stats'];
}

// Ініціалізація свайпу між вкладками
function initSwipeNavigation() {
  const tabs = ['list', 'add', 'stats'];
  let touchStartX = 0;
  let touchEndX = 0;
  const minSwipeDistance = 50; // Мінімальна відстань для свайпу (px)

  const mainScreen = document.getElementById('main-screen');
  if (!mainScreen) return;

  mainScreen.addEventListener('touchstart', (e) => {
    touchStartX = e.changedTouches[0].screenX;
  }, { passive: true });

  mainScreen.addEventListener('touchend', (e) => {
    touchEndX = e.changedTouches[0].screenX;
    handleSwipe();
  }, { passive: true });

  function handleSwipe() {
    const swipeDistance = touchEndX - touchStartX;
    const currentTab = getCurrentTab();
    const tabOrder = getTabOrder();
    const currentIndex = tabOrder.indexOf(currentTab);

    // Свайп вліво (наступна вкладка)
    if (swipeDistance < -minSwipeDistance && currentIndex < tabOrder.length - 1) {
      const nextTab = tabOrder[currentIndex + 1];
      switchTab(nextTab);
    }
    // Свайп вправо (попередня вкладка)
    else if (swipeDistance > minSwipeDistance && currentIndex > 0) {
      const prevTab = tabOrder[currentIndex - 1];
      switchTab(prevTab);
    }
  }
}

// Фільтр періоду для статистики
function setPeriodFilter(period) {
  currentPeriodFilter = period;
  // Скидаємо дати при виборі нового періоду - встановлюємо поточний період
  statsFromDate = null;
  statsToDate = null;
  updateStats();
}

// Функція для навігації по періодах (direction: -1 для попереднього, +1 для наступного)
function navigatePeriod(direction) {
  if (!currentPeriodFilter) return;
  
  const now = new Date();
  const today = new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate()));
  
  // Якщо дати не встановлені, встановлюємо поточний період
  if (!statsFromDate || !statsToDate) {
    const result = getFilteredTrainings();
    statsFromDate = result.startDate;
    statsToDate = result.today;
  }
  
  let newFromDate, newToDate;
  
  switch (currentPeriodFilter) {
    case 'week': {
      const daysToAdd = direction * 7;
      newFromDate = new Date(statsFromDate);
      newFromDate.setUTCDate(newFromDate.getUTCDate() + daysToAdd);
      
      // Якщо це попередній тиждень (direction < 0) - від понеділка до неділі
      // Якщо це поточний тиждень (direction >= 0) - від понеділка до сьогодні
      if (direction < 0) {
        // Попередні тижні: завжди від понеділка до неділі (7 днів)
        newToDate = new Date(newFromDate);
        newToDate.setUTCDate(newToDate.getUTCDate() + 6);
      } else {
        // Поточний тиждень: від понеділка до сьогодні
        const endOfWeek = new Date(newFromDate);
        endOfWeek.setUTCDate(endOfWeek.getUTCDate() + 6);
        newToDate = endOfWeek > today ? new Date(today) : endOfWeek;
        // Переконаємося, що початок тижня - понеділок
        if (newToDate <= today) {
          const dayOfWeek = newToDate.getUTCDay();
          const diff = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
          newFromDate = new Date(newToDate);
          newFromDate.setUTCDate(newFromDate.getUTCDate() - diff);
        }
      }
      break;
    }
    case 'month': {
      newFromDate = new Date(statsFromDate);
      newToDate = new Date(statsToDate);
      
      if (direction > 0) {
        newFromDate.setUTCMonth(newFromDate.getUTCMonth() + 1);
        newFromDate.setUTCDate(1);
        newToDate = new Date(newFromDate);
        newToDate.setUTCMonth(newToDate.getUTCMonth() + 1);
        newToDate.setUTCDate(0); // Останній день попереднього місяця
      } else {
        newFromDate.setUTCMonth(newFromDate.getUTCMonth() - 1);
        newFromDate.setUTCDate(1);
        newToDate = new Date(newFromDate);
        newToDate.setUTCMonth(newToDate.getUTCMonth() + 1);
        newToDate.setUTCDate(0); // Останній день поточного місяця
      }
      
      // Переконаємося, що не виходимо за межі сьогодні
      if (newToDate > today) {
        newToDate = new Date(today);
      }
      break;
    }
    case 'year': {
      newFromDate = new Date(statsFromDate);
      newToDate = new Date(statsToDate);
      
      if (direction > 0) {
        newFromDate.setUTCFullYear(newFromDate.getUTCFullYear() + 1);
        newFromDate.setUTCMonth(0);
        newFromDate.setUTCDate(1);
        newToDate = new Date(newFromDate);
        newToDate.setUTCFullYear(newToDate.getUTCFullYear() + 1);
        newToDate.setUTCMonth(0);
        newToDate.setUTCDate(0); // Останній день попереднього року
      } else {
        newFromDate.setUTCFullYear(newFromDate.getUTCFullYear() - 1);
        newFromDate.setUTCMonth(0);
        newFromDate.setUTCDate(1);
        newToDate = new Date(newFromDate);
        newToDate.setUTCFullYear(newToDate.getUTCFullYear() + 1);
        newToDate.setUTCMonth(0);
        newToDate.setUTCDate(0); // Останній день поточного року
      }
      
      // Переконаємося, що не виходимо за межі сьогодні
      if (newToDate > today) {
        newToDate = new Date(today);
      }
      break;
    }
  }
  
  statsFromDate = newFromDate;
  statsToDate = newToDate;
  updateStats();
}

// Функція для перевірки та оновлення статистики
function ensureStatsUpdated() {
  // Перевіряємо, чи є тренування та чи оновлена статистика
  if (trainings.length > 0) {
    updateStats();
  }
}

function getFilteredTrainings() {
  // Використовуємо UTC для уникнення проблем з часовим поясом
  const now = new Date();
  const today = new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate()));
  let startDate, endDate;

  // Якщо дати встановлені вручну (через навігацію), використовуємо їх
  if (statsFromDate && statsToDate) {
    startDate = new Date(statsFromDate);
    endDate = new Date(statsToDate);
  } else if (!currentPeriodFilter) {
    // Якщо фільтр не встановлено, використовуємо початок року
    startDate = new Date(Date.UTC(today.getUTCFullYear(), 0, 1));
    endDate = today;
  } else {
    switch (currentPeriodFilter) {
      case 'week':
        const dayOfWeek = today.getUTCDay();
        const diff = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // Понеділок = 0
        startDate = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate() - diff));
        // Поточний тиждень: від понеділка до сьогодні (не до неділі)
        endDate = new Date(today);
        break;
      case 'month':
        startDate = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), 1));
        endDate = today;
        break;
      case 'year':
        startDate = new Date(Date.UTC(today.getUTCFullYear(), 0, 1));
        endDate = today;
        break;
      default:
        startDate = new Date(Date.UTC(today.getUTCFullYear(), 0, 1));
        endDate = today;
    }
  }

  // Обчислюємо epochDay в UTC (як в Android)
  const startEpochDay = Math.floor(startDate.getTime() / 86400000);
  const endEpochDay = Math.floor(endDate.getTime() / 86400000);

  // Перевіряємо, чи trainings існує і є масивом
  if (!trainings || !Array.isArray(trainings)) {
    return { filtered: [], startDate, today };
  }

  const filtered = trainings.filter(t => {
    const epochDay = typeof t.dateEpochDay === 'number' ? t.dateEpochDay : parseInt(t.dateEpochDay);
    if (isNaN(epochDay)) {
      return false;
    }
    return epochDay >= startEpochDay && epochDay <= endEpochDay;
  });
  
  return { filtered, startDate, today: endDate };
}

// Real-time listener для тренувань (замінює loadTrainings)
function initTrainingsListener() {
  if (!currentUser) return;

  // Видаляємо попередній listener, якщо він існує
  if (trainingsListener) {
    trainingsListener();
    trainingsListener = null;
  }

  // Підписуємося на real-time зміни в Firestore
  try {
    trainingsListener = db.collection('users').doc(currentUser.uid).collection('trainings')
      .orderBy('dateEpochDay', 'desc')
      .onSnapshot(
        {
          includeMetadataChanges: false // Не отримуємо мета-зміни для оптимізації
        },
        (snapshot) => {
          // Перевіряємо, чи є помилки в snapshot
          if (snapshot.metadata.hasPendingWrites && snapshot.metadata.fromCache) {
            console.warn('Отримано дані з кешу, очікуємо серверні дані');
          }
          
          const oldTrainingsCount = trainings.length;
          trainings = [];
          snapshot.forEach((doc) => {
            const data = doc.data();
            // Переконуємося, що dateEpochDay є числом
            if (data && data.dateEpochDay !== undefined) {
              trainings.push({ 
                id: doc.id, 
                ...data,
                dateEpochDay: typeof data.dateEpochDay === 'number' ? data.dateEpochDay : parseInt(data.dateEpochDay)
              });
            }
          });
          
          const newCount = trainings.length - oldTrainingsCount;
          console.log(`Real-time listener: було ${oldTrainingsCount}, стало ${trainings.length}, нових: ${newCount}`);
          
          if (newCount > 0) {
            console.log('Нові тренування:', trainings.slice(-newCount).map(t => `${t.sport} ${t.dateText}`));
          }
          
          renderTrainings();
          // Оновлюємо статистику після оновлення тренувань
          setTimeout(() => {
            if (trainings && trainings.length > 0) {
              updateStats();
            }
          }, 100);
        },
        (error) => {
          console.error('Помилка real-time listener:', error);
          console.error('Код помилки:', error.code);
          console.error('Повідомлення:', error.message);
          
          // Показуємо користувачу зрозуміле повідомлення
          if (error.code === 'permission-denied') {
            showToast('Помилка доступу. Перевірте правила Firestore.');
          } else if (error.code === 'failed-precondition') {
            // Це означає, що потрібен індекс
            console.warn('Потрібен індекс для orderBy. Використовуємо запит без orderBy як fallback.');
            // Спробуємо без orderBy як fallback
            initTrainingsListenerFallback();
          } else if (error.code === 'unavailable') {
            showToast('Firestore недоступний. Перевірте інтернет.');
          } else if (error.code === 'invalid-argument') {
            console.warn('Помилка конфігурації Firestore:', error.message);
            // Не показуємо toast для технічних помилок
          } else {
            showToast('Помилка синхронізації');
          }
        }
      );
  } catch (e) {
    console.error('Критична помилка при ініціалізації listener:', e);
  }
}

// Fallback метод без orderBy (якщо індекс не налаштовано)
function initTrainingsListenerFallback() {
  if (!currentUser) return;
  
  // Видаляємо попередній listener, якщо він існує
  if (trainingsListener) {
    trainingsListener();
    trainingsListener = null;
  }
  
  // Використовуємо простий запит без orderBy
  trainingsListener = db.collection('users').doc(currentUser.uid).collection('trainings')
    .onSnapshot((snapshot) => {
      trainings = [];
      snapshot.forEach((doc) => {
        const data = doc.data();
        if (data && data.dateEpochDay !== undefined) {
          trainings.push({ 
            id: doc.id, 
            ...data,
            dateEpochDay: typeof data.dateEpochDay === 'number' ? data.dateEpochDay : parseInt(data.dateEpochDay)
          });
        }
      });
      // Сортуємо вручну
      trainings.sort((a, b) => (b.dateEpochDay || 0) - (a.dateEpochDay || 0));
      console.log('Оновлено тренувань (real-time, fallback):', trainings.length);
      renderTrainings();
      setTimeout(() => {
        if (trainings && trainings.length > 0) {
          updateStats();
        }
      }, 100);
    }, (error) => {
      console.error('Помилка fallback listener:', error);
      if (error.code === 'permission-denied') {
        showToast('Помилка доступу. Перевірте правила Firestore.');
      }
    });
}

// Завантаження тренувань (використовується тільки для fallback)
function loadTrainings() {
  // Тепер використовуємо real-time listener
  initTrainingsListener();
}

  // Функція для оновлення тексту періоду (видалено, бо період не відображається на вкладці "Список")
function updatePeriodText() {
  // Період більше не відображається на вкладці "Список"
}

  // Ініціалізація фільтрів списку
function initListFilters() {
  // Встановлюємо початкові дати (поточний місяць)
  const today = new Date();
  const firstDay = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), 1));
  const lastDay = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate()));
  
  listFromDate = firstDay;
  listToDate = lastDay;
  
  // Встановлюємо значення в input
  const fromInput = document.getElementById('from-date-input');
  const toInput = document.getElementById('to-date-input');
  const fromDisplay = document.getElementById('from-date-display');
  const toDisplay = document.getElementById('to-date-display');
  
  if (fromInput && toInput && fromDisplay && toDisplay) {
    fromInput.value = formatDateForInput(firstDay);
    toInput.value = formatDateForInput(lastDay);
    fromDisplay.textContent = `Від: ${formatDateUTC(firstDay)}`;
    toDisplay.textContent = `До: ${formatDateUTC(lastDay)}`;
    
    // Оновлюємо текст періоду при ініціалізації
    updatePeriodText();
    
    // Перевіряємо, чи завантажився Flatpickr
    if (typeof flatpickr === 'undefined') {
      console.error('Flatpickr не завантажено!');
      return;
    }
    
    // Ініціалізація Flatpickr для "Від"
    let fromPicker;
    try {
      fromPicker = flatpickr(fromInput, {
        dateFormat: 'Y-m-d',
        locale: 'uk',
        maxDate: new Date(),
        defaultDate: formatDateForInput(firstDay),
        allowInput: false,
        clickOpens: false, // Не відкривається при кліку на input
        appendTo: document.body, // Додаємо до body для коректного відображення
        onChange: function(selectedDates, dateStr) {
          if (selectedDates.length > 0) {
            // Використовуємо UTC компоненти для правильної обробки дати
            const selectedDate = selectedDates[0];
            const newDate = new Date(Date.UTC(
              selectedDate.getFullYear(),
              selectedDate.getMonth(),
              selectedDate.getDate()
            ));
            listFromDate = newDate;
            // Оновлюємо відображення дати (отримуємо актуальний елемент)
            const fromDisplayEl = document.getElementById('from-date-display');
            if (fromDisplayEl) {
              fromDisplayEl.textContent = `Від: ${formatDateUTC(newDate)}`;
            }
            if (listFromDate > listToDate) {
              listToDate = newDate;
              if (toPicker) {
                toPicker.setDate(newDate, false);
              }
              const toDisplayEl = document.getElementById('to-date-display');
              if (toDisplayEl) {
                toDisplayEl.textContent = `До: ${formatDateUTC(newDate)}`;
              }
            }
            updatePeriodText();
            renderTrainings();
          }
        }
      });
    } catch (e) {
      console.error('Помилка ініціалізації Flatpickr для "Від":', e);
    }
    
    // Ініціалізація Flatpickr для "До"
    let toPicker;
    try {
      toPicker = flatpickr(toInput, {
        dateFormat: 'Y-m-d',
        locale: 'uk',
        maxDate: new Date(),
        defaultDate: formatDateForInput(lastDay),
        allowInput: false,
        clickOpens: false, // Не відкривається при кліку на input
        appendTo: document.body, // Додаємо до body для коректного відображення
        onChange: function(selectedDates, dateStr) {
          if (selectedDates.length > 0) {
            // Використовуємо UTC компоненти для правильної обробки дати
            const selectedDate = selectedDates[0];
            const newDate = new Date(Date.UTC(
              selectedDate.getFullYear(),
              selectedDate.getMonth(),
              selectedDate.getDate()
            ));
            listToDate = newDate;
            // Оновлюємо відображення дати (отримуємо актуальний елемент)
            const toDisplayEl = document.getElementById('to-date-display');
            if (toDisplayEl) {
              toDisplayEl.textContent = `До: ${formatDateUTC(newDate)}`;
            }
            if (listFromDate > listToDate) {
              listFromDate = newDate;
              if (fromPicker) {
                fromPicker.setDate(newDate, false);
              }
              const fromDisplayEl = document.getElementById('from-date-display');
              if (fromDisplayEl) {
                fromDisplayEl.textContent = `Від: ${formatDateUTC(newDate)}`;
              }
            }
            updatePeriodText();
            renderTrainings();
          }
        }
      });
    } catch (e) {
      console.error('Помилка ініціалізації Flatpickr для "До":', e);
    }
    
    // Обробники кліків на кнопки
    const fromDateBtn = document.getElementById('from-date-btn');
    const toDateBtn = document.getElementById('to-date-btn');
    
    if (fromDateBtn && fromPicker) {
      // Видаляємо всі існуючі обробники через заміну
      fromDateBtn.onclick = null;
      fromDateBtn.replaceWith(fromDateBtn.cloneNode(true));
      const newFromBtn = document.getElementById('from-date-btn');
      
      newFromBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('Відкриваємо календар для "Від"', fromPicker);
        if (fromPicker) {
          fromPicker.open();
        } else {
          console.error('fromPicker не ініціалізований');
        }
        return false;
      }, true);
    }
    
    if (toDateBtn && toPicker) {
      // Видаляємо всі існуючі обробники через заміну
      toDateBtn.onclick = null;
      toDateBtn.replaceWith(toDateBtn.cloneNode(true));
      const newToBtn = document.getElementById('to-date-btn');
      
      newToBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('Відкриваємо календар для "До"', toPicker);
        if (toPicker) {
          toPicker.open();
        } else {
          console.error('toPicker не ініціалізований');
        }
        return false;
      }, true);
    }
  }
}

// Функція для форматування дати для input (YYYY-MM-DD)
function formatDateForInput(date) {
  const year = date.getUTCFullYear();
  const month = String(date.getUTCMonth() + 1).padStart(2, '0');
  const day = String(date.getUTCDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function renderTrainings() {
  const list = document.getElementById('trainings-list');
  const countElement = document.getElementById('list-count');
  
  // Оновлюємо текст періоду
  updatePeriodText();
  
  if (trainings.length === 0) {
    list.innerHTML = '<p class="empty-state">Немає тренувань</p>';
    if (countElement) countElement.textContent = '';
    return;
  }

  // Фільтруємо тренування
  let filtered = trainings;
  
  // Фільтр по періоду
  if (listFromDate && listToDate) {
    const fromEpochDay = Math.floor(listFromDate.getTime() / 86400000);
    const toEpochDay = Math.floor(listToDate.getTime() / 86400000);
    
    filtered = filtered.filter(t => {
      const epochDay = typeof t.dateEpochDay === 'number' ? t.dateEpochDay : parseInt(t.dateEpochDay);
      return !isNaN(epochDay) && epochDay >= fromEpochDay && epochDay <= toEpochDay;
    });
  }
  
  // Фільтр по виду спорту
  if (listSportFilter) {
    filtered = filtered.filter(t => t.sport === listSportFilter);
  }
  
  // Оновлюємо кількість
  if (countElement) {
    if (filtered.length === 0) {
      countElement.innerHTML = '<p class="empty-state">Немає тренувань у вибраному фільтрі.</p>';
      list.innerHTML = '';
      return;
    } else {
      countElement.textContent = `Кількість: ${filtered.length}`;
    }
  }

  list.innerHTML = filtered.map(training => {
    const sport = sportDisplayInfo(training.sport);
    const date = new Date(training.dateEpochDay * 86400000).toLocaleDateString('uk-UA');
    const borderColor = sport.color;
    const bgColor = hexToRgba(sport.color, 0.12);
    const badgeBgColor = hexToRgba(sport.color, 0.25);
    
    return `
      <div class="training-card" data-id="${training.id}" style="border: 0.5px solid ${borderColor}40; background-color: ${bgColor};">
        <div class="training-emoji" style="background-color: ${badgeBgColor};">${sport.emoji}</div>
        <div class="training-info">
          <div class="training-name">${sport.name}</div>
          <div class="training-date">${training.dateText || date}</div>
        </div>
        <button class="btn-delete material-icons" onclick="deleteTraining('${training.id}')" style="color: ${borderColor};">delete_outline</button>
      </div>
    `;
  }).join('') + '<div class="list-spacer"></div>';
}

// Допоміжна функція для конвертації hex в rgba
function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Додавання тренування
function initForms() {
  document.getElementById('save-training-btn').addEventListener('click', saveTraining);

  // Ініціалізація кнопки вибору дати
  const datePickerBtn = document.getElementById('date-picker-btn');
  const dateInput = document.getElementById('date-input');
  const dateDisplay = document.getElementById('date-display');
  
  if (datePickerBtn && dateInput && dateDisplay) {
    // Встановлюємо сьогоднішню дату за замовчуванням (UTC)
    const today = new Date();
    const todayISO = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate())).toISOString().split('T')[0];
    dateInput.value = todayISO;
    dateDisplay.textContent = `Дата: ${formatDateUTC(new Date(todayISO + 'T00:00:00Z'))}`;
    
    // Перевіряємо, чи завантажився Flatpickr
    if (typeof flatpickr === 'undefined') {
      console.error('Flatpickr не завантажено!');
      return;
    }
    
    // Ініціалізація Flatpickr для "Додати"
    try {
      window.addScreenDatePicker = flatpickr(dateInput, {
        dateFormat: 'Y-m-d',
        locale: 'uk',
        maxDate: new Date(),
        defaultDate: todayISO,
        allowInput: false,
        clickOpens: false, // Не відкривається при кліку на input
        appendTo: document.body, // Додаємо до body для коректного відображення
        onChange: function(selectedDates, dateStr) {
          if (selectedDates.length > 0) {
            // Використовуємо UTC компоненти для правильної обробки дати
            const selectedDate = selectedDates[0];
            const newDate = new Date(Date.UTC(
              selectedDate.getFullYear(),
              selectedDate.getMonth(),
              selectedDate.getDate()
            ));
            // Оновлюємо відображення дати (отримуємо актуальний елемент)
            const dateDisplayEl = document.getElementById('date-display');
            if (dateDisplayEl) {
              dateDisplayEl.textContent = `Дата: ${formatDateUTC(newDate)}`;
            }
          }
        }
      });
      
      // При кліку на кнопку відкриваємо date picker
      // Видаляємо всі існуючі обробники через заміну
      datePickerBtn.onclick = null;
      datePickerBtn.replaceWith(datePickerBtn.cloneNode(true));
      const newDateBtn = document.getElementById('date-picker-btn');
      
      newDateBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('Відкриваємо календар для "Дата"', window.addScreenDatePicker);
        if (window.addScreenDatePicker) {
          window.addScreenDatePicker.open();
        } else {
          console.error('addScreenDatePicker не ініціалізований');
        }
        return false;
      }, true);
    } catch (e) {
      console.error('Помилка ініціалізації Flatpickr для "Додати":', e);
    }
  }
}

function saveTraining() {
  const sport = selectedSport;
  const dateInput = document.getElementById('date-input').value;

  if (!sport) {
    showToast('Оберіть вид спорту');
    return;
  }

  if (!dateInput) {
    showToast('Оберіть дату');
    return;
  }

  // Використовуємо UTC для уникнення проблем з часовим поясом (як в Android)
  const date = new Date(dateInput + 'T00:00:00Z'); // Додаємо час в UTC
  const dateEpochDay = Math.floor(date.getTime() / 86400000);
  const dateText = formatDateUTC(date);

  const training = {
    sport,
    dateEpochDay,
    dateText
  };

  db.collection('users').doc(currentUser.uid).collection('trainings')
    .add(training)
    .then(() => {
      showToast('Збережено ✅');
      // Скидаємо вибір виду спорту
      selectedSport = null;
      const sportSelectText = document.getElementById('sport-select-text');
      if (sportSelectText) {
        sportSelectText.textContent = 'Оберіть вид спорту';
      }
      // Скидаємо активний елемент
      const sportSelectItems = document.querySelectorAll('#sport-select-dropdown .sport-filter-item');
      sportSelectItems.forEach(i => i.classList.remove('active'));
      const placeholderItem = document.querySelector('#sport-select-dropdown .sport-filter-item[data-sport=""]');
      if (placeholderItem) {
        placeholderItem.classList.add('active');
      }
      // Скидаємо дату на сьогодні
      const today = new Date();
      const todayISO = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate())).toISOString().split('T')[0];
      const dateInput = document.getElementById('date-input');
      const dateDisplay = document.getElementById('date-display');
      if (dateInput && dateDisplay) {
        // Оновлюємо flatpickr якщо він ініціалізований
        if (window.addScreenDatePicker) {
          window.addScreenDatePicker.setDate(todayISO, false);
        }
        dateDisplay.textContent = `Дата: ${formatDateUTC(new Date(todayISO + 'T00:00:00Z'))}`;
      }
      // Дані оновляться автоматично через real-time listener
      switchTab('list');
    })
    .catch((error) => {
      console.error('Помилка збереження:', error);
      showToast('Помилка збереження');
    });
}

function deleteTraining(trainingId) {
  if (!confirm('Видалити тренування?')) return;

  db.collection('users').doc(currentUser.uid).collection('trainings')
    .doc(trainingId)
    .delete()
    .then(() => {
      showToast('Видалено ✅');
      // Дані оновляться автоматично через real-time listener
    })
    .catch((error) => {
      console.error('Помилка видалення:', error);
      showToast('Помилка видалення');
    });
}

// Статистика
function updateStats() {
  // Завжди оновлюємо період, навіть якщо немає тренувань
  const result = getFilteredTrainings();
  if (result && result.startDate && result.today) {
    const periodText = `${formatDateUTC(result.startDate)} — ${formatDateUTC(result.today)}`;
    const periodElement = document.getElementById('period-text');
    if (periodElement) {
      periodElement.textContent = periodText;
    }
    // Оновлюємо стан кнопок навігації
    updateNavButtons();
  }
  
  if (!trainings || trainings.length === 0) {
    const totalElement = document.getElementById('total-trainings');
    const statsElement = document.getElementById('stats-by-sport');
    if (totalElement) totalElement.textContent = '0';
    if (statsElement) statsElement.innerHTML = '';
    return;
  }
  
  if (!result || !result.filtered) {
    console.error('getFilteredTrainings повернув некоректний результат:', result);
    return;
  }
  
  const { filtered } = result;
  if (!filtered || !Array.isArray(filtered)) {
    console.error('filtered не є масивом:', filtered);
    return;
  }
  
  const total = filtered.length;
  
  // Оновлюємо загальну кількість
  document.getElementById('total-trainings').textContent = total;
  
  // Статистика по видах спорту
  const bySport = {};
  filtered.forEach(t => {
    if (t.sport) {
      bySport[t.sport] = (bySport[t.sport] || 0) + 1;
    }
  });

  const statsContainer = document.getElementById('stats-by-sport');
  if (!statsContainer) {
    return;
  }
  
  if (Object.keys(bySport).length === 0) {
    statsContainer.innerHTML = '';
    return;
  }

  // Створюємо картки для кожного виду спорту (сортуємо за кількістю)
  const cardsHtml = Object.entries(bySport)
    .sort((a, b) => b[1] - a[1])
    .map(([sport, count]) => {
      const sportInfo = sportDisplayInfo(sport);
      const badgeColor = sportInfo.color;
      const bgColor = hexToRgba(badgeColor, 0.15);
      const borderColor = hexToRgba(badgeColor, 0.35);
      const emojiBg = hexToRgba(badgeColor, 0.25);
      
      return `
        <div class="stat-card" style="border: 1px solid ${borderColor}; background: ${bgColor};">
          <div class="stat-emoji" style="background: ${emojiBg};">${sportInfo.emoji}</div>
          <div class="stat-info">
            <div class="stat-name">${sportInfo.name}</div>
          </div>
          <div class="stat-count">${count}</div>
        </div>
      `;
    })
    .join('');
  
  statsContainer.innerHTML = cardsHtml;
}

// Toast повідомлення
function showToast(message) {
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('show');
  }, 10);

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 2000);
}
