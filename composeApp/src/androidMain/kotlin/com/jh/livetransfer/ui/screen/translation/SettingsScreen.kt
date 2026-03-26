package com.jh.livetransfer.ui.screen.translation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jh.livetransfer.data.model.SpeechSpeed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jh.livetransfer.data.model.SUPPORTED_LANGUAGES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TranslationViewModel,
    onBackClick: () -> Unit
) {
    val langA by viewModel.currentLangA.collectAsState()
    val langB by viewModel.currentLangB.collectAsState()
    val currentSpeed by viewModel.currentVadSpeed.collectAsState()

    var showLangADialog by remember { mutableStateOf(false) }
    var showLangBDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통역 설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. 언어 설정 섹션
            Text("언어 선택", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LanguageSelector(label = "내 언어 (A)", selectedName = langA) { showLangADialog = true }
                LanguageSelector(label = "상대 언어 (B)", selectedName = langB) { showLangBDialog = true }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // 2. 말하기 속도(VAD) 설정 섹션
            Text("말하기 속도 감지 (VAD)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("말을 멈췄을 때 번역을 시작하기까지의 대기 시간입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            SpeechSpeed.values().forEach { speed ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (currentSpeed == speed),
                            onClick = { viewModel.setSpeechSpeed(speed) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentSpeed == speed),
                        onClick = null // Row 클릭으로 처리
                    )
                    Text(
                        text = speed.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }

    // 언어 선택 다이얼로그 (A)
    if (showLangADialog) {
        LanguageSelectionDialog(
            currentSelected = langA,
            onDismiss = { showLangADialog = false },
            onSelect = { name, code ->
                viewModel.setLangA(name, code)
                showLangADialog = false
            }
        )
    }

    // 언어 선택 다이얼로그 (B)
    if (showLangBDialog) {
        LanguageSelectionDialog(
            currentSelected = langB,
            onDismiss = { showLangBDialog = false },
            onSelect = { name, code ->
                viewModel.setLangB(name, code)
                showLangBDialog = false
            }
        )
    }
}

@Composable
fun LanguageSelector(label: String, selectedName: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.width(120.dp).height(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(selectedName, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentSelected: String,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("언어 선택") },
        text = {
            Column {
                SUPPORTED_LANGUAGES.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang.name, lang.code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (lang.name == currentSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                        Text(lang.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}