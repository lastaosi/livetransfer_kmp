package com.jh.livetransfer

import android.app.Application
import com.jh.livetransfer.di.appModule
import com.jh.livetransfer.di.dataModule
import com.jh.livetransfer.di.useCaseModule
import com.jh.livetransfer.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


/**
 * 앱 최초 진입점. Application 서브클래스.
 *
 * onCreate에서 Koin DI 컨테이너를 초기화한다.
 * AndroidManifest.xml의 android:name에 등록되어야 Koin이 정상 작동한다.
 */
class LiveTransferApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LiveTransferApplication)
            modules(
                appModule,      // Gemini GenerativeModel, DataStore
                dataModule,     // Repository, AudioCaptureManager, VadProcessor, TtsManager
                useCaseModule,  // 모든 UseCase (factory)
                viewModelModule // 모든 ViewModel (viewModel DSL)
            )
        }
    }
}

