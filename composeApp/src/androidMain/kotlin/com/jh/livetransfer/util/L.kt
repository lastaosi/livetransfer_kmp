package com.jh.livetransfer.util

import com.google.gson.Gson
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

/**
 * orhanobut/logger 기반 로깅 유틸리티.
 *
 * 특징:
 * - PRINT_LOG 플래그로 릴리즈 빌드에서 로그를 일괄 차단 가능
 * - methodOffset(1): L 객체를 거치는 1단계를 무시해 logcat 클릭 시 실제 호출부로 이동
 * - L.d(Any?): 객체를 자동으로 JSON 직렬화해 Pretty Print
 * - L.d(titles, contents): 제목-값 쌍을 zip으로 출력 (IndexOutOfBoundsException 방지)
 */
object L {

    // true: 항상 출력 / false: 로그 차단 (릴리즈 빌드 배포 전 false로 변경 권장)
    private val PRINT_LOG = true
    const val LOG_PREFIX = "AI_BIZ"
    private const val LOG_TAG = "${LOG_PREFIX}_LOG"
    private const val NULL = "null"

    init {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .tag(LOG_TAG)
            .methodOffset(1) // L 객체를 거치므로 offset 1 유지 (클릭 시 실제 호출부로 이동)
            .methodCount(3)
            .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?) = PRINT_LOG
        })
    }

    fun v(log: String?) = Logger.v(log ?: NULL)
    fun i(log: String?) = Logger.i(log ?: NULL)

    // 객체가 들어오면 알아서 Json으로 예쁘게 파싱
    fun d(log: Any?) = log?.let { Logger.json(Gson().toJson(it)) } ?: d(NULL)
    fun d(log: String?) = Logger.d(log ?: NULL)

    // Kotlin 'zip' 활용으로 우아하게 리팩토링 (IndexOutOfBoundsException 완벽 방지)
    fun d(titles: List<String>, contents: List<Any?>) {
        val logs = titles.zip(contents) { title, content ->
            "$title : $content"
        }.joinToString("\n")
        Logger.d(logs)
    }

    fun d(vararg items: Pair<String, Any?>) {
        Logger.d(items.joinToString("\n") { "${it.first} : ${it.second}" })
    }

    fun d(vararg items: String) {
        Logger.d(items.joinToString("\n"))
    }


    fun d(items: List<String?>) {
        Logger.d(items.joinToString("\n"))
    }

    fun w(log: String?) = Logger.w(log ?: NULL)
    fun e(log: String?) = Logger.e(log ?: NULL)
    fun e(e: Throwable) = Logger.e(e, e.toString())
    fun wtf(log: String?) = Logger.wtf(log ?: NULL)
    fun json(json: String?) = Logger.json(json ?: NULL)
    fun xml(xml: String?) = Logger.xml(xml ?: NULL)
}