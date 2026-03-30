package com.jh.livetransfer.ui.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.usecase.weather.AddCityWeatherUseCase
import com.jh.livetransfer.domain.usecase.weather.GetSavedCityNamesUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByCityUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByLocationUseCase
import com.jh.livetransfer.domain.usecase.weather.SaveCityNamesUseCase
import com.jh.livetransfer.util.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 날씨 화면 ViewModel.
 *
 * 상태:
 * - [currentWeather]: 현재 위치 날씨 (GPS 기반, null이면 미조회)
 * - [cityWeatherList]: 사용자가 추가한 도시 목록 (중복 체크 포함)
 * - [isLoading]: 현재 위치 날씨 조회 중 로딩 상태
 * - [errorMessage]: 에러 발생 시 Toast 메시지용. clearError()로 초기화.
 *
 * 도시 목록은 DataStore에 영속된다. init 블록의 [loadSavedCities]가 앱 시작 시 복원.
 */
class WeatherViewModel(
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val addCityWeatherUseCase: AddCityWeatherUseCase,
    private val getSavedCityNamesUSeCase: GetSavedCityNamesUseCase,
    private val saveCityNamesUseCase: SaveCityNamesUseCase
) : ViewModel() {

    // 현재 위치 날씨
    private val _currentWeather = MutableStateFlow<WeatherResponse?>(null)
    val currentWeather : StateFlow<WeatherResponse?> =_currentWeather

    // 추가한 도시 날씨 리스트
    private val _cityWeatherList = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val cityWeatherList : StateFlow<List<WeatherResponse>> = _cityWeatherList

    // 로딩상태
    private val _isLoading = MutableStateFlow(false)
     val isLoading : StateFlow<Boolean> = _isLoading

    // 에러상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage : StateFlow<String?> = _errorMessage

    init {
        loadSavedCities()
    }
    //현재위치 날씨 조회
    fun fetchCurrentLocationWeather(lat:Double,lon:Double){
        viewModelScope.launch{
            _isLoading.value = true
            getWeatherByLocationUseCase(lat,lon)
                .onSuccess { _currentWeather.value = it }
                .onFailure {
                    L.d("날씨 실패 : ${it.message}")
                    _errorMessage.value = "현재 위치 날씨를 가져올수 없어요" }
            _isLoading.value = false
        }
    }

    // 도시 날씨 추가
    fun addCity(city:String){
        viewModelScope.launch {
            addCityWeatherUseCase(city)
                .onSuccess { response ->
                    // 중복 도시 체크
                    val isDuplicate = _cityWeatherList.value.any { it.name == response.name }
                    if(!isDuplicate){
                        _cityWeatherList.value = _cityWeatherList.value + response
                        saveCityNames()
                    }else{
                        _errorMessage.value = "이미 추가된 도시입니다."
                    }
                }
                .onFailure { _errorMessage.value = "도시를 찾을 수 없어요" }
        }
    }

    // 도시 삭제
    fun removeCity(cityName : String){
        _cityWeatherList.value = _cityWeatherList.value.filter{ it.name != cityName}
        viewModelScope.launch {
            saveCityNames()
        }
    }

    private suspend fun saveCityNames(){
        val cityNames = _cityWeatherList.value.map { it.name }
        saveCityNamesUseCase(cityNames)
    }

    // 에러 메시지 초기화
    fun clearError(){
        _errorMessage.value = null
    }

    private fun loadSavedCities(){
        viewModelScope.launch {
            getSavedCityNamesUSeCase().collect { cityNames ->
                val weatherList = cityNames.map { cityName ->
                    addCityWeatherUseCase(cityName).getOrNull()
                }.filterNotNull()
                _cityWeatherList.value = weatherList
            }
        }
    }
}