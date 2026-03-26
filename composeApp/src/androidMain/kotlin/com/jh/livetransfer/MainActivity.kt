package com.jh.livetransfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHost
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jh.livetransfer.ui.screen.exchangerate.ExchangeRateScreen
import com.jh.livetransfer.ui.screen.translation.TranslationNavigation
import com.jh.livetransfer.ui.screen.translation.TranslationScreen
import com.jh.livetransfer.ui.screen.translation.TranslationViewModel
import com.jh.livetransfer.ui.screen.weather.WeatherNavigation
import com.jh.livetransfer.ui.screen.weather.WeatherViewModel
import com.jh.livetransfer.ui.theme.LiveTransferTheme
import org.koin.androidx.compose.koinViewModel


/**
 * 앱의 유일한 Activity. Single-Activity 아키텍처.
 * Compose setContent로 루트 UI를 설정하고 이후 모든 화면은 Navigation Compose가 담당한다.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveTransferTheme {
                AppNavigation()
            }
        }
    }
}

/**
 * 앱 최상위 네비게이션 구조.
 * BottomNavigationBar로 3개 탭(번역 / 날씨 / 환율)을 전환하며,
 * 각 탭은 내부에 별도 NavHost(중첩 네비게이션)를 가진다.
 *
 * - Translation 탭: TranslationNavigation (번역 ↔ 설정)
 * - Weather 탭: WeatherNavigation (날씨 메인 ↔ 날씨 설정)
 * - Exchange 탭: ExchangeRateScreen (단일 화면, 미구현)
 */
@Composable
private fun AppNavigation() {
    val navController = rememberNavController()

    val items = listOf(BottomNavItem.Translation, BottomNavItem.Weather, BottomNavItem.Exchange)
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {Icon(item.icon, contentDescription = item.label)},
                        label = {Text(item.label)},
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route){
                                // 뒤로가기 시 스택이 쌓이지 않도록 startDestination까지 팝
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) {paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Translation.route,
            modifier = Modifier.padding(paddingValues)
        ){
            composable(BottomNavItem.Translation.route){
                // ViewModel을 여기서 생성해 TranslationNavigation 전체에 공유
                val viewModel: TranslationViewModel = koinViewModel()
                TranslationNavigation(viewModel = viewModel)
            }

            composable(BottomNavItem.Exchange.route){
                ExchangeRateScreen()
            }
            composable(BottomNavItem.Weather.route){
                val viewModel: WeatherViewModel = koinViewModel()
                WeatherNavigation(viewModel = viewModel)
            }
        }
    }

}

/**
 * 하단 네비게이션 탭 정의.
 * sealed class로 타입 안전하게 관리하며, route/label/icon을 한 곳에서 선언.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Translation : BottomNavItem(
        route = "translation",
        label = "번역",
        icon = Icons.Default.Translate
    )

    object Weather : BottomNavItem(
        route = "weather",
        label = "날씨",
        icon = Icons.Default.Cloud
    )

    object Exchange : BottomNavItem("exchange", "환율", Icons.Outlined.CurrencyExchange)
}
