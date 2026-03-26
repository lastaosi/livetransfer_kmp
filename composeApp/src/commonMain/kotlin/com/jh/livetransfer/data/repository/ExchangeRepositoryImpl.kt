package com.jh.livetransfer.data.repository

import com.jh.livetransfer.data.model.ExchangeResponse
import com.jh.livetransfer.data.remote.KTorClient
import com.jh.livetransfer.domain.repository.ExchangeRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * Frankfurter API를 통해 환율 정보를 조회하는 Repository.
 *
 * Ktor HttpClient로 REST 호출. 내부에서 try-catch로 감싸 [Result]로 반환하므로
 * 호출부에서 예외 처리 없이 [Result.onSuccess] / [Result.onFailure]로 분기 가능.
 */
class ExchangeRepositoryImpl : ExchangeRepository {
    private val client = KTorClient.client
    private val baseUrl = "https://api.frankfurter.app"

    /**
     * 기준 통화 대비 최신 환율 목록을 조회한다.
     *
     * @param base 기준 통화 코드 (기본값: "USD")
     * @return 성공 시 [ExchangeResponse], 실패 시 예외를 담은 [Result]
     */
    override suspend fun getExchangeRates(base: String): Result<ExchangeResponse> {
        return try {
            val response = client.get("$baseUrl/latest"){
                parameter("from",base)
            }
            Result.success(response.body<ExchangeResponse>())
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}