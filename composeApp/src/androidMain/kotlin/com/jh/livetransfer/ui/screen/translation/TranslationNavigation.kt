package com.jh.livetransfer.ui.screen.translation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * 번역 탭 내부 중첩 네비게이션.
 *
 * ViewModel은 MainActivity에서 생성되어 이 함수로 전달된다.
 * translation_home ↔ settings 간 화면 전환을 담당.
 *
 * 라우트:
 * - "translation_home": 실시간 번역 메인 화면
 * - "settings": 언어 및 VAD 속도 설정 화면 (popBackStack으로 복귀)
 */
@Composable
fun TranslationNavigation(viewModel: TranslationViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "translation_home"
    ) {
        composable("translation_home") {
            TranslationScreen(
                viewModel = viewModel,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}