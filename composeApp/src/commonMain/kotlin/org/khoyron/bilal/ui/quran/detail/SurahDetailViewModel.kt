package org.khoyron.bilal.ui.quran.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Models ────────────────────────────────────────────────────────────────────

data class AyahUi(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabic: String,
    val translation: String,
    val isBookmarked: Boolean = false
)

data class SurahDetailUi(
    val number: Int,
    val nameLatn: String,
    val nameArabic: String,
    val translation: String,
    val totalAyah: Int,
    val ayahs: List<AyahUi>
)

data class SurahDetailUiState(
    val isLoading: Boolean = true,
    val surah: SurahDetailUi? = null,
    val isPlaying: Boolean = false,
    val error: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SurahDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SurahDetailUiState())
    val uiState: StateFlow<SurahDetailUiState> = _uiState.asStateFlow()

    init {
        loadSurah()
    }

    fun loadSurah(surahNumber: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulasi network delay — ganti dengan API call nanti
            // Contoh: val response = quranRepository.getSurahDetail(surahNumber)
            delay(1500)

            val surah = when (surahNumber) {
                1    -> getDummyAlFatihah()
                else -> getDummyAlFatihah() // fallback dummy
            }

            _uiState.update {
                it.copy(isLoading = false, surah = surah)
            }
        }
    }

    fun togglePlayAudio() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun toggleBookmark(ayahNumber: Int) {
        _uiState.update { state ->
            state.copy(
                surah = state.surah?.copy(
                    ayahs = state.surah.ayahs.map { ayah ->
                        if (ayah.ayahNumber == ayahNumber)
                            ayah.copy(isBookmarked = !ayah.isBookmarked)
                        else ayah
                    }
                )
            )
        }
    }

    fun onShareAyah(ayah: AyahUi) {
        // TODO: implement share intent per platform
    }

    fun onPlayAyah(ayah: AyahUi) {
        // TODO: implement play single ayah audio
    }

    // ── Dummy data — ganti dengan repository/API ──────────────────────────────

    private fun getDummyAlFatihah(): SurahDetailUi {
        return SurahDetailUi(
            number = 1,
            nameLatn = "Al-Fatihah",
            nameArabic = "الفاتحة",
            translation = "THE OPENING",
            totalAyah = 7,
            ayahs = listOf(
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 1,
                    arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    translation = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 2,
                    arabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                    translation = "[All] praise is [due] to Allah, Lord of the worlds -"
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 3,
                    arabic = "الرَّحْمَٰنِ الرَّحِيمِ",
                    translation = "The Entirely Merciful, the Especially Merciful,"
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 4,
                    arabic = "مَالِكِ يَوْمِ الدِّينِ",
                    translation = "Sovereign of the Day of Recompense."
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 5,
                    arabic = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                    translation = "It is You we worship and You we ask for help."
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 6,
                    arabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                    translation = "Guide us to the straight path -"
                ),
                AyahUi(
                    surahNumber = 1,
                    ayahNumber = 7,
                    arabic = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                    translation = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray."
                )
            )
        )
    }
}