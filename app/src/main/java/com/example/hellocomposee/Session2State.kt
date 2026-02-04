package com.example.hellocomposee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hellocomposee.ui.theme.HelloComposeeTheme

/*
 * ========== 2교시: 상태 관리와 화면 업데이트 ==========
 *
 * 핵심: "상태가 변해야 UI가 바뀐다"
 *
 * [remember]
 * - Composable이 다시 그려져도 값을 유지함.
 * - remember 안 쓰면 리컴포지션마다 값이 초기화됨 → 화면이 안 바뀌는 것처럼 보일 수 있음.
 *
 * [mutableStateOf]
 * - 값이 바뀌면 해당 State를 읽는 Composable만 다시 그려짐 (리컴포지션).
 * - var count by remember { mutableStateOf(0) } → count 변경 시 UI 자동 갱신.
 *
 * [State Hoisting (상태 끌어올리기)]
 * - 상태를 Composable 내부가 아니라 "호출하는 쪽(상위)"으로 올리는 것.
 * - 이유: 재사용성, 단일 소스 of truth, 테스트 용이.
 * - 패턴: 상태는 상위에서 관리, 하위에는 value + onValueChange 같은 콜백 전달.
 *
 * [TextField]
 * - value = 상태, onValueChange = { 새값 -> 상태 = 새값 } 으로 실시간 반영.
 */

// 예제1: remember + mutableStateOf 카운터
@Preview
@Composable
fun Session2Counter() {
    var count by remember {
        mutableStateOf(0)
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text("클릭 횟수: $count")
        Button(onClick = { count++ }) {
            Text("증가")
        }
    }
}

// 예제2: State Hoisting, 상태를 상위로 올린 카운터
@Composable
fun Session2CounterHoisted(
    count: Int,
    onCountChange:(Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Hoisted 카운트: $count")
        Button(onClick = {onCountChange(count+1)}){
            Text("증가")
        }
    }
}

@Preview
@Composable
fun Session2CounterHoistedPreview(modifier: Modifier = Modifier) {
    HelloComposeeTheme {
        var count by remember { mutableStateOf(0) }
        Session2CounterHoisted(count = count, onCountChange = { count = it })
    }
}

// 예제3: TextField, 입력이 실시간으로 화면에 반영
@Preview
@Composable
fun SessionTextField() {
    var text by remember {mutableStateOf("")}
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("이름 입력(개행도 들어가지롱)")},
            modifier = Modifier.fillMaxWidth()
        )
        Text("실시간 반영: $text")
    }
}