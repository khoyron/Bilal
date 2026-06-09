package org.khoyron.bilal.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Models ────────────────────────────────────────────────────────────────────

data class SurahUi(
    val number: Int,
    val nameLatn: String,      // "Al-Fatiha"
    val nameArabic: String,    // "الفاتحة"
    val translation: String,   // "THE OPENING"
    val totalAyah: Int,
    val isFavorite: Boolean = false
)

data class JuzUi(
    val number: Int,
    val nameLatn: String,      // "Juz' 1"
    val startSurah: String,    // "Al-Fatiha 1"
    val endSurah: String,      // "Al-Baqarah 141"
    val totalAyah: Int
)

data class LastReadUi(
    val surahName: String,
    val surahNumber: Int,
    val ayahNumber: Int
)

enum class QuranTab { SURAH, JUZ }

data class QuranUiState(
    val isLoading: Boolean = true,
    val selectedTab: QuranTab = QuranTab.SURAH,
    val surahList: List<SurahUi> = emptyList(),
    val juzList: List<JuzUi> = emptyList(),
    val lastRead: LastReadUi? = null,
    val searchQuery: String = "",
    val error: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class QuranViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulasi network delay — ganti dengan API call nanti
            // Contoh: val response = quranRepository.getSurahList()
            delay(1500)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    surahList = getDummySurahList(),
                    juzList = getDummyJuzList(),
                    lastRead = LastReadUi(surahName = "Al-Baqarah", surahNumber = 2, ayahNumber = 142)
                )
            }
        }
    }

    fun selectTab(tab: QuranTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite(surahNumber: Int) {
        _uiState.update { state ->
            state.copy(
                surahList = state.surahList.map { surah ->
                    if (surah.number == surahNumber) surah.copy(isFavorite = !surah.isFavorite)
                    else surah
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() { loadData() }

    // ── Dummy data — ganti dengan repository/API ──────────────────────────────

    private fun getDummySurahList(): List<SurahUi> = listOf(
        SurahUi(1,  "Al-Fatihah",    "الفاتحة",   "THE OPENING",          7),
        SurahUi(2,  "Al-Baqarah",    "البقرة",    "THE COW",              286),
        SurahUi(3,  "Ali 'Imran",    "آل عمران",  "FAMILY OF IMRAN",      200),
        SurahUi(4,  "An-Nisa'",      "النساء",    "THE WOMEN",            176),
        SurahUi(5,  "Al-Ma'idah",    "المائدة",   "THE TABLE SPREAD",     120),
        SurahUi(6,  "Al-An'am",      "الأنعام",   "THE CATTLE",           165),
        SurahUi(7,  "Al-A'raf",      "الأعراف",   "THE HEIGHTS",          206),
        SurahUi(8,  "Al-Anfal",      "الأنفal",   "THE SPOILS OF WAR",    75),
        SurahUi(9,  "At-Tawbah",     "التوبة",    "THE REPENTANCE",       129),
        SurahUi(10, "Yunus",         "يونس",      "JONAH",                109),
        SurahUi(11, "Hud",           "هود",       "HUD",                  123),
        SurahUi(12, "Yusuf",         "يوسف",      "JOSEPH",               111),
        SurahUi(13, "Ar-Ra'd",       "الرعد",     "THE THUNDER",          43),
        SurahUi(14, "Ibrahim",       "إبراهيم",   "ABRAHAM",              52),
        SurahUi(15, "Al-Hijr",       "الحجر",     "THE ROCKY TRACT",      99),
        SurahUi(16, "An-Nahl",       "النحل",     "THE BEE",              128),
        SurahUi(17, "Al-Isra'",      "الإسراء",   "THE NIGHT JOURNEY",    111),
        SurahUi(18, "Al-Kahf",       "الكهف",     "THE CAVE",             110),
        SurahUi(19, "Maryam",        "مريم",      "MARY",                 98),
        SurahUi(20, "Ta-Ha",         "طه",        "TA-HA",                135),
        SurahUi(21, "Al-Anbiya'",    "الأنبياء",  "THE PROPHETS",         112),
        SurahUi(22, "Al-Hajj",       "الحج",      "THE PILGRIMAGE",       78),
        SurahUi(23, "Al-Mu'minun",   "المؤمنون",  "THE BELIEVERS",        118),
        SurahUi(24, "An-Nur",        "النور",     "THE LIGHT",            64),
        SurahUi(25, "Al-Furqan",     "الفرقان",   "THE CRITERION",        77),
        SurahUi(26, "Ash-Shu'ara'",  "الشعراء",   "THE POETS",            227),
        SurahUi(27, "An-Naml",       "النمل",     "THE ANT",              93),
        SurahUi(28, "Al-Qasas",      "القصص",     "THE STORIES",          88),
        SurahUi(29, "Al-'Ankabut",   "العنكبوت",  "THE SPIDER",           69),
        SurahUi(30, "Ar-Rum",        "الروم",     "THE ROMANS",           60),
        SurahUi(31, "Luqman",        "لقمان",     "LUQMAN",               34),
        SurahUi(32, "As-Sajdah",     "السجدة",    "THE PROSTRATION",      30),
        SurahUi(33, "Al-Ahzab",      "الأحزاب",   "THE COMBINED FORCES",  73),
        SurahUi(34, "Saba'",         "سبأ",       "SHEBA",                54),
        SurahUi(35, "Fatir",         "فاطر",      "THE ORIGINATOR",       45),
        SurahUi(36, "Ya-Sin",        "يس",        "YA SIN",               83),
        SurahUi(37, "As-Saffat",     "الصافات",   "THOSE WHO SET RANKS",  182),
        SurahUi(38, "Sad",           "ص",         "THE LETTER SAD",       88),
        SurahUi(39, "Az-Zumar",      "الزمر",     "THE TROOPS",           75),
        SurahUi(40, "Ghafir",        "غافر",      "THE FORGIVER",         85),
        SurahUi(41, "Fussilat",      "فصلت",      "EXPLAINED IN DETAIL",  54),
        SurahUi(42, "Ash-Shura",     "الشورى",    "THE CONSULTATION",     53),
        SurahUi(43, "Az-Zukhruf",    "الزخرف",    "THE ORNAMENTS OF GOLD",89),
        SurahUi(44, "Ad-Dukhan",     "الدخان",    "THE SMOKE",            59),
        SurahUi(45, "Al-Jathiyah",   "الجاثية",   "THE CROUCHING",        37),
        SurahUi(46, "Al-Ahqaf",      "الأحقاف",   "THE WIND-CURVED SAND", 35),
        SurahUi(47, "Muhammad",      "محمد",      "MUHAMMAD",             38),
        SurahUi(48, "Al-Fath",       "الفتح",     "THE VICTORY",          29),
        SurahUi(49, "Al-Hujurat",    "الحجرات",   "THE ROOMS",            18),
        SurahUi(50, "Qaf",           "ق",         "THE LETTER QAF",       45),
        SurahUi(51, "Adh-Dhariyat",  "الذاريات",  "THE WINNOWING WINDS",  60),
        SurahUi(52, "At-Tur",        "الطور",     "THE MOUNT",            49),
        SurahUi(53, "An-Najm",       "النجم",     "THE STAR",             62),
        SurahUi(54, "Al-Qamar",      "القمر",     "THE MOON",             55),
        SurahUi(55, "Ar-Rahman",     "الرحمن",    "THE BENEFICENT",       78),
        SurahUi(56, "Al-Waqi'ah",    "الواقعة",   "THE INEVITABLE",       96),
        SurahUi(57, "Al-Hadid",      "الحديد",    "THE IRON",             29),
        SurahUi(58, "Al-Mujadila",   "المجادلة",  "THE PLEADING WOMAN",   22),
        SurahUi(59, "Al-Hashr",      "الحشر",     "THE EXILE",            24),
        SurahUi(60, "Al-Mumtahanah", "الممتحنة",  "SHE THAT IS TO BE EXAMINED", 13),
        SurahUi(61, "As-Saf",        "الصف",      "THE RANKS",            14),
        SurahUi(62, "Al-Jumu'ah",    "الجمعة",    "THE CONGREGATION",     11),
        SurahUi(63, "Al-Munafiqun",  "المنافقون", "THE HYPOCRITES",       11),
        SurahUi(64, "At-Taghabun",   "التغابن",   "THE MUTUAL DISILLUSION",18),
        SurahUi(65, "At-Talaq",      "الطلاق",    "THE DIVORCE",          12),
        SurahUi(66, "At-Tahrim",     "التحريم",   "THE PROHIBITION",      12),
        SurahUi(67, "Al-Mulk",       "الملك",     "THE SOVEREIGNTY",      30),
        SurahUi(68, "Al-Qalam",      "القلم",     "THE PEN",              52),
        SurahUi(69, "Al-Haqqah",     "الحاقة",    "THE REALITY",          52),
        SurahUi(70, "Al-Ma'arij",    "المعارج",   "THE ASCENDING STAIRWAYS",44),
        SurahUi(71, "Nuh",           "نوح",       "NOAH",                 28),
        SurahUi(72, "Al-Jinn",       "الجن",      "THE JIN",              28),
        SurahUi(73, "Al-Muzzammil",  "المزمل",    "THE ENSHROUDED ONE",   20),
        SurahUi(74, "Al-Muddaththir","المدثر",    "THE CLOAKED ONE",      56),
        SurahUi(75, "Al-Qiyamah",    "القيامة",   "THE RESURRECTION",     40),
        SurahUi(76, "Al-Insan",      "الإنسان",   "THE MAN",              31),
        SurahUi(77, "Al-Mursalat",   "المرسلات",  "THE EMISSARIES",       50),
        SurahUi(78, "An-Naba'",      "النبأ",     "THE ANNOUNCEMENT",     40),
        SurahUi(79, "An-Nazi'at",    "النازعات",  "THOSE WHO DRAG FORTH", 46),
        SurahUi(80, "'Abasa",        "عبس",       "HE FROWNED",           42),
        SurahUi(81, "At-Takwir",     "التكوير",   "THE OVERTHROWING",     29),
        SurahUi(82, "Al-Infitar",    "الانفطار",  "THE CLEAVING",         19),
        SurahUi(83, "Al-Mutaffifin", "المطففين",  "THE DEFRAUDING",       36),
        SurahUi(84, "Al-Inshiqaq",   "الانشقاق",  "THE SUNDERING",        25),
        SurahUi(85, "Al-Buruj",      "البروج",    "THE MANSIONS OF STARS",22),
        SurahUi(86, "At-Tariq",      "الطارق",    "THE MORNING STAR",     17),
        SurahUi(87, "Al-A'la",       "الأعلى",    "THE MOST HIGH",        19),
        SurahUi(88, "Al-Ghashiyah",  "الغashiyah", "THE OVERWHELMING",     26),
        SurahUi(89, "Al-Fajr",       "الفجر",     "THE DAWN",             30),
        SurahUi(90, "Al-Balad",      "البلد",     "THE CITY",             20),
        SurahUi(91, "Ash-Shams",     "الشمس",     "THE SUN",              15),
        SurahUi(92, "Al-Layl",       "الليل",     "THE NIGHT",            21),
        SurahUi(93, "Ad-Duha",       "الضحى",     "THE MORNING HOURS",    11),
        SurahUi(94, "Ash-Sharh",     "الشرح",     "THE RELIEF",           8),
        SurahUi(95, "At-Tin",        "التين",     "THE FIG",              8),
        SurahUi(96, "Al-'Alaq",      "العلق",     "THE CLOT",             19),
        SurahUi(97, "Al-Qadr",       "القدر",     "THE POWER",            5),
        SurahUi(98, "Al-Bayyinah",   "البينة",    "THE CLEAR PROOF",      8),
        SurahUi(99, "Az-Zalzalah",   "الزلزلة",   "THE EARTHQUAKE",       8),
        SurahUi(100,"Al-'Adiyat",    "العاديات",  "THE COURSER",          11),
        SurahUi(101,"Al-Qari'ah",    "القارعة",   "THE CALAMITY",         11),
        SurahUi(102,"At-Takathur",   "التكاثر",   "THE RIVALRY IN WORLD INCREASE", 8),
        SurahUi(103,"Al-'Asr",       "العصر",     "THE DECLINING DAY",    3),
        SurahUi(104,"Al-Humazah",    "الهمزة",    "THE TRADUCER",         9),
        SurahUi(105,"Al-Fil",        "الفيل",     "THE ELEPHANT",         5),
        SurahUi(106,"Quraysh",       "قريش",      "QURAYSH",              4),
        SurahUi(107,"Al-Ma'un",      "الماعون",   "THE SMALL KINDNESSES", 7),
        SurahUi(108,"Al-Kawthar",    "الkoathar",  "THE ABUNDANCE",        3),
        SurahUi(109,"Al-Kafirun",    "الكافرون",  "THE DISBELIEVERS",     6),
        SurahUi(110,"An-Nasr",       "النصر",     "THE DIVINE SUPPORT",   3),
        SurahUi(111,"Al-Masad",      "المسد",     "THE PALM FIBRE",       5),
        SurahUi(112,"Al-Ikhlas",     "الإخلاص",   "THE SINCERITY",        4),
        SurahUi(113,"Al-Falaq",      "الفلق",     "THE DAYBREAK",         5),
        SurahUi(114,"An-Nas",        "الناس",     "THE MANKIND",          6)
    )

    private fun getDummyJuzList(): List<JuzUi> = listOf(
        JuzUi(1,  "Juz' 1",  "Al-Fatihah 1",    "Al-Baqarah 141",    148),
        JuzUi(2,  "Juz' 2",  "Al-Baqarah 142",  "Al-Baqarah 252",    111),
        JuzUi(3,  "Juz' 3",  "Al-Baqarah 253",  "Ali 'Imran 92",     126),
        JuzUi(4,  "Juz' 4",  "Ali 'Imran 93",   "An-Nisa' 23",       132),
        JuzUi(5,  "Juz' 5",  "An-Nisa' 24",     "An-Nisa' 147",      124),
        JuzUi(6,  "Juz' 6",  "An-Nisa' 148",    "Al-Ma'idah 81",     111),
        JuzUi(7,  "Juz' 7",  "Al-Ma'idah 82",   "Al-An'am 110",      149),
        JuzUi(8,  "Juz' 8",  "Al-An'am 111",    "Al-A'raf 87",       148),
        JuzUi(9,  "Juz' 9",  "Al-A'raf 88",     "Al-Anfal 40",       159),
        JuzUi(10, "Juz' 10", "Al-Anfal 41",     "At-Tawbah 92",      137),
        JuzUi(11, "Juz' 11", "At-Tawbah 93",    "Hud 5",             151),
        JuzUi(12, "Juz' 12", "Hud 6",           "Yusuf 52",          170),
        JuzUi(13, "Juz' 13", "Yusuf 53",        "Ibrahim 52",        154),
        JuzUi(14, "Juz' 14", "Al-Hijr 1",       "An-Nahl 128",       227),
        JuzUi(15, "Juz' 15", "Al-Isra' 1",      "Al-Kahf 74",        185),
        JuzUi(16, "Juz' 16", "Al-Kahf 75",      "Ta-Ha 135",         179),
        JuzUi(17, "Juz' 17", "Al-Anbiya' 1",    "Al-Hajj 78",        190),
        JuzUi(18, "Juz' 18", "Al-Mu'minun 1",   "Al-Furqan 20",      202),
        JuzUi(19, "Juz' 19", "Al-Furqan 21",    "An-Naml 55",        194),
        JuzUi(20, "Juz' 20", "An-Naml 56",      "Al-'Ankabut 45",    171),
        JuzUi(21, "Juz' 21", "Al-'Ankabut 46",  "Al-Ahzab 30",       178),
        JuzUi(22, "Juz' 22", "Al-Ahzab 31",     "Ya-Sin 27",         160),
        JuzUi(23, "Juz' 23", "Ya-Sin 28",       "Az-Zumar 31",       173),
        JuzUi(24, "Juz' 24", "Az-Zumar 32",     "Fussilat 46",       153),
        JuzUi(25, "Juz' 25", "Fussilat 47",     "Al-Jathiyah 37",    147),
        JuzUi(26, "Juz' 26", "Al-Ahqaf 1",      "Adh-Dhariyat 30",   168),
        JuzUi(27, "Juz' 27", "Adh-Dhariyat 31", "Al-Hadid 29",       177),
        JuzUi(28, "Juz' 28", "Al-Mujadila 1",   "At-Tahrim 12",      137),
        JuzUi(29, "Juz' 29", "Al-Mulk 1",       "Al-Mursalat 50",    431),
        JuzUi(30, "Juz' 30", "An-Naba' 1",      "An-Nas 6",          564)
    )
}