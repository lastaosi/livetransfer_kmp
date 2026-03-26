package com.jh.livetransfer.di

import com.jh.livetransfer.ui.screen.exchangerate.ExchangeViewModel
import com.jh.livetransfer.ui.screen.translation.TranslationViewModel
import com.jh.livetransfer.ui.screen.weather.WeatherViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.coroutines.EmptyCoroutineContext.get

/**
 * ViewModel Koin 모듈.
 *
 * koin viewModel DSL로 등록해 Compose에서 koinViewModel()로 주입 가능하게 한다.
 * ViewModel은 Compose NavHost 진입 시 생성되고, 화면이 완전히 종료되면 소멸된다.
 */
val viewModelModule = module {
    viewModel { WeatherViewModel(get(), get(), get(), get()) }
    viewModel { ExchangeViewModel(get(), get()) }
    viewModel { TranslationViewModel(get(), get(), get(), get(), get()) }
}