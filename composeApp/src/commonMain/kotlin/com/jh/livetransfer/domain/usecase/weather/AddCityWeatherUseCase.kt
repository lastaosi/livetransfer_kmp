package com.jh.livetransfer.domain.usecase.weather

import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.repository.WeatherRepository

/**
 * 도시명으로 날씨를 조회해 도시 목록에 추가하는 UseCase.
 * 중복 체크 및 목록 관리는 [WeatherViewModel]에서 담당하며, 이 UseCase는 조회만 수행.
 */
class AddCityWeatherUseCase (
    private val repository: WeatherRepository
) {
    /** @param city 영문 도시명 (예: "Seoul", "Tokyo") */
    suspend operator fun invoke(city:String) : Result<WeatherResponse>{
        return repository.getWeatherByCity(city)
    }
}