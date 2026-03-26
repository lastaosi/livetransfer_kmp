package com.jh.livetransfer.domain.usecase.weather

import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.repository.WeatherRepository

/** 도시명으로 날씨를 조회하는 UseCase. */
class GetWeatherByCityUseCase (
    private val repository: WeatherRepository
) {
    /** @param city 영문 도시명 (예: "Seoul") */
    suspend operator fun invoke(city: String): Result<WeatherResponse> {
        return repository.getWeatherByCity(city)
    }
}