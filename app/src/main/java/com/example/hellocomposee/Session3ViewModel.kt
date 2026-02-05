package com.example.hellocomposee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hellocomposee.ui.theme.HelloComposeeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * ========== 3교시: 데이터의 흐름과 비동기 ==========
 *
 * 목표: 화면 회전에도 데이터 유지 + 멈추지 않는 앱
 *
 * [ViewModel]
 * - 화면(Activity/Fragment)이 바뀌어도(회전, 재생성) 데이터 유지.
 * - 카운터를 ViewModel에 두면 회전해도 숫자가 유지됨.
 *
 * [Coroutines]
 * - 비동기: "무거운 작업·네트워크는 메인 스레드 말고 따로 처리" → 앱이 멈추지 않음.
 * - viewModelScope.launch: ViewModel 안에서 코루틴 실행. 화면 사라지면 자동 취소.
 * - delay(2000): 2초 대기 (가짜 로딩)
 *
 * [StateFlow]
 * - ViewModel에서 UI로 상태를 전달할 때 사용. Compose와 잘 맞음.
 * - MutableStateFlow(초기값) → asStateFlow() 로 읽기 전용 노출.
 */

class Session3ViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _loadingText = MutableStateFlow("버튼을 눌러 2초 로딩 시작")
    val loadingText: StateFlow<String> = _loadingText.asStateFlow()

    fun incrementCount(){
        _count.value ++;
    }

    fun startFakeLoading(){
        viewModelScope.launch{ // 플젝에서 보통 "네트워크 통신(서버 데이터 가져오기)"와 "로컬 DB 접근"에 쓰인다.
            _loadingText.value = "로딩 중..."
            delay(2000)
            _loadingText.value = "로딩 완료!!!"
        }
    }
}

@Composable
fun Session3Screen(
    viewModel: Session3ViewModel = viewModel(),
) {
    val count by viewModel.count.collectAsState()
    val loadingText by viewModel.loadingText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Text("ViewModel 카운터(회전해도 유지): $count")
        Button(onClick = { viewModel.incrementCount() }) {
            Text("증가")
        }

        Text(loadingText)
        Button(onClick = { viewModel.startFakeLoading() }) {
            Text("2초 가짜 로딩 시작")
        }
    }
}

@Preview
@Composable
fun Session3ScreenPreview() {
    HelloComposeeTheme() {
        Session3Screen()
    }
}