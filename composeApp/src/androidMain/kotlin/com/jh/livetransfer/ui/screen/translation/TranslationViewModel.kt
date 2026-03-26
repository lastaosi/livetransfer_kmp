package com.jh.livetransfer.ui.screen.translation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jh.livetransfer.data.repository.SettingsRepository
import com.jh.livetransfer.data.model.ChatMessage
import com.jh.livetransfer.data.model.SpeechSpeed
import com.jh.livetransfer.domain.repository.TranslationRepository
import com.jh.livetransfer.domain.usecase.translation.TranslateAudioStreamUseCase
import com.jh.livetransfer.domain.usecase.translation.TranslateImageUseCase
import com.jh.livetransfer.util.AudioUtil
import com.jh.livetransfer.util.L
import com.jh.livetransfer.util.LanguageDetector
import com.jh.livetransfer.util.TtsManager
import com.jh.livetransfer.util.VadEvent
import com.jh.livetransfer.util.VadProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private data class AppSettings(
    val langAName: String,
    val langBName: String,
    val langACode: String,
    val langBCode: String,
    val speedDelayMs: Long
)

class TranslationViewModel(
    private val vadProcessor: VadProcessor,
    private val translateAudioStreamUseCase: TranslateAudioStreamUseCase,
    private val translateImageUseCase: TranslateImageUseCase,
    private val ttsManager: TtsManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ──────────────────────────────────────────────
    // UI State
    // ──────────────────────────────────────────────
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _isImageLoading = MutableStateFlow(false)
    val isImageLoading: StateFlow<Boolean> = _isImageLoading.asStateFlow()

    // One-shot UI 이벤트 (Toast 등). extraBufferCapacity=1로 collector가 살짝 늦어도 유실 방지.
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _currentLangA = MutableStateFlow("한국어")
    val currentLangA = _currentLangA.asStateFlow()

    private val _currentLangB = MutableStateFlow("영어")
    val currentLangB = _currentLangB.asStateFlow()

    val currentVadSpeed: StateFlow<SpeechSpeed> = settingsRepository.speechSpeedDelay.map { delay ->
        when (delay) {
            600L -> SpeechSpeed.FAST
            1500L -> SpeechSpeed.SLOW
            else -> SpeechSpeed.NORMAL
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpeechSpeed.NORMAL)

    // ──────────────────────────────────────────────
    // Internal State
    // ──────────────────────────────────────────────
    private var recordingJob: Job? = null

    // [Fix ①] var → MutableStateFlow: update{}로 여러 청크 코루틴의 동시 append를 CAS로 직렬화
    private val sessionText = MutableStateFlow("")

    // [Fix ②] 청크 처리 코루틴들의 부모 Job. stopAudioCapture에서 모든 자식이 끝날 때까지 join.
    private var chunkScope: CoroutineScope? = null

    // 설정에서 읽어온 값 — combine으로 단일 구독
    private var langACode = "ko"
    private var langBCode = "en"
    private var vadSilenceDelay = 1500L

    companion object {
        private const val MAX_BAR_COUNT = 40
        private const val SILENCE_THRESHOLD = 0.05f
    }

    init {
        ttsManager.onSpeakingStateChanged = { isSpeaking ->
            _isTtsSpeaking.value = isSpeaking
        }

        viewModelScope.launch {
            combine(
                settingsRepository.langAName,
                settingsRepository.langBName,
                settingsRepository.langACode,
                settingsRepository.langBCode,
                settingsRepository.speechSpeedDelay
            ) { langAName, langBName, aCode, bCode, delay ->
                AppSettings(langAName, langBName, aCode, bCode, delay)
            }.collect { settings ->
                _currentLangA.value = settings.langAName
                _currentLangB.value = settings.langBName
                langACode = settings.langACode
                langBCode = settings.langBCode
                vadSilenceDelay = settings.speedDelayMs
            }
        }
    }

    // ──────────────────────────────────────────────
    // Public: Settings
    // ──────────────────────────────────────────────
    fun setLangA(name: String, code: String) {
        viewModelScope.launch { settingsRepository.updateLangA(code, name) }
    }

    fun setLangB(name: String, code: String) {
        viewModelScope.launch { settingsRepository.updateLangB(code, name) }
    }

    fun setSpeechSpeed(speed: SpeechSpeed) {
        viewModelScope.launch { settingsRepository.updateSpeechSpeed(speed.silenceDelay) }
    }

    // ──────────────────────────────────────────────
    // Public: Recording
    // ──────────────────────────────────────────────
    fun toggleRecording() {
        if (_isRecording.value) stopAudioCapture() else startAudioCapture()
    }

    fun stopTts() = ttsManager.stop()

    // ──────────────────────────────────────────────
    // Private: Audio Capture
    // ──────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    private fun startAudioCapture() {
        _isRecording.value = true
        _audioAmplitudes.value = emptyList()
        sessionText.value = ""

        // [Fix ②] 세션마다 새 chunkScope 생성.
        // SupervisorJob: 개별 청크 번역 실패가 다른 청크에 영향을 주지 않도록.
        chunkScope?.cancel()
        chunkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        recordingJob = viewModelScope.launch {
            vadProcessor.startCapture(SILENCE_THRESHOLD, vadSilenceDelay).collect { event ->
                when (event) {
                    is VadEvent.AmplitudeUpdate -> {
                        _audioAmplitudes.value =
                            (_audioAmplitudes.value + event.normalized).takeLast(MAX_BAR_COUNT)
                    }
                    is VadEvent.ChunkReady -> processVadChunk(event.pcmData)
                }
            }
        }
    }

    private fun stopAudioCapture() {
        if (!_isRecording.value) return
        _isRecording.value = false
        _audioAmplitudes.value = emptyList()
        vadProcessor.stopRecording()

        viewModelScope.launch {
            // VadProcessor Flow 완료 대기 (마지막 ChunkReady 포함)
            recordingJob?.join()

            // [Fix ②] delay(500) 제거 — chunkScope의 모든 자식 코루틴이 완료될 때까지 정확히 대기
            chunkScope?.coroutineContext?.get(Job)?.children?.toList()?.forEach { it.join() }
            chunkScope?.cancel()
            chunkScope = null

            val text = sessionText.value.trim()
            if (text.isNotBlank()) {
                val langCode = if (LanguageDetector.isLanguage(text, "ko")) "ko-KR"
                               else LanguageDetector.toFullLocale(langBCode)
                ttsManager.speak(text, langCode)
                sessionText.value = ""
            }
        }
    }

    // ──────────────────────────────────────────────
    // Private: Translation
    // ──────────────────────────────────────────────
    private fun processVadChunk(pcmData: ByteArray) {
        // [Fix ②] viewModelScope 대신 chunkScope에서 launch → 부모 Job으로 추적 가능
        chunkScope?.launch {
            val wavData = AudioUtil.addWavHeader(pcmData)
            var chunkResult = ""
            var resultLangCode = LanguageDetector.toFullLocale(langBCode)
            var isMyMessage = true

            // [Fix ③] lastIndex 대신 고정 인덱스 사용.
            // -1: 아직 메시지 미삽입. update{}로 삽입과 인덱스 캡처를 원자적으로 처리.
            var messageIndex = -1

            try {
                translateAudioStreamUseCase(wavData, _currentLangA.value, _currentLangB.value)
                .collect { chunk ->
                    chunkResult += chunk

                    // 첫 번째 청크에서 언어 판별 (messageIndex 미초기화 상태를 sentinel로 활용)
                    if (messageIndex == -1) {
                        val isOutputLangA = LanguageDetector.isLanguage(chunkResult, langACode)
                        isMyMessage = !isOutputLangA
                        resultLangCode = if (isOutputLangA) LanguageDetector.toFullLocale(langACode)
                                         else LanguageDetector.toFullLocale(langBCode)
                    }

                    val currentMessage = ChatMessage(chunkResult, isMyMessage, resultLangCode)
                    L.d(chunkResult)

                    if (messageIndex == -1) {
                        // [Fix ③] 메시지 추가와 인덱스 캡처를 update{} 안에서 원자적으로 수행.
                        // 다른 청크가 동시에 메시지를 추가해도 각자 고유한 인덱스를 가짐.
                        _chatMessages.update { list ->
                            messageIndex = list.size
                            list + currentMessage
                        }
                    } else {
                        // 스트리밍 갱신: 본인 인덱스만 업데이트
                        _chatMessages.update { list ->
                            if (messageIndex in list.indices) {
                                list.toMutableList().also { it[messageIndex] = currentMessage }
                            } else list
                        }
                    }
                }

                // [Fix ①] += 대신 update{}로 원자적 append
                sessionText.update { it + "$chunkResult " }

            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Resource exhausted") == true ||
                    e.message?.contains("429") == true -> "사용량이 많습니다. 잠시 후 다시 시도해 주세요."
                    else -> "번역 중 오류가 발생했습니다."
                }
                L.e("번역 에러: ${e.message}")
                _uiEvent.tryEmit(UiEvent.ShowToast(message))
            }
        }
    }

    // ──────────────────────────────────────────────
    // Public: Image Translation
    // ──────────────────────────────────────────────
    fun translateCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isImageLoading.value = true
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)

                val resultText = translateImageUseCase(
                    imageBytes = stream.toByteArray(),
                    langA = _currentLangA.value,
                    langB = _currentLangB.value
                )

                val newMessage = ChatMessage(
                    text = "[이미지 번역]\n$resultText",
                    isMine = false,
                    languageCode = LanguageDetector.toFullLocale(langBCode)
                )
                _chatMessages.value = _chatMessages.value + newMessage
                ttsManager.speak(resultText, newMessage.languageCode)
            } catch (e: Exception) {
                L.e("이미지 번역 에러: ${e.message}")
                _uiEvent.tryEmit(UiEvent.ShowToast("이미지 번역 중 오류가 발생했습니다."))
            } finally {
                _isImageLoading.value = false
            }
        }
    }

    // ──────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        chunkScope?.cancel()
        vadProcessor.stopRecording()
        ttsManager.shutdown()
    }
}
