package com.jh.livetransfer.ui.screen.weather

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf

/**
 * 날씨 도시 추가 화면.
 *
 * 사용자가 도시명(영문)을 입력하고 "추가" 버튼을 누르면 [WeatherViewModel.addCity]를 호출한 뒤
 * [onBackClick]으로 이전 화면([WeatherMainScreen])으로 복귀한다.
 * 입력 필드 우측의 X 버튼으로 텍스트 초기화 가능.
 *
 * @param viewModel 도시 추가 로직을 처리하는 [WeatherViewModel]
 * @param onBackClick 뒤로가기 / 추가 완료 후 복귀 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSettingScreen(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit
) {
    var cityInput by remember { mutableStateOf("")}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("도시 추가") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ){paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = {cityInput = it},
                label = { Text("도시명 입력(영문)")},
                placeholder = {Text("예: 서울,Seoul,Tokyo")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (cityInput.isNotEmpty()) {
                        IconButton(onClick = { cityInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "지우기")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if(cityInput.isNotBlank()){
                        viewModel.addCity(cityInput.trim())
                        cityInput = ""
                        onBackClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cityInput.isNotBlank()
            ) {
                Text("추가")
            }

        }

    }


}
