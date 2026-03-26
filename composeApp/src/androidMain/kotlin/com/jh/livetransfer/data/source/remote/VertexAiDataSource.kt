package com.jh.livetransfer.data.source.remote

import com.google.firebase.vertexai.GenerativeModel
import com.jh.livetransfer.data.model.TranslationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Vertex AI 데이터 소스. [STUB / 미사용]
 *
 * 하드코딩된 fake 데이터를 emit하는 뼈대 상태.
 * DI 그래프에 연결되지 않았으며, 실제 번역은 [TranslationRepositoryImpl]이 담당한다.
 * DataSource 계층을 분리하려는 경우 이 클래스를 확장해 Repository에 주입.
 */
class VertexAiDataSource @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    fun getTranslationFlow(audioData: ByteArray): Flow<TranslationResult> = flow {
        // TODO: generativeModel.generateContentStream() 호출로 교체
        emit(TranslationResult("Original Text Sample", "번역 텍스트 샘플", true))
    }
}
