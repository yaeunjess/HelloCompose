package com.example.hellocomposee

import android.R
import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
 * ========== 5교시: 비동기 처리와 Coroutines ==========
 *
 * 목표: 앱이 멈추지(Freeze) 않게 무거운 작업을 뒷단에서 처리
 *
 * [viewModelScope.launch]
 * - ViewModel 안에서 비동기 작업을 안전하게 시작.
 * - 화면이 사라지면 scope가 취소되어 메모리 누수 방지.
 *
 * [delay()]
 * - 가짜 로딩: 2초 대기 동안 UI는 그대로 반응 (스피너 표시).
 * - "데이터를 가져오는 동안에도 사용자는 화면을 만질 수 있어야 한다"
 *
 * [네트워크 통신 맛보기]
 * - 서버에서 데이터를 가져온다고 가정 → 비동기로 받아와 리스트 업데이트.
 * - 여기서는 실제 API 대신 delay() 후 가짜 리스트 반환.
 */

// 가짜 서버에서 받아온 데이터 모델
data class Session4Item(
    val id: Int,
    val title: String,
    val subtitle: String,
)

// 로딩 상태를 구분 (UI가 그에 맞게 스피너 vs 리스트 표시)
sealed class Session4UiState {
    data object Idle : Session4UiState()
    data object Loading : Session4UiState()
    data class Success(val items: List<Session4Item>) : Session4UiState()
    data class Error(val message: String) : Session4UiState()
}

class Session4ViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<Session4UiState>(Session4UiState.Idle)
    val uiState: StateFlow<Session4UiState> = _uiState.asStateFlow()

    fun loadData(){
        viewModelScope.launch{
            _uiState.value = Session4UiState.Loading

            delay(2000)

            _uiState.value = Session4UiState.Success(
                items = listOf(
                    Session4Item(1, "첫 번째 항목", "서버에서 가져온 데이터"),
                    Session4Item(2, "두 번째 항목", "비동기로 로드됨"),
                    Session4Item(3, "세 번째 항목", "로딩 중에도 화면 터치 가능"),
                )
            )
        }
    }

}

@Composable
fun Session4Screen(
    viewModel : Session4ViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "비동기 + 가짜 네트워크",
            style = MaterialTheme.typography.titleMedium,
        )
        Button(onClick = { viewModel.loadData() }) {
            Text("데이터 불러오기 (2초 로딩)")
        }

        when (val state = uiState){
            is Session4UiState.Idle -> {
                Text("버튼을 누르면 로딩 스피너 -> 리스트가 나타납니다.")
            }
            is Session4UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        CircularProgressIndicator()
                        Text("로딩 중...(화면 터치 가능)", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            is Session4UiState.Success -> {
                Text("로딩 완료! 리스트:")
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items) { item ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(8.dp)
                        ){
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.subtitle, style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            is Session4UiState.Error -> {
                Text("오류: ${state.message}")
            }
        }
    }
}

@Preview
@Composable
fun Session4ScreenPreview() {
    HelloComposeeTheme() {
        Session4Screen()
    }
}