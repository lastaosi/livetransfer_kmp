package com.jh.livetransfer.ui.screen.exchangerate

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * 환율 계산 화면.
 *
 * [ExchangeViewModel]을 통해 기준/대상 통화와 금액을 입력받아 실시간 환산 결과를 표시한다.
 * 에러 발생 시 Toast로 알림 후 [ExchangeViewModel.clearError]로 소비.
 *
 * @param viewModel Hilt로 주입되는 [ExchangeViewModel]
 */
/** 지원하는 통화 코드 목록 */
val currencies = listOf("USD", "KRW", "JPY", "EUR", "GBP", "CNY", "HKD", "SGD", "CHF")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(
    viewModel: ExchangeViewModel = koinViewModel()
 ) {
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val targetCurrency by viewModel.targetCurrency.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val convertedAmount by viewModel.convertedAmount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context,it,Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { Text(text = "환율 계산기") },
    ) {paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
                ) {
                CurrencyDropdown(
                    selected = baseCurrency,
                    onSelected = { viewModel.onBaseCurrencyChanged(it) },
                    label = "기준 통화",
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "->",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                CurrencyDropdown(
                    selected = targetCurrency,
                    onSelected = { viewModel.onTargetCurrencyChanged(it) },
                    label = "타겟 통화",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                OutlinedTextField(
                    value = amount,
                    onValueChange = {viewModel.onAmountChanged(it)},
                    label = {Text("금액")},
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                if(isLoading){
                    CircularProgressIndicator()
                }else{
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = convertedAmount?.let {
                                String.format("%,.2f",it)
                            } ?: "-",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = targetCurrency,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
/**
 * 통화 코드 선택 드롭다운 컴포넌트.
 *
 * [currencies] 목록을 [ExposedDropdownMenuBox]로 표시하며, 선택 시 [onSelected]를 호출한다.
 *
 * @param selected 현재 선택된 통화 코드
 * @param onSelected 통화 선택 콜백
 * @param label TextField 상단 레이블 텍스트
 * @param modifier 외부에서 전달하는 Modifier (weight 등 레이아웃 조정용)
 */
@ExperimentalMaterial3Api
@Composable
fun CurrencyDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded},
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = {Text(label)},
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {Text(currency)},
                    onClick = {
                        onSelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

