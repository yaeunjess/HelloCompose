package cursor.playground

/**
 * 8교시: AI 오케스트레이션 - Gemini API 연동 ⭐
 *
 * 이 파일은 Google Gemini API를 활용하여 사용자 입력에서
 * 일정 정보(날짜, 시간, 장소)를 추출하는 기능을 학습하기 위한 연습용 코드입니다.
 *
 * ⭐ 핵심 개념:
 * - Gemini API 연동
 * - 프롬프트 엔지니어링
 * - Entity 추출 (NER: Named Entity Recognition)
 * - JSON 파싱 및 데이터 변환
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ============================================================
// 1. 필요한 의존성 (build.gradle.kts 에 추가)
// ============================================================
/*
// Google Generative AI (Gemini)
implementation("com.google.ai.client.generativeai:generativeai:0.2.1")

// Kotlinx Serialization (JSON 파싱)
plugins {
    kotlin("plugin.serialization") version "1.9.21"
}
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
*/

// ============================================================
// 2. 데이터 모델 - 추출된 일정 정보
// ============================================================

/**
 * AI가 추출한 일정 정보
 *
 * @property title 일정 제목 (예: "미팅")
 * @property date 날짜 (예: "2024-01-15")
 * @property time 시간 (예: "15:00")
 * @property location 장소 (예: "강남역")
 * @property rawInput 원본 사용자 입력
 * @property confidence 추출 신뢰도 (0.0 ~ 1.0)
 */
@Serializable
data class ExtractedSchedule(
    val title: String? = null,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val rawInput: String = "",
    val confidence: Double = 0.0
)

/**
 * AI 응답 결과
 */
@Serializable
data class AiExtractionResult(
    val success: Boolean,
    val schedule: ExtractedSchedule? = null,
    val errorMessage: String? = null
)

// ============================================================
// 3. 프롬프트 엔지니어링 ⭐
// ============================================================

/**
 * 프롬프트 템플릿 관리
 *
 * 좋은 프롬프트의 특징:
 * 1. 명확한 역할 부여 (You are a...)
 * 2. 구체적인 출력 형식 지정 (JSON schema)
 * 3. 예시 제공 (Few-shot learning)
 * 4. 제약 조건 명시
 */
object PromptTemplates {

    /**
     * 일정 추출용 시스템 프롬프트
     *
     * ⭐ 프롬프트 엔지니어링 핵심:
     * - 역할 정의: AI가 무엇을 해야 하는지 명확히
     * - 출력 형식: JSON 스키마로 정확히 지정
     * - 예시: 입력과 출력의 예시를 제공
     * - 에지 케이스: 정보가 없을 때 어떻게 할지 명시
     */
    val SCHEDULE_EXTRACTION_PROMPT = """
        당신은 사용자의 자연어 입력에서 일정 정보를 추출하는 AI 어시스턴트입니다.

        ## 작업
        사용자가 입력한 텍스트에서 다음 정보를 추출하세요:
        - title: 일정의 제목 또는 주요 활동
        - date: 날짜 (YYYY-MM-DD 형식으로 변환)
        - time: 시간 (HH:mm 24시간 형식으로 변환)
        - location: 장소 또는 위치

        ## 규칙
        1. 오늘 날짜는 {TODAY_DATE}입니다.
        2. "내일"은 {TOMORROW_DATE}로 변환하세요.
        3. "모레"는 {DAY_AFTER_TOMORROW}로 변환하세요.
        4. "오후 3시"는 "15:00"으로 변환하세요.
        5. 정보가 명시되지 않은 필드는 null로 반환하세요.
        6. 반드시 아래 JSON 형식으로만 응답하세요.

        ## 출력 형식 (JSON)
        ```json
        {
            "title": "일정 제목",
            "date": "YYYY-MM-DD",
            "time": "HH:mm",
            "location": "장소",
            "confidence": 0.95
        }
        ```

        ## 예시

        입력: "내일 오후 3시 강남역에서 미팅"
        출력:
        ```json
        {
            "title": "미팅",
            "date": "{TOMORROW_DATE}",
            "time": "15:00",
            "location": "강남역",
            "confidence": 0.95
        }
        ```

        입력: "다음주 월요일 점심 약속"
        출력:
        ```json
        {
            "title": "점심 약속",
            "date": "2024-01-22",
            "time": null,
            "location": null,
            "confidence": 0.7
        }
        ```

        입력: "회의 참석"
        출력:
        ```json
        {
            "title": "회의 참석",
            "date": null,
            "time": null,
            "location": null,
            "confidence": 0.5
        }
        ```

        ## 사용자 입력
        "{USER_INPUT}"
    """.trimIndent()

    /**
     * 날짜 관련 변수를 실제 값으로 치환
     */
    fun buildScheduleExtractionPrompt(
        userInput: String,
        todayDate: String = "2024-01-15",
        tomorrowDate: String = "2024-01-16",
        dayAfterTomorrow: String = "2024-01-17"
    ): String {
        return SCHEDULE_EXTRACTION_PROMPT
            .replace("{TODAY_DATE}", todayDate)
            .replace("{TOMORROW_DATE}", tomorrowDate)
            .replace("{DAY_AFTER_TOMORROW}", dayAfterTomorrow)
            .replace("{USER_INPUT}", userInput)
    }

    /**
     * 날씨 기반 추천 프롬프트 (추가 활용 예시)
     */
    val WEATHER_RECOMMENDATION_PROMPT = """
        당신은 날씨에 따른 활동을 추천해주는 AI 어시스턴트입니다.
        
        현재 날씨: {WEATHER_CONDITION}
        온도: {TEMPERATURE}°C
        
        위 날씨 정보를 바탕으로 사용자에게 적합한 활동 3가지를 추천하세요.
        
        출력 형식 (JSON):
        ```json
        {
            "recommendations": [
                {"activity": "활동1", "reason": "이유"},
                {"activity": "활동2", "reason": "이유"},
                {"activity": "활동3", "reason": "이유"}
            ]
        }
        ```
    """.trimIndent()
}

// ============================================================
// 4. Gemini API 클라이언트
// ============================================================

/**
 * Gemini API 서비스
 *
 * 실제 구현 시 필요:
 * import com.google.ai.client.generativeai.GenerativeModel
 * import com.google.ai.client.generativeai.type.generationConfig
 */
class GeminiService(
    private val apiKey: String = "YOUR_GEMINI_API_KEY"
) {
    // 실제 구현:
    // private val model = GenerativeModel(
    //     modelName = "gemini-pro",
    //     apiKey = apiKey,
    //     generationConfig = generationConfig {
    //         temperature = 0.2f  // 낮을수록 일관된 응답
    //         topK = 1
    //         topP = 1f
    //         maxOutputTokens = 1024
    //     }
    // )

    /**
     * 텍스트 생성 요청
     */
    suspend fun generateContent(prompt: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 실제 Gemini API 호출:
                // val response = model.generateContent(prompt)
                // val text = response.text ?: throw Exception("Empty response")
                // Result.success(text)

                // 연습용 Mock 응답
                val mockResponse = generateMockResponse(prompt)
                Result.success(mockResponse)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 연습용 Mock 응답 생성
     */
    private fun generateMockResponse(prompt: String): String {
        // 프롬프트에서 사용자 입력 추출 (간단한 파싱)
        val userInputMatch = Regex("\"([^\"]+)\"\\s*$").find(prompt)
        val userInput = userInputMatch?.groupValues?.get(1) ?: ""

        // 간단한 키워드 기반 Mock 응답
        return when {
            "강남역" in userInput || "미팅" in userInput -> """
                ```json
                {
                    "title": "미팅",
                    "date": "2024-01-16",
                    "time": "15:00",
                    "location": "강남역",
                    "confidence": 0.95
                }
                ```
            """.trimIndent()

            "회의" in userInput -> """
                ```json
                {
                    "title": "회의",
                    "date": "2024-01-15",
                    "time": "10:00",
                    "location": null,
                    "confidence": 0.8
                }
                ```
            """.trimIndent()

            else -> """
                ```json
                {
                    "title": "${userInput.take(20)}",
                    "date": null,
                    "time": null,
                    "location": null,
                    "confidence": 0.5
                }
                ```
            """.trimIndent()
        }
    }
}

// ============================================================
// 5. AI 일정 추출 Repository
// ============================================================

/**
 * AI 기반 일정 추출 Repository
 */
class ScheduleExtractionRepository(
    private val geminiService: GeminiService = GeminiService()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 사용자 입력에서 일정 정보 추출
     *
     * @param userInput 자연어 입력 (예: "내일 오후 3시 강남역 미팅")
     * @return 추출된 일정 정보
     */
    suspend fun extractSchedule(userInput: String): Result<ExtractedSchedule> {
        // 1. 프롬프트 생성
        val prompt = PromptTemplates.buildScheduleExtractionPrompt(
            userInput = userInput,
            todayDate = getCurrentDate(),
            tomorrowDate = getTomorrowDate(),
            dayAfterTomorrow = getDayAfterTomorrow()
        )

        // 2. AI 호출
        return geminiService.generateContent(prompt)
            .mapCatching { response ->
                // 3. JSON 파싱
                parseJsonFromResponse(response, userInput)
            }
    }

    /**
     * AI 응답에서 JSON 추출 및 파싱
     */
    private fun parseJsonFromResponse(response: String, rawInput: String): ExtractedSchedule {
        // AI 응답에서 JSON 블록 추출
        val jsonPattern = Regex("```json\\s*([\\s\\S]*?)```")
        val jsonMatch = jsonPattern.find(response)
        val jsonString = jsonMatch?.groupValues?.get(1)?.trim()
            ?: throw Exception("JSON을 찾을 수 없습니다")

        // JSON 파싱
        val parsed = json.decodeFromString<ExtractedSchedule>(jsonString)
        return parsed.copy(rawInput = rawInput)
    }

    // 날짜 유틸리티 (실제로는 java.time 사용)
    private fun getCurrentDate(): String = "2024-01-15"
    private fun getTomorrowDate(): String = "2024-01-16"
    private fun getDayAfterTomorrow(): String = "2024-01-17"
}

// ============================================================
// 6. ViewModel - AI 기능 통합
// ============================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AI 일정 추출 화면의 상태
 */
data class ScheduleInputUiState(
    val userInput: String = "",
    val extractedSchedule: ExtractedSchedule? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val suggestions: List<String> = listOf(
        "내일 오후 3시 강남역 미팅",
        "모레 점심 홍대에서 친구 만남",
        "다음주 월요일 오전 10시 회의"
    )
)

/**
 * AI 일정 추출 ViewModel
 */
class ScheduleInputViewModel(
    private val extractionRepository: ScheduleExtractionRepository = ScheduleExtractionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleInputUiState())
    val uiState: StateFlow<ScheduleInputUiState> = _uiState.asStateFlow()

    /**
     * 사용자 입력 업데이트
     */
    fun updateInput(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input)
    }

    /**
     * AI로 일정 추출
     */
    fun extractSchedule() {
        val input = _uiState.value.userInput
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "텍스트를 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                error = null
            )

            extractionRepository.extractSchedule(input)
                .onSuccess { schedule ->
                    _uiState.value = _uiState.value.copy(
                        extractedSchedule = schedule,
                        isProcessing = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "일정 추출에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    /**
     * 제안 문구 선택
     */
    fun selectSuggestion(suggestion: String) {
        _uiState.value = _uiState.value.copy(userInput = suggestion)
        extractSchedule()
    }

    /**
     * 입력 초기화
     */
    fun clearInput() {
        _uiState.value = ScheduleInputUiState()
    }
}

// ============================================================
// 7. 실제 앱에서의 활용 - Todo 앱과 통합
// ============================================================

/**
 * AI 기능이 통합된 스마트 Todo 아이템
 */
data class SmartTodoItem(
    val id: String,
    val rawInput: String,           // 원본 입력
    val parsedTitle: String,        // AI가 파싱한 제목
    val dueDate: String?,           // AI가 추출한 날짜
    val dueTime: String?,           // AI가 추출한 시간
    val location: String?,          // AI가 추출한 장소
    val isCompleted: Boolean = false,
    val weatherAtLocation: String? = null  // 장소의 날씨 (Session 7 연동)
)

/**
 * 스마트 Todo Repository
 * Session 6 (Firebase) + Session 7 (API) + Session 8 (AI) 통합
 */
class SmartTodoRepository(
    private val scheduleExtractor: ScheduleExtractionRepository = ScheduleExtractionRepository(),
    private val weatherRepository: WeatherRepository = WeatherRepository()
    // private val firebaseTodoRepository: TodoRepository = TodoRepository()
) {
    /**
     * 자연어 입력으로 스마트 Todo 생성
     *
     * 흐름:
     * 1. AI로 입력 파싱 (날짜, 시간, 장소 추출)
     * 2. 장소가 있으면 해당 위치 날씨 조회
     * 3. Firebase에 저장
     */
    suspend fun createSmartTodo(userInput: String): Result<SmartTodoItem> {
        return try {
            // 1. AI로 일정 정보 추출
            val extraction = scheduleExtractor.extractSchedule(userInput)
                .getOrThrow()

            // 2. 장소가 있으면 날씨 조회
            var weatherInfo: String? = null
            extraction.location?.let { location ->
                weatherRepository.getWeatherForCity(location)
                    .onSuccess { weather ->
                        weatherInfo = "${weather.weatherEmoji()} ${weather.temperatureDisplay()}"
                    }
            }

            // 3. SmartTodoItem 생성
            val smartTodo = SmartTodoItem(
                id = "todo_${System.currentTimeMillis()}",
                rawInput = userInput,
                parsedTitle = extraction.title ?: userInput,
                dueDate = extraction.date,
                dueTime = extraction.time,
                location = extraction.location,
                weatherAtLocation = weatherInfo
            )

            // 4. Firebase 저장 (실제 구현 시)
            // firebaseTodoRepository.addTodo(smartTodo)

            Result.success(smartTodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ============================================================
// 8. Compose UI (참고용)
// ============================================================

/*
@Composable
fun SmartScheduleInputScreen(
    viewModel: ScheduleInputViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 헤더
        Text(
            text = "AI 일정 입력",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "자연어로 일정을 입력하면 AI가 자동으로 파싱합니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 입력 필드
        OutlinedTextField(
            value = uiState.userInput,
            onValueChange = { viewModel.updateInput(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("일정 입력") },
            placeholder = { Text("예: 내일 오후 3시 강남역 미팅") },
            trailingIcon = {
                if (uiState.userInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearInput() }) {
                        Icon(Icons.Default.Clear, "지우기")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 추출 버튼
        Button(
            onClick = { viewModel.extractSchedule() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isProcessing
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("AI로 파싱하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 제안 문구
        Text("예시 문구:", style = MaterialTheme.typography.labelMedium)
        uiState.suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { viewModel.selectSuggestion(suggestion) },
                label = { Text(suggestion) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 추출 결과
        uiState.extractedSchedule?.let { schedule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("추출된 정보", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    ExtractedInfoRow("📝 제목", schedule.title ?: "없음")
                    ExtractedInfoRow("📅 날짜", schedule.date ?: "없음")
                    ExtractedInfoRow("⏰ 시간", schedule.time ?: "없음")
                    ExtractedInfoRow("📍 장소", schedule.location ?: "없음")
                    ExtractedInfoRow("🎯 신뢰도", "${(schedule.confidence * 100).toInt()}%")
                }
            }
        }

        // 에러 메시지
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ExtractedInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, modifier = Modifier.width(80.dp))
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
*/

// ============================================================
// 📚 학습 포인트 정리
// ============================================================
/*
 1. 프롬프트 엔지니어링 핵심
    - 명확한 역할 부여: "당신은 ~하는 AI입니다"
    - 구체적인 출력 형식: JSON 스키마 명시
    - Few-shot 예시: 입력-출력 쌍 제공
    - 에지 케이스 처리: 정보 없을 때 행동 명시

 2. Gemini API 사용법
    - GenerativeModel 인스턴스 생성
    - generationConfig 로 온도, 토큰 수 등 설정
    - generateContent() 로 텍스트 생성
    - 응답에서 JSON 파싱

 3. Entity 추출 (NER)
    - 날짜: "내일", "다음주 월요일" → "2024-01-16"
    - 시간: "오후 3시" → "15:00"
    - 장소: "강남역", "홍대" → 그대로 추출

 4. 통합 아키텍처
    UI → ViewModel → Repository → AI Service
                         ↓
                    JSON Parser
                         ↓
                   Data Classes

 5. 확장 가능성
    - 날씨 기반 알림: "비 올 때 우산 챙기기" 알림
    - 위치 기반 알림: "강남역 도착 시" 트리거
    - 스마트 추천: 날씨 + 일정 기반 활동 추천

 6. API 키 보안
    - BuildConfig 또는 local.properties 사용
    - 절대 코드에 하드코딩하지 않기
    - 서버 사이드 프록시 고려 (프로덕션)
 */

// ============================================================
// 9. 보너스: 프롬프트 테스트 유틸리티
// ============================================================

/**
 * 프롬프트 테스트용 함수
 * 다양한 입력에 대해 프롬프트가 어떻게 생성되는지 확인
 */
fun testPromptGeneration() {
    val testInputs = listOf(
        "내일 오후 3시 강남역 미팅",
        "모레 점심에 친구 만나기",
        "다음주 월요일 오전 10시 회의",
        "저녁에 영화 보기",
        "회의 참석"
    )

    println("=== 프롬프트 생성 테스트 ===")
    testInputs.forEach { input ->
        println("\n입력: $input")
        println("-".repeat(50))
        val prompt = PromptTemplates.buildScheduleExtractionPrompt(input)
        // 실제로는 이 prompt 를 Gemini API 에 전달
        println("프롬프트 길이: ${prompt.length}자")
    }
}
