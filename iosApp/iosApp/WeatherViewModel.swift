//
// Created by LeeJungHoon on 2026. 3. 30..
//

import Foundation
import ComposeApp

class WeatherViewModel : ObservableObject{
    @Published var weatherList:[WeatherResponse] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    private let viewModel : WeatherViewModelIos

    init(){
        self.viewModel = KoinInitializerKt.getWeatherViewModel()
    }

    func addCity(_ city: String) {
        isLoading = true
        viewModel.fetchWeatherByCity(city: city) { [weak self] response in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let response = response {
                    self?.weatherList.append(response)
                } else {
                    self?.errorMessage = "도시를 찾을 수 없어요"
                }
            }
        }
    }

    func removeCity(cityName: String) {
        weatherList.removeAll { $0.name == cityName }
    }
}