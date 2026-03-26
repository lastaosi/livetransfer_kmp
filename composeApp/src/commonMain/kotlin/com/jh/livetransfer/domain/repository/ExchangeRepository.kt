package com.jh.livetransfer.domain.repository

import com.jh.livetransfer.data.model.ExchangeResponse

/**
 * 환율 데이터 Domain 계층 인터페이스.
 * 구현체: [ExchangeRepositoryImpl] (Frankfurter API 호출).
 */
interface ExchangeRepository {
    /**
     * 기준 통화 대비 최신 환율 목록을 조회한다.
     * @param base 기준 통화 코드 (기본값: "USD")
     */
    suspend fun getExchangeRates(base: String = "USD"): Result<ExchangeResponse>
}