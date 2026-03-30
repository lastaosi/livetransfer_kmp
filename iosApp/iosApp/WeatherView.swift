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
    @State private var cityInput = ""
    @State private var showAddCity = false
    
    var body: some View {
        NavigationView {
            VStack {
                if viewModel.weatherList.isEmpty {
                    Spacer()
                    Text("도시를 추가해주세요")
                        .foregroundColor(.gray)
                    Spacer()
                } else {
                    List {
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
            }
            .navigationTitle("날씨")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showAddCity = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddCity) {
                AddCityView(viewModel: viewModel, isPresented: $showAddCity)
            }
        }
    }
}

struct WeatherRow: View {
    let weather: WeatherResponse
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(weather.name)
                    .font(.headline)
                Text(weather.weather.first?.description ?? "")
                    .font(.subheadline)
                    .foregroundColor(.gray)
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
}

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
                        viewModel.addCity(cityInput)
                        isPresented = false
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
