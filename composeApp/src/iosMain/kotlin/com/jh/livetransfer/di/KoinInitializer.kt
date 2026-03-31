package com.jh.livetransfer.di

import com.jh.livetransfer.data.repository.ExchangeRepositoryImpl
import com.jh.livetransfer.data.repository.WeatherRepositoryImpl
import com.jh.livetransfer.data.source.local.CityDataStore
import com.jh.livetransfer.domain.repository.ExchangeRepository
import com.jh.livetransfer.domain.repository.WeatherRepository
import com.jh.livetransfer.domain.usecase.exchange.ConvertCurrencyUseCase
import com.jh.livetransfer.domain.usecase.exchange.GetExchangeRatesUseCase
import com.jh.livetransfer.domain.usecase.weather.AddCityWeatherUseCase
import com.jh.livetransfer.domain.usecase.weather.GetSavedCityNamesUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByCityUseCase
import com.jh.livetransfer.domain.usecase.weather.GetWeatherByLocationUseCase
import com.jh.livetransfer.domain.usecase.weather.SaveCityNamesUseCase
import com.jh.livetransfer.ui.screen.exchangerate.ExchangeViewModelIos
import com.jh.livetransfer.ui.screen.weather.WeatherViewModelIos
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * iOS용 Koin DI 초기화.
 *
 * Swift 진입점(iosApp.swift 등)에서 앱 시작 시 한 번만 호출해야 한다.
 * Android의 startKoin { androidContext(...) }과 달리 Context가 필요 없으므로
 * 파라미터 없이 호출 가능.
 *
 * - single: 앱 생명주기 동안 단일 인스턴스 유지 (Repository, DataStore)
 * - factory: 호출마다 새 인스턴스 생성 (UseCase, ViewModel)
 */
fun initKoin(){
    startKoin {
        modules(
            module {
                // iOS는 NSUserDefaults 기반 CityDataStore — 생성자 파라미터 없음
                single { CityDataStore() }

                single<WeatherRepository>{ WeatherRepositoryImpl(get())}
                single<ExchangeRepository>{ ExchangeRepositoryImpl() }

                factory { GetWeatherByLocationUseCase(get()) }
                factory { GetWeatherByCityUseCase(get()) }
                factory { AddCityWeatherUseCase(get()) }
                factory { GetSavedCityNamesUseCase(get()) }
                factory { SaveCityNamesUseCase(get()) }
                factory { GetExchangeRatesUseCase(get()) }
                factory { ConvertCurrencyUseCase() }

                factory { WeatherViewModelIos(get(),get(),get(),get())}
                factory { ExchangeViewModelIos(get(),get()) }
            }
        )
    }
}

/** Swift에서 WeatherViewModelIos 인스턴스를 Koin 컨테이너에서 꺼내는 진입점. */
fun getWeatherViewModel(): WeatherViewModelIos =
    KoinPlatform.getKoin().get()

/** Swift에서 ExchangeViewModelIos 인스턴스를 Koin 컨테이너에서 꺼내는 진입점. */
fun getExchangeViewModel(): ExchangeViewModelIos =
    KoinPlatform.getKoin().get()