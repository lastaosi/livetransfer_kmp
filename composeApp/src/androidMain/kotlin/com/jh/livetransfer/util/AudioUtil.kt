package com.jh.livetransfer.util

/**
 * PCM / WAV 오디오 유틸리티 모음.
 * 모든 메서드는 stateless이므로 object 싱글톤으로 선언.
 */
object AudioUtil {

    /**
     * PCM 16bit 데이터에서 정규화된 진폭(0.0~1.0)을 계산합니다.
     * 16bit 샘플은 리틀 엔디언 2바이트 단위이므로 step 2로 순회합니다.
     */
    fun calculateAmplitude(pcmData: ByteArray): Float {
        var maxAmplitude = 0
        var i = 0
        while (i < pcmData.size - 1) {
            val low = pcmData[i].toInt() and 0xFF
            val high = pcmData[i + 1].toInt()
            val sample = (high shl 8) or low
            val abs = kotlin.math.abs(sample.toShort().toInt())
            if (abs > maxAmplitude) maxAmplitude = abs
            i += 2
        }
        return (maxAmplitude / 32768f).coerceIn(0f, 1f)
    }

    /**
     * 순수 PCM 데이터 앞에 44바이트 WAV 헤더를 붙여 반환합니다.
     * 포맷 고정: 16kHz / 16bit / Mono (Gemini API 권장 포맷).
     *
     * WavUtil.kt의 [ByteArray.pcmToWav]와 동일 역할이나 바이트 직접 조작 방식 사용.
     */
    fun addWavHeader(pcmData: ByteArray): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val longSampleRate = 16000L
        val channels = 1
        val byteRate = 16000 * 16 * 1 / 8L

        val header = ByteArray(44)
        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // 'fmt ' chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        // 44바이트 헤더 + 원본 PCM 데이터 합치기
        val wavData = ByteArray(44 + pcmData.size)
        System.arraycopy(header, 0, wavData, 0, 44)
        System.arraycopy(pcmData, 0, wavData, 44, pcmData.size)

        return wavData
    }
}