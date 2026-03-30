package com.jh.livetransfer.data.repository

import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.data.remote.KTorClient
import com.jh.livetransfer.data.source.local.CityDataStore
import com.jh.livetransfer.domain.repository.WeatherRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow

/**
 * OpenWeatherMap API v2.5 날씨 데이터 Repository.
 *
 * 두 가지 조회 방식을 제공:
 * 1. [getWeatherByCity]: 도시명으로 검색 (사용자 직접 입력)
 * 2. [getWeatherByLocation]: 위도/경도로 검색 (현재 위치)
 *
 * 네트워크 에러를 Result.failure로 래핑해 ViewModel에서 onFailure 처리.
 * 주의: apiKey가 소스코드에 하드코딩되어 있음 — 프로덕션 배포 전 local.properties 등으로 분리 필요.
 */
class WeatherRepositoryImpl (
    private val cityDataStore: CityDataStore
) : WeatherRepository {
    private val client = KTorClient.client
    private val apiKey = "08ab4d8c18ea8338110a76f133ccd9a7"
    private val baseUrl = "https://api.openweathermap.org/data/2.5"

    //도시명으로 날씨 조회
    override suspend fun getWeatherByCity(city : String) : Result<WeatherResponse>{
        return try{
            val response = client.get("$baseUrl/weather"){
                parameter("q",city)
                parameter("appid",apiKey)
                parameter("units","metric") //섭씨
                parameter("lang","kr")
            }
            Result.success(response.body<WeatherResponse>())

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    // 위도 경도로 날씨 조회(현재위치용)
    override suspend fun getWeatherByLocation(lat:Double,lon:Double) : Result<WeatherResponse>{
        return try{
            val response = client.get("$baseUrl/weather") {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("appid", apiKey)
                parameter("units","metric")
                parameter("lang","ko")
            }
            Result.success(response.body<WeatherResponse>())
        }catch (e:Exception){
            Result.failure(e)
        }
    }

    override fun getSavedCityNames(): Flow<List<String>> {
        return cityDataStore.cityNames
    }

    override suspend fun saveCityNames(cities: List<String>) {
        cityDataStore.saveCityNames(cities)
    }
}