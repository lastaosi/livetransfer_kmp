package com.jh.livetransfer.data.model

/**
 * 번역 채팅 화면에서 한 발화(utterance) 단위를 표현하는 모델.
 *
 * @param text 번역된 텍스트. 스트리밍 중에는 청크가 누적되며 실시간 업데이트됨.
 * @param isMine true: 내가 말한 쪽(오른쪽 말풍선), false: 상대방 언어로 번역된 결과(왼쪽 말풍선)
 * @param languageCode TTS 발화에 사용되는 BCP-47 로케일 코드 (예: "ko-KR", "en-US")
 */
data class ChatMessage(
    val text: String,
    val isMine: Boolean,
    val languageCode: String
)
