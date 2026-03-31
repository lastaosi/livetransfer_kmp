package com.jh.livetransfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OpenWeatherMap API /weather 응답 최상위 모델 */
@Serializable
data class WeatherResponse(
    val name: String,       // 도시명 (예: "Seoul")
    val main: Main,         // 온도/습도 등 주요 수치
    val weather: List<Weather>, // 날씨 상태 (맑음, 흐림 등). 일반적으로 1개 원소
    val wind: Wind          // 풍속 정보
)

/** 온도 및 습도 데이터 */
@Serializable
data class Main(
    val temp: Double,       // 현재 기온 (°C, units=metric)
    val feels_like: Double, // 체감 온도 (°C)
    val humidity: Int       // 습도 (%)
)

/** 날씨 상태 설명 */
@Serializable
data class Weather(
    @SerialName("description")
    val weatherDesc: String, // 날씨 설명 (lang=kr 설정 시 한국어 반환)
    val icon: String          // 날씨 아이콘 코드 (OpenWeatherMap 아이콘 URL 조합용)
)

/** 풍속 데이터 */
@Serializable
data class Wind(
    val speed: Double // 풍속 (m/s)
)

