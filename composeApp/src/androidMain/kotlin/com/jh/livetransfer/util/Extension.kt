package com.jh.livetransfer.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 시스템 폰트 크기 설정(접근성)에 영향받지 않는 고정 sp 확장 프로퍼티.
 *
 * Android 접근성 설정에서 폰트 크기를 키워도 이 값은 변하지 않는다.
 * UI 레이아웃 깨짐을 방지하고 싶은 컴포넌트에만 제한적으로 사용해야 하며,
 * 일반 텍스트에는 접근성을 위해 기본 sp를 유지하는 것이 권장된다.
 */

// Int용 (예: 16.nonScaledSp)
val Int.nonScaledSp: TextUnit
    @Composable
    get() = (this / LocalDensity.current.fontScale).sp

// Float용 (예: 16.5f.nonScaledSp)
val Float.nonScaledSp: TextUnit
    @Composable
    get() = (this / LocalDensity.current.fontScale).sp