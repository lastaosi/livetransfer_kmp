package com.jh.livetransfer.data.model

/**
 * 언어 선택 UI에서 사용하는 언어 옵션 모델.
 *
 * @param name 표시 이름 (예: "한국어")
 * @param code BCP-47 짧은 코드 (예: "ko"). LanguageDetector / TtsManager에서 사용.
 */
data class LanguageOption(val name: String, val code: String)

/** 앱이 지원하는 언어 목록. SettingsScreen 언어 선택 다이얼로그에 표시된다. */
val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("한국어", "ko"),
    LanguageOption("영어", "en"),
    LanguageOption("일본어", "ja"),
    LanguageOption("중국어", "zh"),
    LanguageOption("스페인어", "es"),
    LanguageOption("프랑스어", "fr"),
    LanguageOption("독일어", "de")
)
