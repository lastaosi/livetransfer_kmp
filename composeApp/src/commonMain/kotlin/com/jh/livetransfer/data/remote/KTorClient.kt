package com.jh.livetransfer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP 클라이언트 싱글톤.
 * OpenWeatherMap API 호출에 사용된다.
 *
 * - Android 엔진 사용 (OkHttp 대신 경량 Android HttpURLConnection 기반)
 * - ContentNegotiation + kotlinx.serialization: JSON 응답을 data class로 자동 역직렬화
 * - ignoreUnknownKeys = true: API 응답에 모델에 없는 필드가 있어도 무시
 */
expect fun createHttpClient(): HttpClient
object KTorClient {
    val client: HttpClient by lazy{
        createHttpClient().config {
            install(ContentNegotiation){
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}