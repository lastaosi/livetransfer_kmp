package com.jh.livetransfer.domain.usecase.weather

import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.repository.WeatherRepository

/** GPS 위도/경도로 현재 위치 날씨를 조회하는 UseCase. */
class GetWeatherByLocationUseCase (
    private val repository: WeatherRepository
){
    /**
     * @param lat 위도 (예: 37.5665)
     * @param lon 경도 (예: 126.9780)
     */
    suspend operator fun invoke(lat:Double,lon:Double) : Result<WeatherResponse>{
        return repository.getWeatherByLocation(lat,lon)
    }
}