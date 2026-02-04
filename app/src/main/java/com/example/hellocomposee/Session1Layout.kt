package com.example.hellocomposee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hellocomposee.ui.theme.HelloComposeeTheme

/*
 * ========== 1교시: UI 기본기 잡기 ==========
 *
 * [Kotlin 복습]
 * - val: 한 번 할당하면 변경 불가 (읽기 전용)
 * - var: 값 변경 가능
 * - ? (Null Safety): nullable 타입. null일 수 있으면 "타입?" 사용.
 *   예: String? → null 또는 문자열
 *
 * [Compose Layout]
 * - Column: 세로로 쌓기 (위 → 아래)
 * - Row: 가로로 나열 (왼쪽 → 오른쪽)
 * - Box: 겹쳐서 배치 (z축). alignment로 자식 위치 지정
 *
 * [Modifier]
 * - .padding(16.dp): 안쪽 여백
 * - .fillMaxWidth(): 가로 전체 차지
 * - .fillMaxSize(): 가로·세로 전체 (Box 등에서)
 * - .clickable { }: 클릭 가능하게 (2교시에서 더 사용)
 * - .size(48.dp): 고정 크기
 * - .clip(CircleShape): 원형으로 자르기
 */

@Preview
@Composable
fun Session1ProfileCard() {
    HelloComposeeTheme() {
        Column(
            modifier = Modifier.padding(16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text("J", color= MaterialTheme.colorScheme.onPrimary)
            }

            Text(
                text="김예은",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text("안드로이드 개발자", style= MaterialTheme.typography.bodySmall)
                Text("대학생 5학년", style= MaterialTheme.typography.bodySmall)
            }

            Text(
                text="예은입니다.",
                modifier = Modifier.padding(top = 16.dp)
                    .clickable{ },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

}