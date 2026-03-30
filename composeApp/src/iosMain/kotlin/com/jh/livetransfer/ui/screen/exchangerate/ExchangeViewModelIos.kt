package com.jh.livetransfer.ui.screen.exchangerate

import com.jh.livetransfer.domain.usecase.exchange.ConvertCurrencyUseCase
import com.jh.livetransfer.domain.usecase.exchange.GetExchangeRatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExchangeViewModelIos(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
){
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _convertedAmount = MutableStateFlow<Double?>(null)
    val convertedAmount: StateFlow<Double?> = _convertedAmount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun convert(
        base:String,
        target: String,
        amount:String,
        onResult:(Double?) -> Unit
    ){
        scope.launch {
            getExchangeRatesUseCase(base)
                .onSuccess { response ->
                    val result = convertCurrencyUseCase(
                        rates = response.rates,
                        target = target,
                        amount = amount
                    )
                    _convertedAmount.value = result
                    onResult(result)
                }
                .onFailure {
                    _errorMessage.value = "환율 정보를 가져올 수 없어요"
                    onResult(null)
                }
            _isLoading.value = false
        }
    }

}