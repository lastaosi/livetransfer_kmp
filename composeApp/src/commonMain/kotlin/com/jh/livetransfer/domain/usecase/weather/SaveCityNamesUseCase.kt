package com.jh.livetransfer.domain.usecase.weather

import com.jh.livetransfer.domain.repository.WeatherRepository

/** 도시 이름 목록을 DataStore에 저장하는 UseCase. */
class SaveCityNamesUseCase (
    private val repository: WeatherRepository
) {
    /** @param cities 저장할 영문 도시명 목록 */
    suspend operator fun invoke(cities: List<String>){
        repository.saveCityNames(cities)
    }
}