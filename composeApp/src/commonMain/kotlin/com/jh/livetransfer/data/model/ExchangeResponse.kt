package com.jh.livetransfer.data.model

import kotlinx.serialization.Serializable

/**
 * Frankfurter API의 최신 환율 응답 모델.
 *
 * @property base 기준 통화 코드 (예: "USD")
 * @property date 환율 기준일 (예: "2026-03-25")
 * @property rates 대상 통화 코드 → 환율 비율 맵 (예: {"KRW": 1320.5, "EUR": 0.92})
 */
@Serializable
data class ExchangeResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)