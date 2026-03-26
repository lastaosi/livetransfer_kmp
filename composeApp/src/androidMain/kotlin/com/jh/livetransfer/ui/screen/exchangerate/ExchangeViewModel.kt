package com.jh.livetransfer.ui.screen.exchangerate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jh.livetransfer.data.model.ExchangeResponse
import com.jh.livetransfer.data.repository.ExchangeRepositoryImpl
import com.jh.livetransfer.domain.usecase.exchange.ConvertCurrencyUseCase
import com.jh.livetransfer.domain.usecase.exchange.GetExchangeRatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 환율 계산 화면의 ViewModel.
 *
 * 기준 통화([baseCurrency])와 대상 통화([targetCurrency])가 변경될 때마다
 * [ExchangeRepositoryImpl]를 통해 최신 환율을 조회하고, 입력 금액([amount])에 곱해
 * [convertedAmount]를 갱신한다.
 *
 * 흐름: 사용자 입력 → [fetchExchangeRate] → [calculateResult] → [convertedAmount] 업데이트
 */
class ExchangeViewModel(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) : ViewModel() {
    /** 기준 통화 코드 (기본값: "USD") */
    private val _baseCurrency = MutableStateFlow("USD")
    val baseCurrency: StateFlow<String> = _baseCurrency

    /** 대상 통화 코드 (기본값: "KRW") */
    private val _targetCurrency = MutableStateFlow("KRW")
    val targetCurrency: StateFlow<String> = _targetCurrency

    /** 환산할 금액 문자열 (TextField 바인딩용) */
    private val _amount = MutableStateFlow("1")
    val amount: StateFlow<String> = _amount

    /** 환산 결과. API 응답 전 또는 변환 불가 시 null */
    private val _convertedAmount = MutableStateFlow<Double?>(null)
    val convertedAmount: StateFlow<Double?> = _convertedAmount

    /** API 호출 중 여부 */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** 에러 메시지. 소비 후 [clearError]로 null 처리 */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** 마지막으로 받아온 환율 응답 캐시. [calculateResult] 재계산에 활용 */
    private val _exchangeRate = MutableStateFlow<ExchangeResponse?>(null)

    /** 기준 통화 변경 시 호출. 즉시 환율 재조회 */
    fun onBaseCurrencyChanged(currency: String) {
        _baseCurrency.value = currency
        fetchExchangeRate()
    }

    /** 대상 통화 변경 시 호출. 즉시 환율 재조회 */
    fun onTargetCurrencyChanged(currency: String) {
        _targetCurrency.value = currency
        fetchExchangeRate()
    }

    /** 금액 입력 변경 시 호출. 즉시 환율 재조회 */
    fun onAmountChanged(value: String) {
        _amount.value = value
        fetchExchangeRate()
    }

    /**
     * 캐시된 환율 데이터([_exchangeRate])와 현재 입력 금액으로 환산 결과를 계산한다.
     * 파싱 불가 입력값은 1.0으로 폴백.
     */
    private fun calculateResult() {
        val rates = _exchangeRate.value?.rates ?: return
        _convertedAmount.value = convertCurrencyUseCase(
            rates = rates,
            target = _targetCurrency.value,
            amount = _amount.value
        )
    }

    /** [ExchangeRepositoryImpl]를 호출해 환율을 조회하고, 성공 시 [calculateResult]를 실행한다. */
    private fun fetchExchangeRate() {
        viewModelScope.launch {
            _isLoading.value = true
            getExchangeRatesUseCase(_baseCurrency.value)
                .onSuccess { response ->
                    _exchangeRate.value = response
                    calculateResult()
                }
                .onFailure { _errorMessage.value = "환율 정보를 가져올 수 없어요" }
            _isLoading.value = false
        }
    }

    /** 에러 메시지를 소비한 뒤 초기화한다. UI에서 Toast 표시 후 호출 */
    fun clearError() {
        _errorMessage.value = null
    }
}