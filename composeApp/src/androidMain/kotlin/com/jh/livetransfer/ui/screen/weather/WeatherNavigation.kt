package com.jh.livetransfer.ui.screen.weather

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * 날씨 탭 내부 중첩 네비게이션.
 *
 * 라우트:
 * - "weather_main": 현재 위치 + 추가 도시 날씨 목록 ([WeatherMainScreen])
 * - "weather_settings": 도시 추가 화면 ([WeatherSettingScreen])
 *
 * [WeatherViewModel]을 단일 인스턴스로 공유하여 도시 목록 상태를 유지한다.
 */
@Composable
fun WeatherNavigation(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "weather_main"
    ) {
        composable("weather_main") {
            WeatherMainScreen(
                viewModel,
                onSettingsClick = {
                    navController.navigate("weather_settings")
                }
            )
        }
        composable(route = "weather_settings") {
            WeatherSettingScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}