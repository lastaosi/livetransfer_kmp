package com.jh.livetransfer.domain.usecase.translation

import com.jh.livetransfer.domain.repository.TranslationRepository

/**
 * 이미지에서 텍스트를 OCR하고 번역하는 UseCase.
 * Gemini 멀티모달 API로 이미지를 분석해 번역문만 반환한다.
 */
class TranslateImageUseCase (
    private val repository: TranslationRepository
) {
    /**
     * @param imageBytes JPEG 인코딩된 이미지 바이트 배열
     * @param langA 언어 A 이름 (예: "한국어")
     * @param langB 언어 B 이름 (예: "영어")
     * @return 번역된 텍스트. 이미지에 텍스트가 없으면 "No text detected in the image."
     */
    suspend operator fun invoke(
        imageBytes: ByteArray,
        langA: String,
        langB: String
    ): String {
        return repository.translateImage(imageBytes, langA, langB)
    }
}