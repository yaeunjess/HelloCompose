package cursor.playground

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hellocomposee.R

/**
 * 1교시: UI 기본기 연습용 파일
 *
 * - Kotlin val / var 간단 예시
 * - Column / Row / Box 중첩
 * - Modifier 자주 쓰는 것 손 연습
 *
 * 이 파일은 앱에서 실제로 사용되지 않는 연습용 코드입니다.
 */

// Kotlin val / var 복기용 예시 함수
fun kotlinValVarExample() {
    // val: 한 번 정하면 재할당 불가 (불변 참조)
    val name: String = "Jess"
    // name = "Other" // 컴파일 에러

    // var: 값 변경 가능 (가변 참조)
    var age: Int = 20
    age = 21

    // ? : null 허용 타입
    var nickname: String? = null
    nickname = "제스"

    // 안전 호출 예시
    val nicknameLength: Int? = nickname?.length

    // 엘비스 연산자 예시 (null 이면 기본값)
    val safeLength: Int = nicknameLength ?: 0
}

@Composable
fun ProfileCard(
    name: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onClick() }, // Modifier 순서에 따라 동작/레이아웃이 달라지는 것도 관찰해보기
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box + Image 로 프로필 동그라미 영역 만들기
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBBDEFB)),
                contentAlignment = Alignment.Center
            ) {
                // 실제 앱에서는 painterResource 로 이미지 사용
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 영역: Column 사용
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileCard(
                name = "제스",
                description = "Jetpack Compose 연습 중입니다."
            )
        }
    }
}

