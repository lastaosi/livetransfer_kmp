package com.jh.livetransfer.domain.usecase.translation

import com.jh.livetransfer.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow

/**
 * WAV 오디오를 스트리밍으로 번역하는 UseCase.
 * Gemini generateContentStream을 통해 청크 단위 [Flow]<String>으로 번역 결과를 방출한다.
 */
class TranslateAudioStreamUseCase (
    private val repository: TranslationRepository
) {
    /**
     * @param audioWavBytes WAV 헤더가 포함된 PCM 오디오 바이트 배열
     * @param langA 언어 A 이름 (예: "한국어"). 감지 언어에 따라 A↔B 방향으로 번역.
     * @param langB 언어 B 이름 (예: "영어")
     */
    suspend operator fun invoke(
        audioWavBytes: ByteArray,
        langA:String,
        langB:String
    ): Flow<String> {
        return repository.translateAudioStream(audioWavBytes,langA,langB)
    }
}