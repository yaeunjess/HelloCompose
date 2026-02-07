package cursor.playground

/**
 * 7교시: 외부 라이브러리 & API 연동
 *
 * 이 파일은 Retrofit/Ktor 를 이용한 REST API 통신과
 * Google Maps SDK 연동의 개념을 학습하기 위한 연습용 코드입니다.
 *
 * ⭐ 핵심 개념:
 * - Retrofit: 안드로이드에서 가장 많이 쓰이는 HTTP 클라이언트
 * - Ktor: Kotlin 친화적인 비동기 HTTP 클라이언트
 * - Google Maps SDK: 위치 기반 기능 구현
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ============================================================
// 1. 필요한 의존성 (build.gradle.kts 에 추가)
// ============================================================
/*
// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// 또는 Ktor (Kotlin 친화적)
implementation("io.ktor:ktor-client-android:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

// Google Maps
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.0.1")

// Kotlinx Serialization (JSON 파싱용)
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
*/

// ============================================================
// 2. 데이터 모델 - 날씨 API 응답
// ============================================================

/**
 * 날씨 정보 데이터 클래스
 * OpenWeatherMap API 응답 구조에 맞춤
 */
data class WeatherResponse(
    val name: String,           // 도시 이름
    val main: MainWeather,
    val weather: List<WeatherDescription>,
    val coord: Coordinates
)

data class MainWeather(
    val temp: Double,           // 온도 (켈빈 → 섭씨 변환 필요)
    val humidity: Int,          // 습도 (%)
    val pressure: Int           // 기압 (hPa)
)

data class WeatherDescription(
    val id: Int,
    val main: String,           // "Clear", "Clouds", "Rain" 등
    val description: String,    // 상세 설명
    val icon: String            // 아이콘 코드
)

data class Coordinates(
    val lat: Double,            // 위도
    val lon: Double             // 경도
)

// ============================================================
// 3. Retrofit 방식 - 인터페이스 기반 API 정의
// ============================================================

/**
 * Retrofit 인터페이스
 *
 * 실제 사용 시 아래 import 필요:
 * import retrofit2.http.GET
 * import retrofit2.http.Query
 * import retrofit2.Response
 */
interface WeatherApiService {
    /**
     * 도시 이름으로 날씨 조회
     *
     * 실제 API 호출 예시:
     * https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=YOUR_API_KEY&units=metric
     */
    // @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        // @Query("q") city: String,
        // @Query("appid") apiKey: String,
        // @Query("units") units: String = "metric"  // 섭씨 온도
        city: String,
        apiKey: String,
        units: String = "metric"
    ): WeatherResponse  // Response<WeatherResponse>

    /**
     * 좌표로 날씨 조회 (위치 기반 알림에 활용)
     */
    // @GET("data/2.5/weather")
    suspend fun getWeatherByLocation(
        // @Query("lat") lat: Double,
        // @Query("lon") lon: Double,
        // @Query("appid") apiKey: String,
        // @Query("units") units: String = "metric"
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String = "metric"
    ): WeatherResponse
}

/**
 * Retrofit 클라이언트 설정
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    // 실제 구현:
    // val weatherApi: WeatherApiService by lazy {
    //     Retrofit.Builder()
    //         .baseUrl(BASE_URL)
    //         .addConverterFactory(GsonConverterFactory.create())
    //         .build()
    //         .create(WeatherApiService::class.java)
    // }

    // 연습용 Mock
    val weatherApi: WeatherApiService = MockWeatherApiService()
}

/**
 * 연습용 Mock API 서비스
 */
class MockWeatherApiService : WeatherApiService {
    override suspend fun getWeatherByCity(
        city: String,
        apiKey: String,
        units: String
    ): WeatherResponse {
        // 실제 API 대신 Mock 데이터 반환
        return WeatherResponse(
            name = city,
            main = MainWeather(temp = 15.5, humidity = 65, pressure = 1013),
            weather = listOf(
                WeatherDescription(
                    id = 800,
                    main = "Clear",
                    description = "맑음",
                    icon = "01d"
                )
            ),
            coord = Coordinates(lat = 37.5665, lon = 126.9780)
        )
    }

    override suspend fun getWeatherByLocation(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String
    ): WeatherResponse {
        return WeatherResponse(
            name = "현재 위치",
            main = MainWeather(temp = 18.0, humidity = 55, pressure = 1015),
            weather = listOf(
                WeatherDescription(
                    id = 801,
                    main = "Clouds",
                    description = "구름 조금",
                    icon = "02d"
                )
            ),
            coord = Coordinates(lat = lat, lon = lon)
        )
    }
}

// ============================================================
// 4. Ktor 방식 - Kotlin 친화적 HTTP 클라이언트
// ============================================================

/**
 * Ktor 클라이언트 설정
 *
 * Retrofit 과의 차이점:
 * - 인터페이스 없이 직접 함수로 호출
 * - Kotlin Coroutines 와 자연스럽게 통합
 * - Kotlinx Serialization 사용
 */
object KtorClient {
    // 실제 구현:
    // private val client = HttpClient(Android) {
    //     install(ContentNegotiation) {
    //         json(Json {
    //             ignoreUnknownKeys = true
    //             isLenient = true
    //         })
    //     }
    //     install(Logging) {
    //         level = LogLevel.BODY
    //     }
    // }

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5"
    private const val API_KEY = "YOUR_API_KEY" // 실제 API 키로 교체

    /**
     * Ktor 로 날씨 조회
     */
    suspend fun getWeather(city: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 실제 Ktor 코드:
                // val response: WeatherResponse = client.get("$BASE_URL/weather") {
                //     parameter("q", city)
                //     parameter("appid", API_KEY)
                //     parameter("units", "metric")
                // }.body()
                // Result.success(response)

                // 연습용 Mock
                val mockResponse = WeatherResponse(
                    name = city,
                    main = MainWeather(temp = 20.0, humidity = 60, pressure = 1010),
                    weather = listOf(
                        WeatherDescription(
                            id = 500,
                            main = "Rain",
                            description = "가벼운 비",
                            icon = "10d"
                        )
                    ),
                    coord = Coordinates(lat = 37.5665, lon = 126.9780)
                )
                Result.success(mockResponse)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// ============================================================
// 5. Repository 패턴 - API 추상화
// ============================================================

/**
 * 날씨 Repository
 * API 호출 로직을 캡슐화하여 ViewModel 에서 쉽게 사용
 */
class WeatherRepository {
    private val api = RetrofitClient.weatherApi
    private val apiKey = "YOUR_API_KEY" // 실제 API 키로 교체

    /**
     * 도시별 날씨 조회
     */
    suspend fun getWeatherForCity(city: String): Result<WeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getWeatherByCity(city, apiKey)
                val weatherInfo = response.toWeatherInfo()
                Result.success(weatherInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 현재 위치 날씨 조회
     */
    suspend fun getWeatherForLocation(lat: Double, lon: Double): Result<WeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getWeatherByLocation(lat, lon, apiKey)
                val weatherInfo = response.toWeatherInfo()
                Result.success(weatherInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * API 응답을 앱 내부 모델로 변환
     */
    private fun WeatherResponse.toWeatherInfo(): WeatherInfo {
        val weather = this.weather.firstOrNull()
        return WeatherInfo(
            cityName = this.name,
            temperature = this.main.temp,
            humidity = this.main.humidity,
            condition = weather?.main ?: "Unknown",
            description = weather?.description ?: "",
            iconCode = weather?.icon ?: "",
            latitude = this.coord.lat,
            longitude = this.coord.lon
        )
    }
}

/**
 * 앱 내부에서 사용할 날씨 정보 모델
 * API 응답 구조에 종속되지 않도록 분리
 */
data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val humidity: Int,
    val condition: String,      // Clear, Clouds, Rain 등
    val description: String,    // 상세 설명
    val iconCode: String,
    val latitude: Double,
    val longitude: Double
) {
    /**
     * 온도를 보기 좋은 문자열로 변환
     */
    fun temperatureDisplay(): String = "${temperature.toInt()}°C"

    /**
     * 날씨 상태에 따른 이모지 반환
     */
    fun weatherEmoji(): String = when (condition) {
        "Clear" -> "☀️"
        "Clouds" -> "☁️"
        "Rain" -> "🌧️"
        "Snow" -> "❄️"
        "Thunderstorm" -> "⛈️"
        else -> "🌤️"
    }
}

// ============================================================
// 6. Google Maps & Location - 위치 기반 기능
// ============================================================

/**
 * 위치 정보 데이터 클래스
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

/**
 * 위치 서비스 관리자
 *
 * 실제 구현 시 필요한 것들:
 * - AndroidManifest.xml 에 위치 권한 추가
 * - FusedLocationProviderClient 사용
 * - 런타임 권한 요청 처리
 */
class LocationManager {
    // private val fusedLocationClient: FusedLocationProviderClient

    /**
     * 현재 위치 가져오기
     *
     * 실제 구현:
     * @RequiresPermission(ACCESS_FINE_LOCATION)
     * suspend fun getCurrentLocation(): LocationData
     */
    suspend fun getCurrentLocation(): Result<LocationData> {
        return try {
            // 실제 구현:
            // val location = fusedLocationClient.lastLocation.await()
            // location?.let {
            //     Result.success(LocationData(it.latitude, it.longitude))
            // } ?: Result.failure(Exception("위치를 가져올 수 없습니다"))

            // 연습용 Mock (서울 시청 좌표)
            Result.success(LocationData(
                latitude = 37.5665,
                longitude = 126.9780,
                address = "서울특별시 중구 세종대로 110"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 특정 위치에 도착했을 때 알림 설정 (Geofencing)
     *
     * 이 기능으로 "강남역 근처 도착 시 알림" 같은 기능 구현 가능
     */
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float = 100f,
        onEnter: () -> Unit
    ) {
        // 실제 구현:
        // val geofence = Geofence.Builder()
        //     .setRequestId(id)
        //     .setCircularRegion(latitude, longitude, radiusMeters)
        //     .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        //     .setExpirationDuration(Geofence.NEVER_EXPIRE)
        //     .build()
        //
        // geofencingClient.addGeofences(geofencingRequest, pendingIntent)

        println("📍 Geofence 추가됨: $id at ($latitude, $longitude)")
    }
}

// ============================================================
// 7. ViewModel - 날씨 & 위치 통합
// ============================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 날씨 화면의 상태
 */
data class WeatherUiState(
    val weatherInfo: WeatherInfo? = null,
    val currentLocation: LocationData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 날씨 화면의 ViewModel
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository = WeatherRepository(),
    private val locationManager: LocationManager = LocationManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // 앱 시작 시 현재 위치의 날씨 로드
        loadCurrentLocationWeather()
    }

    /**
     * 현재 위치의 날씨 로드
     */
    fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. 현재 위치 가져오기
            locationManager.getCurrentLocation()
                .onSuccess { location ->
                    _uiState.value = _uiState.value.copy(currentLocation = location)

                    // 2. 해당 위치의 날씨 조회
                    weatherRepository.getWeatherForLocation(location.latitude, location.longitude)
                        .onSuccess { weather ->
                            _uiState.value = _uiState.value.copy(
                                weatherInfo = weather,
                                isLoading = false,
                                error = null
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "날씨 정보를 가져올 수 없습니다: ${e.message}"
                            )
                        }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "위치를 가져올 수 없습니다: ${e.message}"
                    )
                }
        }
    }

    /**
     * 도시 이름으로 날씨 검색
     */
    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            weatherRepository.getWeatherForCity(city)
                .onSuccess { weather ->
                    _uiState.value = _uiState.value.copy(
                        weatherInfo = weather,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "날씨 정보를 가져올 수 없습니다: ${e.message}"
                    )
                }
        }
    }
}

// ============================================================
// 8. Compose UI (참고용)
// ============================================================

/*
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 도시 검색
        var searchCity by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchCity,
            onValueChange = { searchCity = it },
            label = { Text("도시 검색") },
            trailingIcon = {
                IconButton(onClick = { viewModel.searchWeatherByCity(searchCity) }) {
                    Icon(Icons.Default.Search, "검색")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 로딩 상태
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        // 에러 메시지
        uiState.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        // 날씨 정보 표시
        uiState.weatherInfo?.let { weather ->
            WeatherCard(weather = weather)
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.weatherEmoji(),
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = weather.temperatureDisplay(),
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = weather.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "습도: ${weather.humidity}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
*/

// ============================================================
// 📚 학습 포인트 정리
// ============================================================
/*
 1. Retrofit vs Ktor
    - Retrofit: 인터페이스 기반, 어노테이션으로 API 정의, Java 친화적
    - Ktor: 함수 기반, Kotlin DSL 사용, Coroutines 네이티브 지원

 2. API 호출 구조
    API Service (Retrofit/Ktor)
         ↓
    Repository (비즈니스 로직, 에러 처리)
         ↓
    ViewModel (UI 상태 관리)
         ↓
    Compose UI (화면 표시)

 3. 위치 기반 기능
    - FusedLocationProviderClient: 현재 위치 조회
    - Geofencing: 특정 위치 진입/이탈 감지
    - Google Maps SDK: 지도 표시 및 마커

 4. 필요한 권한 (AndroidManifest.xml):
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

 5. API 키 관리 Best Practice
    - local.properties 에 API 키 저장 (git ignore)
    - BuildConfig 를 통해 접근
    - 절대 코드에 직접 하드코딩하지 않기!
 */
