package org.khoyron.bilal.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.khoyron.bilal.domain.usecase.GetSurahListUseCase

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

class QuranViewModel(
    private val getSurahListUseCase: GetSurahListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val surahs = getSurahListUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        surahList = surahs,
                        juzList = getNameJuzList(),
                        lastRead = LastReadUi(surahName = "Al-Baqarah", surahNumber = 2, ayahNumber = 142)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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

    private fun getNameJuzList(): List<JuzUi> = listOf(
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
