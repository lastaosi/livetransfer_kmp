package com.jh.livetransfer.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Android TextToSpeech 래퍼. @Singleton으로 앱 생명주기와 함께한다.
 *
 * - 초기화는 생성 시점에 비동기로 시작되며, onInit 콜백에서 완료 여부를 확인.
 * - 발화 시작/종료를 [onSpeakingStateChanged] 콜백으로 외부에 알려
 *   ViewModel이 isTtsSpeaking StateFlow를 업데이트하도록 한다.
 * - QUEUE_FLUSH: 새 발화 요청 시 이전 큐를 비워 즉시 새 텍스트를 읽음.
 */
class TtsManager (
     context: Context
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        // 객체 생성 시점에 TTS 엔진 초기화 시작 (비동기 — onInit 콜백에서 완료 확인)
        tts = TextToSpeech(context, this)
    }

    /** 발화 상태 변경 콜백. true = 시작, false = 완료/에러 */
    var onSpeakingStateChanged: ((Boolean) -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged?.invoke(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged?.invoke(false)
                }

                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged?.invoke(false)
                }
            })
            L.d("TTS 초기화 성공")
        } else {
            L.d("TTS 초기화 실패")
        }
    }

    /**
     * 지정 언어로 텍스트를 읽는다.
     *
     * @param languageCode BCP-47 로케일 코드 (예: "ko-KR", "en-US")
     * 기기에 해당 언어 음성 데이터가 없으면 에러 로그 후 조용히 무시.
     */
    fun speak(text: String, languageCode: String) {
        if (!isInitialized) {
            L.w("TTS is not initialized")
            return
        }
        val locale = Locale.forLanguageTag(languageCode)
        val result = tts?.setLanguage(locale)

        // 기기에 언어 음성 데이터가 없는 경우 방어
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            L.e("TTS 오류: 기기에서 지원하지 않는 언어입니다 ($languageCode)")
            return
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
    }

    /** 현재 발화 즉시 중단 */
    fun stop() {
        tts?.stop()
    }

    /** ViewModel onCleared()에서 호출. 리소스를 완전히 해제한다. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}