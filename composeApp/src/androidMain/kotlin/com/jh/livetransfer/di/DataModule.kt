package com.jh.livetransfer.di

import com.jh.livetransfer.data.repository.ExchangeRepositoryImpl
import com.jh.livetransfer.data.repository.WeatherRepositoryImpl
import com.jh.livetransfer.data.repository.SettingsRepository
import com.jh.livetransfer.data.repository.TranslationRepositoryImpl
import com.jh.livetransfer.data.source.local.CityDataStore
import com.jh.livetransfer.domain.repository.ExchangeRepository
import com.jh.livetransfer.domain.repository.TranslationRepository
import com.jh.livetransfer.domain.repository.WeatherRepository
import com.jh.livetransfer.util.AudioCaptureManager
import com.jh.livetransfer.util.TtsManager
import com.jh.livetransfer.util.VadProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 데이터 계층 Koin 모듈.
 *
 * Repository 구현체, 로컬/원격 데이터 소스, 오디오 관련 유틸리티를 싱글톤으로 등록한다.
 * ViewModel 생명주기와 무관하게 앱 전체에서 하나의 인스턴스를 공유한다.
 */
val dataModule = module {
    single<WeatherRepository> { WeatherRepositoryImpl(get<CityDataStore>()) }
    single<ExchangeRepository> { ExchangeRepositoryImpl() }
    single<TranslationRepository> { TranslationRepositoryImpl(get()) }
    single { SettingsRepository(get()) }
    single { CityDataStore(get()) }

    single { AudioCaptureManager(androidContext()) }
    single { VadProcessor (get<AudioCaptureManager>()) }
    single { TtsManager(androidContext()) }
}