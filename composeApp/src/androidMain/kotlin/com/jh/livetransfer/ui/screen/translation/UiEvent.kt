package com.jh.livetransfer.ui.screen.translation

/**
 * ViewModel → UI 단방향 이벤트 정의.
 *
 * StateFlow가 아닌 SharedFlow(extraBufferCapacity=1)로 방출되어 one-shot 처리된다.
 * 현재는 Toast 표시만 있으나 Snackbar, Dialog 등 확장 가능.
 */
sealed class UiEvent {
    /** Toast 메시지 표시 요청 */
    data class ShowToast(val message: String) : UiEvent()
}
