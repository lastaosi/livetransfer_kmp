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

fun initKoin(){
    startKoin {
        modules(
            module {
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

                factory { WeatherViewModelIos(get(),get())}
                factory { ExchangeViewModelIos(get(),get()) }
            }
        )
    }


}
fun getWeatherViewModel(): WeatherViewModelIos =
    KoinPlatform.getKoin().get()

fun getExchangeViewModel(): ExchangeViewModelIos =
    KoinPlatform.getKoin().get()