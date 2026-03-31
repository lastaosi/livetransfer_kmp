//
//  ExchangeViewModel.swift
//  iosApp
//
//  Created by LeeJungHoon on 3/31/26.
//
import Foundation
import ComposeApp

/// SwiftUI Exchange 화면의 ViewModel.
/// Kotlin 측 ExchangeViewModelIos를 래핑하며, 통화·금액 변경 시 didSet으로 자동 환산을 트리거한다.
class ExchangeViewModel : ObservableObject {
    /// 기준 통화 (변경 시 자동으로 convert() 호출)
    @Published var baseCurrency: String = "USD"{
        didSet {
            convert()
        }
    }

    /// 대상 통화 (변경 시 자동으로 convert() 호출)
    @Published var targetCurrency: String = "KRW"{
        didSet {
            convert()
        }
    }

    /// 변환할 금액 문자열 (변경 시 자동으로 convert() 호출)
    @Published var amount: String = "1" {
        didSet {
            convert()
        }
    }
    @Published var convertedAmount: Double? = nil
    @Published var isLoading:Bool = false

    private let viewModel: ExchangeViewModelIos

    init(){
        self.viewModel = KoinInitializerKt.getExchangeViewModel()
        convert()
    }

    /// Kotlin ExchangeViewModelIos.convert()를 호출해 환율 계산 결과를 수신한다.
    /// 결과는 콜백으로 전달되며 DispatchQueue.main에서 UI 상태를 업데이트한다.
    func convert(){
        isLoading = true
        viewModel.convert(base:baseCurrency,target:targetCurrency,amount:amount){
            [weak self] result in
            DispatchQueue.main.async {
                self?.isLoading = false
                self?.convertedAmount = result?.doubleValue
                }
            }
        }
    
    
    

}
