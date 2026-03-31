# LiveTransfer KMP 프로젝트 완성 기록

## 완성된 기능 목록

### Android
- Clean Architecture + UseCase 레이어
- KMP 구조 (commonMain/androidMain)
- Hilt → Koin 마이그레이션
- 날씨/환율/번역 기능
- 날씨 아이콘 + 습도/풍속
- 도시 영구 저장 (DataStore)

### iOS
- SwiftUI 날씨/환율 화면
- SF Symbol 날씨 아이콘
- 현재 위치 날씨 (CoreLocation)
- 도시 영구 저장 (NSUserDefaults)
- 탭바 구조
- Koin iOS 초기화

### 공통 (commonMain)
- Domain 레이어 (UseCase, Repository 인터페이스)
- Data 레이어 (Model, KtorClient, WeatherRepositoryImpl)
- expect/actual 패턴 (KtorClient, CityDataStore)

---

## KMP 프로젝트 시작부터 완성까지 핵심 스텝

### 1단계. 프로젝트 구조 설계

KMP에서 가장 먼저 결정해야 할 건 무엇을 공유하고 무엇을 플랫폼별로 만들지야.

**공유 가능 (commonMain)**
```
├── domain/repository/     인터페이스 (구현 없음)
├── domain/usecase/        비즈니스 로직
├── data/model/            데이터 모델 (@Serializable)
├── data/remote/           네트워크 (Ktor는 KMP 지원)
└── data/repository/       구현체 (플랫폼 API 안 쓰는 것만)
```

**플랫폼별 (androidMain / iosMain)**
```
├── UI                     Android: Compose / iOS: SwiftUI
├── ViewModel              Android: Jetpack / iOS: 래퍼 클래스
├── DI                     Android: Koin 모듈 / iOS: Koin 모듈
└── 플랫폼 API 사용 코드   DataStore, CoreLocation 등
```

> **핵심 원칙:** 플랫폼 API를 쓰는 순간 그 파일은 commonMain에 못 들어가. 이걸 먼저 파악하고 설계해야 해.

---

### 2단계. Gradle 설정

KMP에서 Gradle 설정이 일반 Android 프로젝트보다 복잡해. 핵심만 정리하면:

**libs.versions.toml 필수 항목**
```toml
[versions]
kotlin = "2.x.x"                    # Kotlin 버전
koin = "4.0.0"                      # KMP 지원 DI
ktor = "2.3.7"                      # KMP 지원 네트워크 (Firebase와 충돌 주의)
kotlinx-serialization = "1.x.x"

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization" }
```

**build.gradle.kts 핵심 구조**
```kotlin
kotlin {
    androidTarget { }      // Android 타겟
    iosArm64()             // 실기기
    iosSimulatorArm64()    // 시뮬레이터

    sourceSets {
        commonMain.dependencies {
            // 플랫폼 무관 라이브러리만
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            // Android 전용
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            // iOS 전용
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

**주의사항**
- Ktor 버전이 Firebase SDK와 충돌할 수 있어 → 2.3.7 사용
- `platform()` 함수는 Kotlin 2.3+ 에서 에러 → BOM 대신 버전 직접 명시
- Java 버전은 반드시 17 또는 21 사용 (25는 Gradle 미지원)

---

### 3단계. expect/actual 패턴

플랫폼별로 구현이 달라야 하는 코드에 사용해.

**언제 쓰냐면**
- 네트워크 엔진 (Android: OkHttp, iOS: Darwin)
- 로컬 저장소 (Android: DataStore, iOS: NSUserDefaults)
- 플랫폼 API (위치, 카메라 등)

**구현 방법**
```kotlin
// commonMain — 선언만
expect class CityDataStore {
    val cityNames: Flow<List<String>>
    suspend fun saveCityNames(cities: List<String>)
}

// androidMain — Android 구현
actual class CityDataStore(
    private val dataStore: DataStore<Preferences>
) {
    actual val cityNames: Flow<List<String>> = ...
    actual suspend fun saveCityNames(cities: List<String>) { ... }
}

// iosMain — iOS 구현
actual class CityDataStore {
    actual val cityNames: Flow<List<String>> = ...
    actual suspend fun saveCityNames(cities: List<String>) {
        NSUserDefaults.standardUserDefaults.setObject(...)
    }
}
```

**주의사항**
- expect class의 생성자 파라미터가 플랫폼마다 달라도 됨
- `actual` 키워드를 프로퍼티/함수에도 붙여야 함
- `@JvmName` 같은 JVM 전용 어노테이션은 iosMain에서 사용 불가

---

### 4단계. Koin DI 설정

Hilt는 Android 전용이라 KMP에서는 Koin을 써야 해.

**Android 초기화 (LiveTransferApplication.kt)**
```kotlin
class LiveTransferApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LiveTransferApplication)
            modules(appModule, dataModule, useCaseModule, viewModelModule)
        }
    }
}
```

**iOS 초기화 (KoinInitializer.kt in iosMain)**
```kotlin
fun initKoin() {
    startKoin {
        modules(
            module {
                single { CityDataStore() }              // 파라미터 없음
                single<WeatherRepository> { WeatherRepositoryImpl(get()) }
                factory { GetWeatherByCityUseCase(get()) }
                factory { WeatherViewModelIos(get(), get(), get(), get()) }
            }
        )
    }
}
```

**iOS에서 Koin 인스턴스 가져오기**
```kotlin
fun getWeatherViewModel(): WeatherViewModelIos =
    KoinPlatform.getKoin().get()
```

**주의사항**
- Android의 CityDataStore는 DataStore 주입 필요 → `single { CityDataStore(get()) }`
- iOS의 CityDataStore는 파라미터 없음 → `single { CityDataStore() }`
- `GlobalContext`는 iOS에서 import 불가 → `KoinPlatform` 사용

---

### 5단계. iOS ViewModel 래퍼

Android의 Jetpack ViewModel은 iOS에서 사용 불가야. iOS용 래퍼를 iosMain에 만들어야 해.

**핵심 원칙**
- `suspend` 함수 → callback으로 변환
- `Flow` → callback 또는 polling으로 변환
- `CoroutineScope(Dispatchers.Main)` 직접 생성

```kotlin
class WeatherViewModelIos(
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val addCityWeatherUseCase: AddCityWeatherUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    // suspend → callback
    fun fetchWeatherByCity(city: String, onResult: (WeatherResponse?) -> Unit) {
        scope.launch {
            addCityWeatherUseCase(city)
                .onSuccess { onResult(it) }
                .onFailure { onResult(null) }
        }
    }

    // Flow → callback
    fun observeCityList(onUpdate: (List<WeatherResponse>) -> Unit) {
        scope.launch {
            _cityWeatherList.collect { list ->
                onUpdate(list)
            }
        }
    }
}
```

**Swift에서 호출**
```swift
// 일반 함수 호출
viewModel.fetchWeatherByCity(city: "Seoul") { response in
    DispatchQueue.main.async {
        // UI 업데이트
    }
}

// suspend 함수는 Task + try await
func removeCity(cityName: String) {
    Task {
        try await viewModel.removeCity(cityName: cityName) {
            // 완료 콜백
        }
    }
}

// Flow 구독
viewModel.observeCityList { list in
    DispatchQueue.main.async {
        self?.weatherList = list as! [WeatherResponse]
    }
}
```

---

### 6단계. Swift/Kotlin 타입 충돌 주의사항

Kotlin 코드가 Swift로 변환될 때 이름 충돌이 생기는 경우가 있어.

**description 프로퍼티 충돌**
```kotlin
// Kotlin data class
data class Weather(
    val description: String,  // Swift의 CustomStringConvertible.description과 충돌!
    val icon: String
)

// 해결 — @SerialName으로 JSON 키는 유지하고 프로퍼티명 변경
data class Weather(
    @SerialName("description")
    val weatherDesc: String,   // Swift에서 w.weatherDesc로 접근
    val icon: String
)
```

**Substring vs String**
```swift
// Swift에서 prefix()는 Substring 반환
let prefix = icon.prefix(2)         // Substring — switch 매칭 실패
let prefix = String(icon.prefix(2)) // String — switch 정상 작동
```

**Kotlin Flow → Swift 접근**
```swift
// StateFlow.value는 Swift에서 직접 접근 어려움
// → observeXxx 콜백 함수 패턴 사용 권장
viewModel.observeCityList { list in ... }
```

---

### 7단계. Xcode 빌드 설정

**Java 버전 문제**

Xcode는 시스템 Java를 사용하는데 버전이 맞지 않으면 빌드 실패.
Build Phases → Compile Kotlin Framework 스크립트 상단에 추가:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

**Kotlin 빌드 순서**
1. Android Studio에서 먼저 Kotlin 빌드 (Cmd + F9)
2. 그 다음 Xcode에서 빌드

> Kotlin 코드 변경 후 Xcode에서 Swift 자동완성이 안 되면 반드시 Kotlin 빌드 먼저

**iOS 권한 설정 (Info.plist)**
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>현재 위치의 날씨를 가져오기 위해 위치 권한이 필요합니다.</string>
```

---

### 8단계. 자주 발생하는 에러 모음

| 에러 | 원인 | 해결 |
|---|---|---|
| `Unable to locate Java Runtime` | Xcode가 Java 못 찾음 | JAVA_HOME 환경변수 설정 |
| `platform() is an error` | Kotlin 2.3+ BOM 미지원 | 버전 직접 명시 |
| `No definition found for type 'X'` | Koin 모듈 미등록 | `module { }` 에 추가 |
| `KoinApplication has not been started` | Koin 초기화 전 사용 | `initKoin()` 먼저 호출 |
| `Unresolved reference` | Kotlin 빌드 안 됨 | Android Studio 빌드 먼저 |
| `expect class has no actual` | actual 구현 누락 | androidMain/iosMain에 actual 추가 |
| `Cannot find 'X' in scope` | Swift에서 Kotlin 함수 못 찾음 | Kotlin 빌드 후 Xcode 빌드 |

---

### 9단계. 아키텍처 최종 정리

```
[Android]                    [iOS]
Jetpack Compose UI           SwiftUI
WeatherViewModel             WeatherViewModelIos (래퍼)
        ↓                           ↓
        ─────────── commonMain ───────────
        UseCase (GetWeatherByCityUseCase 등)
        Repository Interface (WeatherRepository)
        Model (WeatherResponse 등)
        KtorClient (expect/actual)
        ─────────────────────────────────
        ↓                           ↓
[androidMain]                [iosMain]
WeatherRepositoryImpl        (공유)
CityDataStore (DataStore)    CityDataStore (NSUserDefaults)
Koin 모듈                    Koin 모듈
```
