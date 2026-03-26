package com.jh.livetransfer.domain.usecase.exchange

import com.jh.livetransfer.data.model.ExchangeResponse
import com.jh.livetransfer.domain.repository.ExchangeRepository


/** [ExchangeRepository]에서 최신 환율을 조회한다. */
class GetExchangeRatesUseCase (
    private val repository: ExchangeRepository
) {
    /** @param base 기준 통화 코드 (기본값: "USD") */
    suspend operator fun invoke(base:String="USD"): Result<ExchangeResponse>{
        return repository.getExchangeRates(base)
    }
}