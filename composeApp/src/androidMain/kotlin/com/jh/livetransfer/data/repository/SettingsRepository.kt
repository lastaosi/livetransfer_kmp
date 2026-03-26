package com.jh.livetransfer.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore 확장 프로퍼티 (싱글톤처럼 작동)
private val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository (
     private val context: Context
) {
    // 저장할 키(Key) 정의
    companion object {
        val KEY_LANG_A_CODE = stringPreferencesKey("lang_a_code")
        val KEY_LANG_A_NAME = stringPreferencesKey("lang_a_name")
        val KEY_LANG_B_CODE = stringPreferencesKey("lang_b_code")
        val KEY_LANG_B_NAME = stringPreferencesKey("lang_b_name")
        val KEY_SPEECH_SPEED = longPreferencesKey("speech_speed_delay") // VAD 대기 시간(ms)
    }

    // 기본값 설정 (한국어 <-> 영어, 보통 속도)
    val langACode: Flow<String> = context.dataStore.data.map { it[KEY_LANG_A_CODE] ?: "ko" }
    val langAName: Flow<String> = context.dataStore.data.map { it[KEY_LANG_A_NAME] ?: "한국어" }

    val langBCode: Flow<String> = context.dataStore.data.map { it[KEY_LANG_B_CODE] ?: "en" }
    val langBName: Flow<String> = context.dataStore.data.map { it[KEY_LANG_B_NAME] ?: "영어" }

    // 기본값 1000ms (보통 속도). SpeechSpeed.NORMAL = 1000L, FAST = 600L, SLOW = 1500L
    val speechSpeedDelay: Flow<Long> = context.dataStore.data.map { it[KEY_SPEECH_SPEED] ?: 1000L }

    suspend fun updateLangA(code: String, name: String) {
        context.dataStore.edit { it[KEY_LANG_A_CODE] = code; it[KEY_LANG_A_NAME] = name }
    }

    suspend fun updateLangB(code: String, name: String) {
        context.dataStore.edit { it[KEY_LANG_B_CODE] = code; it[KEY_LANG_B_NAME] = name }
    }

    suspend fun updateSpeechSpeed(delayMs: Long) {
        context.dataStore.edit { it[KEY_SPEECH_SPEED] = delayMs }
    }
}