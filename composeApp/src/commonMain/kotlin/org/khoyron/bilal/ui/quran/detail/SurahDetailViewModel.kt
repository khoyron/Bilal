package org.khoyron.bilal.ui.quran.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.khoyron.bilal.domain.usecase.GetSurahDetailUseCase
import org.khoyron.bilal.domain.usecase.GetJuzDetailUseCase
import org.khoyron.bilal.util.AudioPlayer

// ── Models ────────────────────────────────────────────────────────────────────

data class AyahUi(
    val globalNumber: Int,
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
    val playingAyahNumber: Int? = null,
    val error: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SurahDetailViewModel(
    private val getSurahDetailUseCase: GetSurahDetailUseCase,
    private val getJuzDetailUseCase: GetJuzDetailUseCase,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurahDetailUiState())
    val uiState: StateFlow<SurahDetailUiState> = _uiState.asStateFlow()

    init {
        audioPlayer.setOnCompletionListener {
            _uiState.update { it.copy(isPlaying = false) }
        }
        audioPlayer.setOnUrlChangeListener { url ->
            if (url == null) return@setOnUrlChangeListener
            val globalNumber = url.substringAfterLast("/").substringBefore(".mp3").toIntOrNull()
            if (globalNumber != null) {
                _uiState.update { it.copy(playingAyahNumber = globalNumber) }
            }
        }
    }

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val surah = getSurahDetailUseCase(surahNumber)
                _uiState.update {
                    it.copy(isLoading = false, surah = surah)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load Surah")
                }
            }
        }
    }

    fun loadJuz(juzNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val juz = getJuzDetailUseCase(juzNumber)
                _uiState.update {
                    it.copy(isLoading = false, surah = juz)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load Juz")
                }
            }
        }
    }

    fun togglePlayAudio() {
        val currentSurah = _uiState.value.surah ?: return
        val isCurrentlyPlaying = _uiState.value.isPlaying

        if (isCurrentlyPlaying) {
            audioPlayer.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            if (audioPlayer.isPlaying()) {
                audioPlayer.resume()
                _uiState.update { it.copy(isPlaying = true) }
            } else {
                val urls = currentSurah.ayahs.map {
                    "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${it.globalNumber}.mp3"
                }
                audioPlayer.play(urls)
                _uiState.update { it.copy(isPlaying = true) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
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

    }

    fun onPlayAyah(ayah: AyahUi) {
        val isCurrentlyPlaying = _uiState.value.isPlaying
        val isSameAyah = _uiState.value.playingAyahNumber == ayah.globalNumber

        if (isCurrentlyPlaying && isSameAyah) {
            audioPlayer.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            val urls = listOf("https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.globalNumber}.mp3")
            audioPlayer.play(urls)
            _uiState.update { it.copy(isPlaying = true, playingAyahNumber = ayah.globalNumber) }
        }
    }
}
