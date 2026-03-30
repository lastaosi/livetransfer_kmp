package com.jh.livetransfer.data.source.local

import kotlinx.coroutines.flow.Flow

expect class CityDataStore {
    val cityNames: Flow<List<String>>
    suspend fun saveCityNames(cities: List<String>)
}