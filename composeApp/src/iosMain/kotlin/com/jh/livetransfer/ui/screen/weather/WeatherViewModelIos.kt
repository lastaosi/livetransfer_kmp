package com.jh.livetransfer.ui.screen.weather

import androidx.compose.runtime.MutableState
import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.usecase.weather.AddCityWeatherUseCase
import com.jh.livetransfer.domain.usecase.weather.GetSavedCityNamesUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByCityUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByLocationUseCase
import com.jh.livetransfer.domain.usecase.weather.SaveCityNamesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import platform.posix.err

/**
 * iOS용 날씨 ViewModel 래퍼.
 *
 * Jetpack ViewModel은 iOS에서 사용할 수 없으므로, iosMain에 별도로 정의한 클래스.
 * suspend 함수는 callback으로 변환하고, Flow는 observeXxx 패턴으로 Swift에 노출한다.
 * CoroutineScope는 Dispatchers.Main으로 직접 생성해 UI 스레드에서 동작을 보장한다.
 */
class WeatherViewModelIos(
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val addCityWeatherUseCase: AddCityWeatherUseCase,
    private val getSavedCityNamesUseCase: GetSavedCityNamesUseCase,
    private val saveCityNamesUseCase: SaveCityNamesUseCase
){
    private val scope = CoroutineScope(Dispatchers.Main)

    // 현재 위치 날씨 상태
    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    // replay=1: Swift에서 구독 전 마지막 값을 즉시 받을 수 있도록 캐싱
    private val _cityWeatherList = MutableSharedFlow<List<WeatherResponse>>(1)
    val cityWeatherList : SharedFlow<List<WeatherResponse>> = _cityWeatherList.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadSavedCities()
    }

    /** DataStore에 저장된 도시 목록을 불러와 각 도시의 날씨를 일괄 조회한다. */
    private fun loadSavedCities(){
        scope.launch {
            getSavedCityNamesUseCase().collect { cityNames ->
                val weatherList = cityNames.mapNotNull { cityName ->
                    addCityWeatherUseCase(cityName).getOrNull()
                }
                _cityWeatherList.emit(weatherList)
            }
        }
    }

    /**
     * 도시명으로 날씨를 조회하고 목록에 추가한다.
     * 중복 도시는 추가하지 않고 에러 메시지를 설정한다.
     * @param onResult 성공 시 WeatherResponse, 실패/중복 시 null 전달
     */
    fun fetchWeatherByCity(city: String, onResult: (WeatherResponse?) -> Unit) {
        scope.launch {
            _isLoading.value = true
            addCityWeatherUseCase(city)
                .onSuccess { response ->
                    val currentList = _cityWeatherList.replayCache.firstOrNull() ?: emptyList()
                    val isDuplicate = currentList.any { it.name == response.name }
                    if (!isDuplicate) {
                        _cityWeatherList.emit(currentList + response)
                        saveCityNames()
                        onResult(response)
                    } else {
                        _errorMessage.value = "이미 추가된 도시입니다."
                        onResult(null)
                    }
                }
                .onFailure {
                    _errorMessage.value = "날씨를 가져올 수 없어요"
                    onResult(null)
                }
            _isLoading.value = false
        }
    }

    /**
     * 위도/경도로 현재 위치 날씨를 조회한다. CoreLocation에서 받은 좌표를 전달받아 사용.
     * @param onResult 성공 시 WeatherResponse, 실패 시 null 전달
     */
    fun fetchWeatherByLocation(lat: Double, lon: Double, onResult: (WeatherResponse?) -> Unit){
        scope.launch {
            _isLoading.value = true
            getWeatherByLocationUseCase(lat, lon)
                .onSuccess {
                    _weatherState.value = it
                    onResult(it)
                }
                .onFailure {
                    _errorMessage.value = "날씨를 가져올 수 없어요"
                    onResult(null)
                }
            _isLoading.value = false
        }
    }

    /**
     * 도시를 목록에서 제거하고 DataStore에 저장한다.
     * Swift에서 Task { try await viewModel.removeCity(...) } 패턴으로 호출.
     */
    suspend fun removeCity(cityName: String, onResult: () -> Unit) {
        _cityWeatherList.emit(_cityWeatherList.replayCache.first().filter { it.name != cityName })
        scope.launch {
            saveCityNames()
            onResult()
        }
    }

    /** 현재 cityWeatherList를 DataStore에 저장한다. */
    private suspend fun saveCityNames() {
        val cityNames = _cityWeatherList.replayCache.firstOrNull()?.map { it.name } ?: emptyList()
        saveCityNamesUseCase(cityNames)
    }

    /**
     * cityWeatherList의 변경을 Swift로 전달하는 콜백 구독.
     * StateFlow.value는 Swift에서 직접 접근이 어렵기 때문에 이 패턴을 사용한다.
     */
    fun observeCityList(onUpdate : (List<WeatherResponse>) -> Unit ){
        scope.launch {
            _cityWeatherList.collect { list ->
                println("observeCityList emit: ${list.size}개")
                onUpdate(list)
            }
        }
    }
}