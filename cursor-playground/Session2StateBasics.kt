package cursor.playground

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 2교시: 상태 관리와 화면 업데이트 연습용 파일
 *
 * - remember + mutableStateOf 로 카운터 만들기
 * - State Hoisting (상태 끌어올리기) 연습
 * - TextField 입력 → 실시간 UI 반영
 */

// 1) remember + mutableStateOf: 가장 기본적인 카운터
@Composable
fun SimpleCounter() {
    // remember 를 안 쓰면, recomposition 마다 0으로 다시 초기화되어서 숫자가 유지되지 않음
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "카운트: $count",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { count++ }) {
            Text(text = "+1 올리기")
        }
    }
}

// 2) State Hoisting 예제

// 상태를 외부에서 주입받는 "UI 전용" 컴포저블
@Composable
fun CounterStateless(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "카운트: $count",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onIncrement) {
            Text(text = "+1 (호이스팅)")
        }
    }
}

// 상태를 이 컴포저블에서 관리하고, 실제 UI 는 위의 CounterStateless 에 위임
@Composable
fun CounterStateful() {
    var count by remember { mutableStateOf(0) }

    CounterStateless(
        count = count,
        onIncrement = { count++ }
    )
}

// 3) TextField + 상태: 입력값 실시간 반영
@Composable
fun TextFieldEcho() {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newText -> text = newText },
            label = { Text("아무 내용이나 입력") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "입력한 내용: $text")
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleCounterPreview() {
    Surface {
        SimpleCounter()
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterStatefulPreview() {
    Surface {
        CounterStateful()
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldEchoPreview() {
    Surface {
        TextFieldEcho()
    }
}

