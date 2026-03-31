import SwiftUI
import ComposeApp

struct ExchangeView: View {
    @StateObject private var viewModel = ExchangeViewModel()
    
    let currencies = ["USD", "KRW", "JPY", "EUR", "GBP", "CNY", "HKD", "SGD", "CHF"]
    
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                
                // 통화 선택 영역
                HStack(spacing: 12) {
                    // 기준 통화
                    VStack(alignment: .leading, spacing: 6) {
                        Text("기준 통화")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Picker("기준 통화", selection: $viewModel.baseCurrency) {
                            ForEach(currencies, id: \.self) { currency in
                                Text(currency).tag(currency)
                            }
                        }
                        .pickerStyle(MenuPickerStyle())
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                    }
                    
                    // 교환 버튼
                    Button(action: {
                        let temp = viewModel.baseCurrency
                        viewModel.baseCurrency = viewModel.targetCurrency
                        viewModel.targetCurrency = temp
                    }) {
                        Image(systemName: "arrow.left.arrow.right")
                            .font(.title2)
                            .foregroundColor(.blue)
                    }
                    .padding(.top, 20)
                    
                    // 대상 통화
                    VStack(alignment: .leading, spacing: 6) {
                        Text("대상 통화")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Picker("대상 통화", selection: $viewModel.targetCurrency) {
                            ForEach(currencies, id: \.self) { currency in
                                Text(currency).tag(currency)
                            }
                        }
                        .pickerStyle(MenuPickerStyle())
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                    }
                }
                
                // 금액 입력
                VStack(alignment: .leading, spacing: 6) {
                    Text("금액")
                        .font(.caption)
                        .foregroundColor(.gray)
                    HStack {
                       
                        TextField("금액 입력", text: $viewModel.amount)
                            .keyboardType(.decimalPad)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                }
                
                // 결과
                if viewModel.isLoading {
                    ProgressView()
                        .padding()
                } else if let result = viewModel.convertedAmount {
                    VStack(spacing: 8) {
                        Text("환산 결과")
                            .font(.caption)
                            .foregroundColor(.gray)
                        HStack(alignment: .firstTextBaseline, spacing: 8) {
                            
                            Text(String(format: "%.2f", result))
                                .font(.system(size: 42, weight: .bold))
                                .foregroundColor(.blue)
                            Text(viewModel.targetCurrency)
                                .font(.title2)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color(.systemGray6))
                    .cornerRadius(16)
                }
                
                Spacer()
            }
            .padding()
            .navigationTitle("환율")
        }
    }
    
    /// 통화 코드를 2자리 국가 코드로 변환한다. 국기 이모지 등 표시 목적으로 사용 가능.
    func currencyCode(_ currency: String) -> String {
        switch currency {
        case "USD": return "US"
        case "KRW": return "KR"
        case "JPY": return "JP"
        case "EUR": return "EU"
        case "GBP": return "GB"
        case "CNY": return "CN"
        case "HKD": return "HK"
        case "SGD": return "SG"
        case "CHF": return "CH"
        default:    return "--"
        }
    }
}
