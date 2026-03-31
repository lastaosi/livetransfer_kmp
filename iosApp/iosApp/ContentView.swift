import UIKit
import SwiftUI


struct ContentView: View {
    var body: some View {
        TabView {
            WeatherView()
                .tabItem{
                    Label("날씨", systemImage: "cloud.sun.fill")
                }
            ExchangeView()
                .tabItem{
                    Label("환율", systemImage: "dollarsign.circle.fill")
                }
        }
        
    }
}



