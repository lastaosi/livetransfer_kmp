package com.jh.livetransfer.data.model

/**
 * 번역 결과 모델. 현재 [VertexAiDataSource] stub에서만 사용되며 실제 흐름에서는 미사용.
 *
 * @param originalText 원본 텍스트 (STT 인식 결과)
 * @param translatedText 번역된 텍스트
 * @param isFinal 스트리밍 완료 여부. true이면 최종 확정 결과.
 */
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val isFinal: Boolean = false
)
