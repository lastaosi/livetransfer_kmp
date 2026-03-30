package com.jh.livetransfer.ui.screen.weather

import androidx.compose.runtime.MutableState
import com.jh.livetransfer.data.model.WeatherResponse
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByCityUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.posix.err

class WeatherViewModelIos(
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val getWeatherByCityUseCase: GetWeatherByCityUseCase
){
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchWeatherByCity(city:String, onResult:(WeatherResponse?)-> Unit){
        scope.launch {
            _isLoading.value = true
            getWeatherByCityUseCase(city)
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

    fun fetchWeatherByLocation(lat: Double,lon: Double,onResult: (WeatherResponse?) -> Unit){
        scope.launch {
            _isLoading.value = true
            getWeatherByLocationUseCase(lat,lon)
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
}