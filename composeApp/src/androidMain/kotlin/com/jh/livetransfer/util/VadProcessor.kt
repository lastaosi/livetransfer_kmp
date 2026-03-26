package com.jh.livetransfer.util

import android.Manifest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

sealed class VadEvent {
    /** 매 PCM 청크마다 계산된 정규화 진폭 (0.0~1.0) */
    data class AmplitudeUpdate(val normalized: Float) : VadEvent()

    /** 침묵이 감지된 후 확정된 발화 구간의 PCM 원본 데이터 */
    data class ChunkReady(val pcmData: ByteArray) : VadEvent()
}

/**
 * VAD(Voice Activity Detection) 상태 머신을 담당합니다.
 *
 * - 오디오 버퍼 누적
 * - 진폭 계산 및 침묵 감지
 * - 침묵 지속 시 [VadEvent.ChunkReady] 방출
 * - 녹음 종료 시 버퍼에 남은 데이터를 마지막 [VadEvent.ChunkReady]로 방출
 */
class VadProcessor (
    private val audioCaptureManager: AudioCaptureManager
) {
    companion object {
        /** WAV 헤더 없이 최소 의미 있는 오디오 크기 (16kHz × 16bit × 0.5초) */
        private const val MIN_PCM_BYTES = 16_000
    }

    /**
     * 녹음을 시작하고 VAD 이벤트 Flow를 반환합니다.
     * 녹음 중지는 [stopRecording]으로 시그널을 보내면 Flow가 자연스럽게 완료됩니다.
     * Flow 완료 직전, 버퍼에 남은 데이터가 있으면 마지막 [VadEvent.ChunkReady]를 방출합니다.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startCapture(
        silenceThreshold: Float,
        silenceDurationMs: Long
    ): Flow<VadEvent> = callbackFlow {
        val audioBuffer = ByteArrayOutputStream()
        var hasSpoken = false
        var silenceJob: Job? = null

        // AudioCaptureManager의 Flow가 완료(isRecording=false)될 때까지 수집
        audioCaptureManager.startRecording().collect { byteData ->
            audioBuffer.write(byteData)

            val amplitude = AudioUtil.calculateAmplitude(byteData)
            send(VadEvent.AmplitudeUpdate(amplitude))

            if (amplitude > silenceThreshold) {
                hasSpoken = true
                silenceJob?.cancel()
                silenceJob = null
            } else if (hasSpoken && silenceJob == null) {
                silenceJob = launch {
                    delay(silenceDurationMs)
                    val pcmData = audioBuffer.toByteArray()
                    audioBuffer.reset()
                    hasSpoken = false
                    silenceJob = null
                    if (pcmData.size >= MIN_PCM_BYTES) {
                        send(VadEvent.ChunkReady(pcmData))
                    }
                }
            }
        }

        // 녹음이 자연스럽게 종료됨 — 남은 버퍼 플러시
        silenceJob?.cancelAndJoin()
        val remaining = audioBuffer.toByteArray()
        if (remaining.size >= MIN_PCM_BYTES) {
            send(VadEvent.ChunkReady(remaining))
        }

        channel.close()

        // 하위 수집자가 취소한 경우(비정상 종료)의 정리
        awaitClose { audioCaptureManager.stopRecording() }
    }.flowOn(Dispatchers.IO)

    /** AudioCaptureManager에 정지 신호를 보내 Flow가 자연스럽게 완료되도록 합니다. */
    fun stopRecording() = audioCaptureManager.stopRecording()
}
