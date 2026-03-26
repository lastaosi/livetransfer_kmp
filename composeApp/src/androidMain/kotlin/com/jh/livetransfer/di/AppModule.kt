package com.jh.livetransfer.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.Firebase
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 앱 핵심 인프라 Koin 모듈.
 *
 * - [GenerativeModel]: Firebase Vertex AI (Gemini 2.5 Flash). 전문 통역사 시스템 프롬프트 탑재.
 * - [DataStore]<[Preferences]>: 도시 목록 영속화용 싱글톤 (파일명: "city_prefs").
 */
val appModule = module {
    single<GenerativeModel> {
        val config = generationConfig { }
        Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = config,
            systemInstruction = com.google.firebase.vertexai.type.content {
                text("""
                    You are a professional real-time interpreter.
                    Your mission is to translate audio input accurately and instantly.

                    **STRICT RULES:**
                    1. Output ONLY the translated text.
                    2. DO NOT add any conversational fillers like "Here is the translation", "Translated:", or "Okay".
                    3. DO NOT explain the translation or provide pronunciation guides.
                    4. If the audio is silence or unintelligible noise, output nothing (empty string).
                    5. Maintain the original tone and nuance of the speaker.
                """.trimIndent())
            }
        )
    }

    // CityDataStore가 사용할 DataStore<Preferences> 싱글톤 (파일명: "city_prefs")
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("city_prefs")
        }
    }
}