package com.jh.livetransfer.util.audio_recorder

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 * AudioRecord 래퍼 클래스. [STUB / 미사용]
 *
 * 실제 녹음 로직은 [AudioCaptureManager]가 Flow 기반으로 구현되어 있으며,
 * 이 클래스는 초기 뼈대만 존재하고 DI 그래프에 연결되지 않았다.
 */
class AudioRecorder (
     private val context: Context
) {
    // TODO: AudioCaptureManager와 통합하거나 제거 검토
    fun startRecording() {
        // AudioRecord 설정 및 데이터 수집 시작
    }

    fun stopRecording() {
        // 녹음 중지 및 리소스 해제
    }
}
