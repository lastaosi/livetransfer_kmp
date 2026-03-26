package com.jh.livetransfer.util

object LanguageDetector {

    /**
     * 텍스트가 주어진 언어 코드에 해당하는지 판별합니다.
     * 완벽한 언어 감지가 아니라 1차 필터 역할입니다.
     * 정확도가 중요한 경우 ML Kit Language ID 도입을 권장합니다.
     */
    fun isLanguage(text: String, langCode: String): Boolean = when (langCode) {
        "ko" -> text.contains(Regex("[ㄱ-ㅎㅏ-ㅣ가-힣]"))
        "en" -> text.contains(Regex("[a-zA-Z]")) && !text.contains(Regex("[ㄱ-ㅎㅏ-ㅣ가-힣]"))
        "ja" -> text.contains(Regex("[\u3040-\u309F\u30A0-\u30FF]"))
        "zh" -> text.contains(Regex("[\u4E00-\u9FFF]"))
        else -> false
    }

    /**
     * 짧은 언어 코드("ko", "en" 등)를 TTS용 전체 로케일 코드로 변환합니다.
     */
    fun toFullLocale(shortCode: String): String = when (shortCode) {
        "ko" -> "ko-KR"
        "en" -> "en-US"
        "ja" -> "ja-JP"
        "zh" -> "zh-CN"
        "es" -> "es-ES"
        "fr" -> "fr-FR"
        "de" -> "de-DE"
        else -> "en-US"
    }
}
