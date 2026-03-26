package com.jh.livetransfer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * 앱 필수 권한 확인 유틸리티.
 *
 * 현재는 RECORD_AUDIO만 체크하지만, 배열 방식으로 권한을 관리하므로
 * 카메라 등 추가 권한 확장이 용이하다.
 *
 * 참고: 실제 권한 요청(런처)은 각 Composable에서 직접 처리 중.
 */
object PermissionManager {
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    /** 정의된 모든 권한이 허용되었는지 확인 */
    fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
