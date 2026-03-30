package com.jh.livetransfer.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 도시 이름 목록을 DataStore에 저장/조회하는 로컬 데이터 소스.
 *
 * 도시 목록을 쉼표(",") 구분 문자열로 직렬화해 단일 키에 저장한다.
 * [WeatherRepositoryImpl]에 주입되어 사용된다.
 */
actual class CityDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object{
        private val CITY_LIST_KEY = stringPreferencesKey("city_list")
        private const val SEPARATOR = ","
    }

    /** DataStore에서 읽어온 도시 이름 Flow. 빈 문자열 항목은 자동 필터링. */
    actual val cityNames: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[CITY_LIST_KEY]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    /** 도시 이름 목록을 쉼표 구분 문자열로 직렬화해 DataStore에 저장한다. */
    actual suspend fun saveCityNames(cities: List<String>){
        dataStore.edit { preferences ->
            preferences[CITY_LIST_KEY] = cities.joinToString(SEPARATOR)
        }
    }
}