package com.jh.livetransfer.domain.usecase.exchange


/**
 * 환율 맵을 이용해 금액을 환산하는 UseCase.
 *
 * @param rates [ExchangeResponse.rates] — 대상 통화 코드 → 환율 비율 맵
 * @param target 환산할 대상 통화 코드 (예: "KRW")
 * @param amount 환산할 금액 문자열. 숫자로 파싱 불가 시 1.0으로 폴백.
 * @return 환산 결과. [target]이 [rates]에 없으면 null.
 */
class ConvertCurrencyUseCase  {
    operator fun invoke(
        rates: Map<String, Double>,
        target: String,
        amount : String
    ) : Double? {
        val rate = rates[target] ?: return null
        val amountDouble = amount.toDoubleOrNull() ?: 1.0
        return rate * amountDouble
    }
}