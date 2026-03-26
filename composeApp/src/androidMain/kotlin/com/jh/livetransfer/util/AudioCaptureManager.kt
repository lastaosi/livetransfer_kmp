package com.jh.livetransfer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android AudioRecord 래퍼. 마이크 입력을 PCM ByteArray Flow로 방출한다.
 *
 * - 포맷: 16kHz / Mono / PCM 16bit (Gemini API 권장 포맷)
 * - [isRecording]: AtomicBoolean으로 멀티스레드 안전 상태 관리
 * - [stopRecording]: @Synchronized + recordingState 이중 체크로 중복 호출 방어
 * - Flow는 [VadProcessor]에서 수집되며, stopRecording() 호출 시 while 루프 탈출 → Flow 자연 완료
 */
class AudioCaptureManager (
     private val context: Context
) {
    // Gemini API / STT는 16kHz Mono PCM을 권장
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null

    // 멀티스레드 환경에서 녹음 상태를 안전하게 관리하기 위해 AtomicBoolean 사용
    private val isRecording = AtomicBoolean(false)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(): Flow<ByteArray> = flow{
        // 버퍼 사이즈 계산 : 단말기 하드웨어에 맞는 최소 버퍼 크기를 가져옴
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat)

        // 권한 방어 로직(UI에서 체크하지만 시스템 안정성을 위해)
        if(ActivityCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            throw SecurityException("마이크 권한이 없습니다.")
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        )
        audioRecord?.startRecording()
        isRecording.set(true)

        val buffer = ByteArray(minBufferSize)

        // flow 블록 내부에서 루프를 돌며 데이터를 방출(emit)
        while(isRecording.get() && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING){
            val readSize = audioRecord?.read(buffer,0,minBufferSize) ?: 0

            if(readSize >0 ){
                emit(buffer.copyOf(readSize))
            }
        }
    } .flowOn(Dispatchers.IO)

    @Synchronized
    fun stopRecording() {
        isRecording.set(false)
        audioRecord?.apply {
            if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                stop()
            }
            release()
        }
        audioRecord = null
    }

}
