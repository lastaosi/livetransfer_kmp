package com.jh.livetransfer.data.repository

import android.graphics.BitmapFactory
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import com.jh.livetransfer.domain.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * [TranslationRepository] 구현체.
 * Firebase Vertex AI SDK를 통해 Gemini 모델을 호출한다.
 * AppModule에서 주입받은 [GenerativeModel]에 시스템 프롬프트(전문 통역사 페르소나)가 이미 설정되어 있다.
 */
class TranslationRepositoryImpl (
    private val generativeModel: GenerativeModel
) : TranslationRepository {

    /**
     * 단일 응답 번역 (non-streaming). 현재 미사용.
     * blob()으로 WAV 바이트를 인라인 첨부해 Gemini에 전송한다.
     */
    override suspend fun translateAudio(audioBytes: ByteArray, langA: String, langB: String): String =
        withContext(Dispatchers.IO) {
            val response = generativeModel.generateContent(
                content {
                    text("""
                    [Task]
                    Identify the language of the audio.
                    - If it is '$langA', translate it to '$langB'.
                    - If it is '$langB', translate it to '$langA'.

                    [Input Audio]
                """.trimIndent())
                    inlineData( audioBytes,"audio/wav")
                }
            )
            response.text?.trim() ?: ""
        }

    // 예외는 caller(ViewModel)로 전파 — Repository가 에러를 텍스트로 emit하지 않음
    override suspend fun translateAudioStream(
        audioWavBytes: ByteArray,
        langA: String,
        langB: String
    ): Flow<String> = flow {
        // generateContentStream: 서버에서 청크 단위로 응답이 오며 Flow로 순차 방출
        val responseStream = generativeModel.generateContentStream(
            content {
                text("""
    Translate the audio input.
    - If the language is $langA, translate to $langB.
    - If the language is $langB, translate to $langA.
    - Output ONLY the translated text. No explanations, no original text, no commentary.
    - If the audio is silent or unintelligible, output nothing.
""".trimIndent())
                inlineData(audioWavBytes,"audio/wav")
            }
        )
        responseStream.collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }

    /**
     * 이미지 OCR + 번역.
     * ByteArray → Bitmap 변환 후 Gemini에 인라인 이미지로 첨부.
     * 번역문만 출력하도록 프롬프트에 명시적으로 제약을 건다.
     */
    override suspend fun translateImage(imageBytes: ByteArray, langA: String, langB: String): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val response = generativeModel.generateContent(
            content {
                text("""
                    You are an expert OCR and translation AI.
                    Your task is to extract text from the image and translate it.

                    [STRICT RULES]
                    1. Extract ALL visible text from the provided image.
                    2. Detect the language of the extracted text.
                    3. If the text is in '$langA', translate it to '$langB'.
                    4. If the text is in '$langB', translate it to '$langA'.
                    5. If the text is mixed, translate it based on context.
                    6. **OUTPUT ONLY THE TRANSLATED TEXT.**
                    7. Do NOT include the original text or any explanations (e.g., "Translation:", "Here is the text").
                    8. If no text is found, output "No text detected in the image."
                """.trimIndent())
                image(bitmap)
            }
        )
        return response.text ?: ""
    }
}
