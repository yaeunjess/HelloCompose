// plugin: 안드로이드 앱이고 코틀린을 쓴다는 기본 엔진을 장착하는 곳이다.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// android: 앱의 주민등록번호나 어떤 버전의 안드로이드 폰까지 지원할지 같은 앱의 신상 정보를 적는다.
android {
    namespace = "com.example.hellocomposee"
    compileSdk {
        version = release(36)
    }

    // defaultConfig: 기본 설정 (신분증 정보)
    defaultConfig {
        applicationId = "com.example.hellocomposee" // 구글 플레이 스토어에서 앱의 고유 아이디
        minSdk = 24
        targetSdk = 36
        // 앱 업데이트를 위한 번호들
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // buildTypes: 빌드 방식 (배포용 vs 개발용)
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // compileOptions: 자바 언어의 문법 버전을 설정
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true // 최신 UI 기술인 Jetpack Compose를 사용한다는 선언!
    }
}

// dependencies(의존성): 앱을 만드는 데 필요한 라이브러리 목록을 적는다.
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.foundation.layout) // # ViewModel 확장 라이브러리를 위해 추가
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}