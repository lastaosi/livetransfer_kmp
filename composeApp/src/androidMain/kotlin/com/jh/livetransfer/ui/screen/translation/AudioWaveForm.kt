package com.jh.livetransfer.ui.screen.translation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * 실시간 오디오 파형(Waveform) 시각화 컴포넌트.
 *
 * VAD에서 계산된 진폭 리스트를 막대 그래프 형태로 Canvas에 렌더링한다.
 * 각 막대는 중심을 기준으로 위아래로 대칭 성장(StrokeCap.Round로 둥글게 처리).
 *
 * @param amplitudes 0.0~1.0 정규화 진폭 리스트. ViewModel에서 최대 40개(MAX_BAR_COUNT)로 제한.
 * @param barColor 막대 색상. 기본값은 MaterialTheme.colorScheme.primary 권장.
 */
@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF6200EE)
) {
    Canvas(
        modifier = modifier.height(80.dp)
    ) {
        val barWidth = 10.dp.toPx()
        val gap = 4.dp.toPx()
        val centerY = size.height / 2f
        val startX = 0f

        amplitudes.forEachIndexed { index, amp ->
            // 최소 높이 10px 보장 — 완전 무음 구간에도 시각적 피드백 제공
            val barHeight = (size.height * amp * 0.8f).coerceAtLeast(10f)
            val x = startX + index * (barWidth + gap)

            drawLine(
                color = barColor,
                start = Offset(x = x, y = centerY - barHeight / 2),
                end = Offset(x = x, y = centerY + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
