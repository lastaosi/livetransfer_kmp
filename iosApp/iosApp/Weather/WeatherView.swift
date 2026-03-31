//
//  WeatherView.swift
//  iosApp
//
//  Created by LeeJungHoon on 3/30/26.
//

import SwiftUI
import ComposeApp

struct WeatherView: View {
    @StateObject private var viewModel = WeatherViewModel()
    @State private var showAddCity = false
    
    var body: some View {
        NavigationView {
            List {
                if let current = viewModel.currentWeather {
                    Section("현재 위치"){
                        WeatherRow(weather: current)
                    }
                }
                
                // 추가된 도시 목록
               if !viewModel.weatherList.isEmpty {
                   Section("추가된 도시") {
                       ForEach(viewModel.weatherList, id: \.name) { weather in
                           WeatherRow(weather: weather)
                       }
                       .onDelete { indexSet in
                           indexSet.forEach { index in
                               viewModel.removeCity(cityName: viewModel.weatherList[index].name)
                           }
                       }
                   }
               }
                
                if viewModel.currentWeather == nil && viewModel.weatherList.isEmpty{
                    Text("도시를 추가하거나 위치 권한을 허용해주세요")
                        .foregroundColor(.gray)
                        .frame(maxWidth:.infinity, alignment: .center)
                        .padding()
                }
            }
            .navigationTitle("날씨")
            .toolbar{
                ToolbarItem(placement: .navigationBarTrailing){
                    Button(action: {
                        print("+버튼 클릭")
                        showAddCity = true}){
                        Image(systemName: "plus")
                    }
                }
                ToolbarItem(placement: .navigationBarLeading){
                    Button(action: {viewModel.requestLocation()}){
                        Image(systemName: "location.fill")
                    }
                }
            }.sheet(isPresented: $showAddCity){
                AddCityView(viewModel: viewModel, isPresented: $showAddCity)
            }
            .onAppear{
                viewModel.requestLocation()
            }
        }
    }
}

struct WeatherRow: View {
    let weather: WeatherResponse
    
    
    var body: some View {
        HStack {
            // 날씨 이모지
           
            Image(systemName: weatherIcon(icon: weather.weather.first?.icon ?? ""))
                .font(.system(size: 36))
                .foregroundColor(.blue)
                .frame(width:44)
//            Text(weatherEmoji(icon: weather.weather.first?.icon ?? "none"))
//                .font(.system(size: 40))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(weather.name)
                    .font(.headline)
                
                if let w = weather.weather.first {
                                  Text(w.weatherDesc)
                                      .font(.subheadline)
                                      .foregroundColor(.gray)
                              }
                HStack(spacing:12){
                    Label("\(weather.main.humidity)%", systemImage: "humidity.fill")
                        .font(.caption)
                        .foregroundColor(.blue)
                    Label(String(format: "%.1fm/s",weather.wind.speed),systemImage: "wind")
                        .font(.caption)
                        .foregroundColor(.teal)
                }
                
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 4) {
                Text("\(Int(weather.main.temp))°C")
                    .font(.title2)
                    .bold()
                Text("체감 \(Int(weather.main.feels_like))°C")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 4)
    }
    
    
    /// OpenWeatherMap 아이콘 코드를 SF Symbol 이름으로 변환한다.
    /// icon 코드 앞 두 자리만 사용 (뒤의 "d"/"n" 주야간 구분자는 무시).
    /// String(icon.prefix(2))로 명시적 String 변환 필요 — Substring은 switch 매칭 불가.
    func weatherIcon(icon: String) -> String {
        let prefix = String(icon.prefix(2))
        switch prefix {
        case "01": return "sun.max.fill"
        case "02": return "cloud.sun.fill"
        case "03": return "cloud.fill"
        case "04": return "smoke.fill"
        case "09": return "cloud.drizzle.fill"
        case "10": return "cloud.rain.fill"
        case "11": return "cloud.bolt.fill"
        case "13": return "snowflake"
        case "50": return "cloud.fog.fill"
        default:   return "thermometer"
        }
    }
}

/// 도시명을 입력받아 날씨 목록에 추가하는 시트 뷰.
/// WeatherView에서 sheet로 표시되며, 추가 완료 또는 취소 시 isPresented를 false로 설정해 닫는다.
struct AddCityView: View {
    @ObservedObject var viewModel: WeatherViewModel
    @Binding var isPresented: Bool
    @State private var cityInput = ""
    
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                TextField("도시명 입력 (예: Seoul, Tokyo)", text: $cityInput)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding()
                
                Button(action: {
                    if !cityInput.isEmpty {
                        isPresented = false
                        viewModel.addCity(cityInput)
                    }
                }) {
                    Text("추가")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                .padding(.horizontal)
                .disabled(cityInput.isEmpty)
                
                Spacer()
            }
            .navigationTitle("도시 추가")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소") {
                        isPresented = false
                    }
                }
            }
        }
    }
    
    
}
