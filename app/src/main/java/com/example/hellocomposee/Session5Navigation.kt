package com.example.hellocomposee

import android.R.attr.navigationIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hellocomposee.ui.theme.HelloComposeeTheme

/*
 * ========== 6교시: Navigation - 화면 이동 ==========
 *
 * 목표: 여러 화면을 가진 앱의 뼈대 구성
 *
 * [NavHost & NavController]
 * - NavController: 화면 이동을 총괄하는 조종사 (navigate, popBackStack).
 * - NavHost: "지도(Graph)" — 어떤 경로(Route)에 어떤 Composable을 보여줄지 정의.
 *
 * [composable 정의]
 * - composable("경로") { ... }: 경로 이름으로 화면 등록.
 * - 예: "main" → 메인, "detail/{id}" → 상세 (id는 인자로 전달).
 *
 * [데이터 전달]
 * - 경로에 포함: "detail/$profileId" → navController.navigate("detail/123").
 * - 상세 화면에서: backStackEntry.arguments?.getString("profileId") 로 받기.
 *
 * [6교시 완성]
 * - 프로필 카드 클릭 → 상세 프로필 화면으로 이동.
 */

// 경로,Routes 정의
object Session5Routes {
    const val MAIN = "main"
    const val DETAIL = "detail/{profileId}"
    const val DETAIL_NAME_ARG = "profileId"

    fun detail(profileId: String) = "detail/$profileId"
}

// data class 정의
data class Session5Profile(
    val id: String,
    val name: String,
    val role: String
)

@Composable
fun Session5MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profiles = listOf(
        Session5Profile("1", "홍길동", "안드로이드 개발자"),
        Session5Profile("2", "김철수", "iOS 개발자"),
        Session5Profile("3", "이영희", "백엔드 개발자"),
    )

    Column(
        modifier = modifier.padding(16.dp)
    ){
        Text(
            "프로필 목록 - 카드 클릭 시 상페 페이지로 이동",
            style=MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom=16.dp),
        )
        profiles.forEach { profile ->
            Session5ProfileCard(
                profile = profile,
                onClick = {navController.navigate(Session5Routes.detail(profile.id))}
            )
        }
    }
}

@Composable
fun Session5ProfileCard(
    profile: Session5Profile,
    onClick: ()->Unit, //콜백 함수,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // compose는 UI에 있어서 위에서 아래로 순차적으로 적용됨!
            .padding(16.dp)  // 결과적으로 상하에 24.dp, 좌우에 16.dp 값 만큼 패딩된다.
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Box( //여러 요소를 겹치거나, 내부 요소를 정렬할 때 사용하기 좋다.
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ){
            Text(
                profile.name.first().toString(), //.first()는 문자열이나 리스트에서 가장 첫번째 글자(Char)를 꺼내온다.
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            text =profile.name,
            style= MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier.padding(top = 4.dp)){
            Text(profile.role, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text="more...",
            style= MaterialTheme.typography.labelMedium,
            color= MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Session5DetailScreen(
    profileId: String?,
    onBack: ()->Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("상세 프로필")},
                navigationIcon = {
                    IconButton(onClick = onBack){
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ){
            Text(
                text = "전달받은 프로필 ID: ${profileId ?: "(없음)"}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "실제 앱에서는 이 ID로 API 호출해 상세 정보를 불러옵니다.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top=16.dp)
            )
        }
    }
}

@Composable
fun Session5NavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Session5Routes.MAIN,
        modifier = modifier,
    ){
        composable(route = Session5Routes.MAIN){
            Session5MainScreen(navController)
        }
        composable(
            route = Session5Routes.DETAIL,
            arguments = listOf(navArgument(Session5Routes.DETAIL_NAME_ARG){type = NavType.StringType})
        ){ backStackEntry ->
            val profileId = backStackEntry.arguments?.getString(Session5Routes.DETAIL_NAME_ARG)
            Session5DetailScreen(
                profileId = profileId,
                onBack = {navController.popBackStack()}
            )
        }
    }
}

@Preview
@Composable
fun Session5MainScreenVeiw() {
    HelloComposeeTheme() {
        Session5NavHost()
    }
}