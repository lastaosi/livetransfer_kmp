package com.jh.livetransfer.di

import com.jh.livetransfer.domain.usecase.exchange.ConvertCurrencyUseCase
import com.jh.livetransfer.domain.usecase.exchange.GetExchangeRatesUseCase
import com.jh.livetransfer.domain.usecase.translation.TranslateAudioStreamUseCase
import com.jh.livetransfer.domain.usecase.translation.TranslateImageUseCase
import com.jh.livetransfer.domain.usecase.weather.AddCityWeatherUseCase
import com.jh.livetransfer.domain.usecase.weather.GetSavedCityNamesUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByCityUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByLocationUseCase
import com.jh.livetransfer.domain.usecase.weather.SaveCityNamesUseCase
import org.koin.dsl.module

/**
 * UseCase Koin 모듈.
 *
 * 모든 UseCase를 factory로 등록한다.
 * factory: ViewModel이 요청할 때마다 새 인스턴스를 생성 (stateless이므로 공유 불필요).
 */
val useCaseModule = module {
    factory { GetWeatherByLocationUseCase(get()) }
    factory { GetWeatherByCityUseCase(get()) }
    factory { AddCityWeatherUseCase(get()) }
    factory { GetSavedCityNamesUseCase(get()) }
    factory { SaveCityNamesUseCase(get()) }
    factory { GetExchangeRatesUseCase(get()) }
    factory { ConvertCurrencyUseCase() }
    factory { TranslateAudioStreamUseCase(get()) }
    factory { TranslateImageUseCase(get()) }
}