package com.jh.livetransfer.domain.usecase.weather

import com.jh.livetransfer.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

/** DataStore에 저장된 도시 이름 목록을 [Flow]로 반환하는 UseCase. */
class GetSavedCityNamesUseCase (
    private val repository: WeatherRepository
) {
    operator fun invoke() : Flow<List<String>>{
        return repository.getSavedCityNames()
    }
}