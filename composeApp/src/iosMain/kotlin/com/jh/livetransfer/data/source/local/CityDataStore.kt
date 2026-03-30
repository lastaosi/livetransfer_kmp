package com.jh.livetransfer.data.source.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class CityDataStore {
    companion object{
        private const val CITY_LIST_KEY = "city_list"
        private const val SEPARATOR = ","
    }

    private val _cityNames = MutableStateFlow<List<String>>(loadFromUserDefaults())
    actual val cityNames: Flow<List<String>> = _cityNames

    private fun loadFromUserDefaults() : List<String>{
        val saved = NSUserDefaults.standardUserDefaults.stringForKey(CITY_LIST_KEY)
        return saved?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    actual suspend fun saveCityNames(cities: List<String>){
        NSUserDefaults.standardUserDefaults.setObject(
            cities.joinToString(SEPARATOR),
            CITY_LIST_KEY
        )
        _cityNames.value = cities
    }
}