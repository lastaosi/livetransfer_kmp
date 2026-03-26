# LiveTransfer KMP

Kotlin Multiplatform(KMP) + Compose Multiplatform 기반 실시간 번역 앱.
Firebase Vertex AI(Gemini 2.5 Flash)를 활용해 음성 스트리밍 번역, 이미지 OCR 번역, 날씨, 환율 정보를 제공합니다.

> 현재 UI/기능 구현은 **Android** 전용이며, iOS는 Compose 진입점(stub) 수준입니다.

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **실시간 음성 번역** | 마이크 입력 → VAD 침묵 감지 → Gemini 스트리밍 번역 → 채팅 UI 표시 → TTS 음성 출력 |
| **이미지 번역** | 카메라 촬영 → Gemini OCR + 번역 → 결과 텍스트 표시 |
| **날씨** | GPS 현재 위치 날씨 / 도시명 검색 / 도시 목록 DataStore 영속화 |
| **환율 계산** | Frankfurter API 실시간 환율 / 9개 통화 지원 |

---

## 기술 스택

| 영역 | 라이브러리 |
|------|-----------|
| UI | Jetpack Compose, Material 3, Navigation Compose |
| 아키텍처 | Clean Architecture, MVVM, Single-Activity |
| DI | Koin |
| AI / 번역 | Firebase Vertex AI (Gemini 2.5 Flash) |
| 네트워크 | Ktor (Android 엔진), kotlinx.serialization |
| 비동기 | Kotlin Coroutines, Flow |
| 로컬 저장소 | DataStore Preferences |
| TTS | Android TextToSpeech |
| 음성 처리 | AudioRecord (16kHz Mono PCM 16bit) |
| 위치 | Google Play Services FusedLocationProviderClient |
| 날씨 API | OpenWeatherMap API v2.5 |
| 환율 API | Frankfurter API |
| 로깅 | orhanobut/logger |

---

## 프로젝트 구조

```
composeApp/src/
├── commonMain/           # Android/iOS 공유 코드
│   └── kotlin/com/jh/livetransfer/
│       ├── data/
│       │   ├── model/    # ChatMessage, ExchangeResponse, WeatherModel, ...
│       │   ├── remote/   # KTorClient (Ktor HttpClient 싱글톤)
│       │   └── repository/
│       │       └── ExchangeRepositoryImpl.kt  # Frankfurter API
│       └── domain/
│           ├── repository/   # ExchangeRepository, TranslationRepository, WeatherRepository
│           └── usecase/      # exchange / translation / weather UseCase
│
├── androidMain/          # Android 전용 구현
│   └── kotlin/com/jh/livetransfer/
│       ├── LiveTransferApplication.kt  # Koin 초기화
│       ├── MainActivity.kt             # Single-Activity, BottomNav 3탭
│       ├── data/
│       │   ├── repository/
│       │   │   ├── TranslationRepositoryImpl.kt  # Gemini 호출
│       │   │   ├── WeatherRepositoryImpl.kt       # OpenWeatherMap 호출
│       │   │   └── SettingsRepository.kt          # 언어/속도 설정 DataStore
│       │   └── source/local/
│       │       └── CityDataStore.kt  # 도시 목록 DataStore
│       ├── di/
│       │   ├── AppModule.kt       # GenerativeModel, DataStore
│       │   ├── DataModule.kt      # Repository, Audio 유틸
│       │   ├── UseCaseModule.kt   # UseCase (factory)
│       │   └── ViewModelModule.kt # ViewModel
│       ├── ui/
│       │   ├── screen/translation/ # 번역 화면, 설정, 파형 뷰, ViewModel
│       │   ├── screen/weather/     # 날씨 메인, 도시 추가, ViewModel
│       │   ├── screen/exchangerate/ # 환율 화면, ViewModel
│       │   └── theme/              # Material 3 테마
│       └── util/
│           ├── AudioCaptureManager.kt  # AudioRecord Flow 래퍼
│           ├── VadProcessor.kt         # VAD 상태 머신
│           ├── TtsManager.kt           # TextToSpeech 래퍼
│           ├── AudioUtil.kt            # PCM 진폭 계산, WAV 헤더 생성
│           ├── LanguageDetector.kt     # 정규식 언어 감지
│           ├── LocationUtil.kt         # FusedLocation 래퍼
│           ├── PermissionManager.kt    # 권한 확인
│           └── L.kt                    # Logger 유틸
│
└── iosMain/              # iOS 진입점 (stub)
    └── kotlin/com/jh/livetransfer/
        └── MainViewController.kt
```

---

## 아키텍처

```
UI (Compose Screen)
    │  observe StateFlow / SharedFlow
    ▼
ViewModel
    │  invoke
    ▼
UseCase
    │  call
    ▼
Repository (interface)
    │  implement
    ▼
RepositoryImpl
    │  use
    ▼
DataSource (Gemini / Ktor / AudioRecord / DataStore)
```

- **Clean Architecture**: Domain 계층이 Data/UI 계층에 의존하지 않음
- **MVVM**: ViewModel이 StateFlow/SharedFlow로 단방향 데이터 흐름 유지
- **DI**: Koin으로 런타임 의존성 주입

---

## 음성 번역 흐름

```
마이크
  │ AudioCaptureManager.startRecording()
  ▼ Flow<ByteArray> (PCM 16kHz)
VadProcessor
  │ 진폭 계산 → AmplitudeUpdate 이벤트
  │ 침묵 감지(silenceDurationMs) → ChunkReady(pcmData)
  ▼ Flow<VadEvent>
TranslationViewModel.processVadChunk()
  │ AudioUtil.addWavHeader() → WAV 바이트 생성
  │ TranslateAudioStreamUseCase(wavBytes, langA, langB)
  ▼ Flow<String> (Gemini 스트리밍 청크)
ChatMessage 업데이트 (실시간 채팅 UI)
  │
TtsManager.speak()  ← 세션 종료 시 전체 텍스트 TTS 출력
```

---

## VAD 속도 설정

| 설정 | 침묵 감지 대기 | 특징 |
|------|--------------|------|
| 빠름 (FAST) | 600ms | 짧은 침묵에도 즉시 번역 시작 |
| 보통 (NORMAL) | 1000ms | 기본값, 자연스러운 발화 인식 |
| 느림 (SLOW) | 1500ms | 발화 완료를 더 기다린 후 번역 |

---

## 지원 언어

한국어, 영어, 일본어, 중국어, 스페인어, 프랑스어, 독일어

---

## 환경 설정

### 사전 요구사항
- Android Studio Hedgehog 이상
- JDK 11 이상
- Firebase 프로젝트 (Vertex AI 활성화 필요)
- `google-services.json` → `composeApp/` 디렉토리에 배치

### 빌드 (Android)

```shell
# macOS / Linux
./gradlew :composeApp:assembleDebug

# Windows
.\gradlew.bat :composeApp:assembleDebug
```

### 빌드 (iOS)

`/iosApp` 디렉토리를 Xcode에서 열어 실행합니다.

---

## 알려진 이슈 / TODO

| 항목 | 내용 |
|------|------|
| API 키 하드코딩 | `WeatherRepositoryImpl`의 OpenWeatherMap API 키가 소스코드에 노출됨. 프로덕션 배포 전 `local.properties` + `BuildConfig`로 분리 필요 |
| `AudioRecorder.kt` | `AudioCaptureManager`와 기능 중복인 stub 클래스. 통합 또는 제거 검토 |
| `VertexAiDataSource.kt` | DI 미연결 stub. DataSource 계층을 분리할 경우 확장 가능 |
| `WavUtil.kt` | `AudioUtil.addWavHeader`와 동일 기능 중복. 정리 필요 |
| iOS 구현 | UI 및 기능 구현 미완성 (Compose 진입점만 존재) |
| 릴리즈 빌드 로그 | `L.kt`의 `PRINT_LOG = true` → 배포 전 `false`로 변경 필요 |
| 언어 감지 정확도 | `LanguageDetector`가 정규식 기반으로 es/fr/de 감지 불가. ML Kit Language ID 도입 고려 |

---

## 레퍼런스

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Firebase Vertex AI for Android](https://firebase.google.com/docs/vertex-ai/get-started?platform=android)
- [OpenWeatherMap API](https://openweathermap.org/current)
- [Frankfurter API](https://www.frankfurter.app/docs/)
