package com.jh.livetransfer.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 번역 기능의 Domain 계층 인터페이스.
 * 구현체는 [TranslationRepositoryImpl] (Vertex AI / Gemini 호출).
 */
interface TranslationRepository {

    /**
     * WAV 오디오를 단일 응답으로 번역한다 (non-streaming).
     * 현재 미사용 — streaming 버전([translateAudioStream])만 활성화되어 있음.
     */
    suspend fun translateAudio(audioBytes: ByteArray, langA: String, langB: String): String

    /**
     * WAV 오디오를 스트리밍으로 번역한다.
     * Gemini generateContentStream을 통해 청크 단위로 Flow<String>을 방출.
     * 예외는 caller(ViewModel)로 전파한다.
     */
    suspend fun translateAudioStream(audioWavBytes: ByteArray, langA: String, langB: String): Flow<String>

    /**
     * 이미지(JPEG bytes)에서 텍스트를 OCR하고 번역한다.
     * 번역 결과 텍스트만 반환 (원문 미포함).
     */
    suspend fun translateImage(imageBytes: ByteArray, langA: String, langB: String): String
}
