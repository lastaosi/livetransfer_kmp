package com.jh.livetransfer.data.model

/**
 * 말하기 속도 설정. VAD(Voice Activity Detection)의 침묵 감지 대기 시간을 제어한다.
 *
 * [silenceDelay]: 발화 후 이 시간(ms) 동안 조용하면 VadProcessor가 ChunkReady를 방출.
 * 빠를수록 짧은 침묵에도 즉시 번역을 시작하고, 느릴수록 발화 완료를 더 기다린다.
 */
enum class SpeechSpeed(val label: String, val silenceDelay: Long) {
    FAST("빠름", 600L),
    NORMAL("보통", 1000L),
    SLOW("느림", 1500L)
}