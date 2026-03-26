package com.jh.livetransfer.domain.repository

import com.jh.livetransfer.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

/**
 * 날씨 데이터 Domain 계층 인터페이스.
 * 구현체: [WeatherRepositoryImpl] (OpenWeatherMap API + DataStore).
 */
interface WeatherRepository {
    /** 도시명으로 날씨를 조회한다. 존재하지 않는 도시명이면 Result.failure 반환. */
    suspend fun getWeatherByCity(city: String): Result<WeatherResponse>

    /** 위도/경도로 날씨를 조회한다. GPS 기반 현재 위치 날씨에 사용. */
    suspend fun getWeatherByLocation(lat: Double, lon: Double): Result<WeatherResponse>

    /** DataStore에 저장된 도시 이름 목록을 Flow로 반환한다. */
    fun getSavedCityNames(): Flow<List<String>>

    /** 도시 이름 목록을 DataStore에 저장한다. */
    suspend fun saveCityNames(cities: List<String>)
}