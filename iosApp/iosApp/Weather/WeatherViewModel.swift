//
// Created by LeeJungHoon on 2026. 3. 30..
//

import Foundation
import ComposeApp
import CoreLocation
import Combine

/// SwiftUI Weather 화면의 ViewModel.
/// Kotlin 측 WeatherViewModelIos를 래핑하여 @Published 프로퍼티로 UI 상태를 관리한다.
/// - LocationManager를 통해 CoreLocation 위치를 수신하고 현재 위치 날씨를 자동 조회한다.
/// - Combine을 사용해 위치 업데이트를 1회만 구독하여 중복 API 호출을 방지한다.
class WeatherViewModel : ObservableObject{
    @Published var weatherList:[WeatherResponse] = []
    @Published var currentWeather : WeatherResponse? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    private let viewModel : WeatherViewModelIos
    private let locationManager = LocationManager()
    private var cancellables = Set<AnyCancellable>()

    init(){
        self.viewModel = KoinInitializerKt.getWeatherViewModel()
        observeLocation()
        observeCityList()
    }

    /// LocationManager의 location 변경을 구독해 현재 위치 날씨를 1회 조회한다.
    /// .first()로 최초 위치 수신 시에만 API 호출하여 중복 요청을 막는다.
    private func observeLocation(){
        locationManager.$location
            .compactMap { $0 }
            .first()
            .sink { [weak self] location in
                self?.fetchCurrentLocationWeather(
                    lat: location.coordinate.latitude,
                    lon: location.coordinate.longitude
                )
            }
            .store(in: &cancellables)
    }

    /// Kotlin Flow(observeCityList)를 콜백으로 구독해 weatherList를 최신 상태로 유지한다.
    private func observeCityList() {
        viewModel.observeCityList { [weak self] list in
            DispatchQueue.main.async {
                print("observeCityList 업데이트: \(list.count)개")
                self?.weatherList = list as [WeatherResponse]
            }
        }
    }

    /// 위치 권한 요청 및 현재 위치 조회를 시작한다.
    func requestLocation(){
        locationManager.requestLocation()
    }

    /// 위도/경도로 현재 위치 날씨를 조회하고 currentWeather를 갱신한다.
    private func fetchCurrentLocationWeather(lat:Double, lon:Double){
        isLoading = true
        viewModel.fetchWeatherByLocation(lat: lat, lon: lon) {[weak self] response in
            DispatchQueue.main.async {
                self?.isLoading = false
                self?.currentWeather = response
            }
        }
    }

    /// 도시명으로 날씨를 조회해 목록에 추가한다. 실패 시 errorMessage를 설정한다.
    func addCity(_ city: String) {
        isLoading = true
        print("addCity 호출: \(city)")
        viewModel.fetchWeatherByCity(city: city) { [weak self] response in
            DispatchQueue.main.async {
                print("addCity 콜백: \(String(describing: response))")
                self?.isLoading = false
                if response == nil {
                    self?.errorMessage = "도시를 찾을 수 없어요"
                }
            }
        }
    }

    /// 도시를 목록에서 제거하고 DataStore(NSUserDefaults)에 변경 사항을 저장한다.
    func removeCity(cityName: String) {
        Task {
            try await viewModel.removeCity(cityName: cityName) { [weak self] in
                DispatchQueue.main.async {
                    self?.weatherList = self?.weatherList.filter { $0.name != cityName } ?? []
                }
            }
        }
    }
}
