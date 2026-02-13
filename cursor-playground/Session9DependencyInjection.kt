package cursor.playground

/**
 * 9교시: Hilt를 이용한 의존성 주입 (Dependency Injection)
 *
 * 목표: 객체를 내가 직접 만들지 않고 외부에서 주입받는 '자동 배달 시스템' 구축하기
 *
 * ⭐ 핵심 개념:
 * - DI(의존성 주입): "필요한 부품을 직접 만들지 않고 외부에서 배달받는 것"
 * - Hilt: Android 에서 DI 를 쉽게 해주는 구글 공식 라이브러리
 * - 왜 DI 가 필요한가? → 테스트, 유지보수, 부품 교체가 쉬워진다
 *
 * 이 파일은 앱에서 실제로 사용되지 않는 연습용 코드입니다.
 */

// ============================================================
// 1. 필요한 의존성 (build.gradle.kts 에 추가)
// ============================================================
/*
// --- 프로젝트 수준 build.gradle.kts ---
plugins {
    id("com.google.dagger.hilt.android") version "2.50" apply false
}

// --- 앱 수준 build.gradle.kts ---
plugins {
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")   // Hilt 가 어노테이션을 읽기 위해 필요
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")

    // Hilt + ViewModel 통합
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
*/

// ============================================================
// 0. DI 가 왜 필요한지 — Before & After 비교
// ============================================================

/**
 * ❌ DI 없이 직접 만드는 방식 (Bad)
 *
 * 문제점:
 * 1. ViewModel 이 Repository 를 "직접 생성" → 강한 결합(Tight Coupling)
 * 2. 테스트할 때 가짜(Mock) Repository 로 교체할 수 없다
 * 3. Repository 생성 방법이 바뀌면 모든 ViewModel 을 고쳐야 한다
 */
class BadViewModel_NoDI {
    // ❌ 직접 만들기 → "내가 직접 장보고, 직접 요리하는 것"
    private val repository = NoteRepositoryImpl()

    fun loadNotes() {
        val notes = repository.getAllNotes()
        println("노트 목록: $notes")
    }
}

/**
 * ✅ DI 로 주입받는 방식 (Good)
 *
 * 장점:
 * 1. ViewModel 은 Repository "인터페이스"만 알면 된다 → 느슨한 결합(Loose Coupling)
 * 2. 테스트할 때 FakeRepository 를 쉽게 끼워 넣을 수 있다
 * 3. Repository 구현이 바뀌어도 ViewModel 은 안 고쳐도 된다
 */
class GoodViewModel_WithDI(
    // ✅ 외부에서 배달받기 → "배달앱으로 재료를 주문하는 것"
    private val repository: NoteRepository  // 인터페이스에 의존!
) {
    fun loadNotes() {
        val notes = repository.getAllNotes()
        println("노트 목록: $notes")
    }
}

// ============================================================
// 2. 데이터 모델
// ============================================================

/**
 * 노트 데이터 클래스
 */
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ============================================================
// 3. Repository 패턴 — 인터페이스 + 구현 분리
// ============================================================

/**
 * ⭐ Repository 인터페이스
 *
 * "메뉴판" 역할 — 어떤 기능이 있는지만 정의
 * 실제로 어떻게 만드는지(구현)는 여기에 없음
 *
 * 이렇게 분리하는 이유:
 * - ViewModel 은 이 인터페이스만 바라본다
 * - 실제 구현을 자유롭게 교체할 수 있다 (DB ↔ Mock ↔ 네트워크)
 */
interface NoteRepository {
    suspend fun getAllNotes(): List<Note>
    suspend fun getNoteById(id: String): Note?
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
}

/**
 * ✅ 실제 구현체 — Firestore 연동 버전
 *
 * "주방" 역할 — 메뉴판(인터페이스)에 적힌 요리를 실제로 만듦
 *
 * 실제 앱에서는 @Inject constructor 어노테이션을 붙여서
 * Hilt 가 자동으로 이 객체를 만들도록 한다.
 *
 * 실제 코드:
 * class NoteRepositoryImpl @Inject constructor(
 *     private val firestore: FirebaseFirestore
 * ) : NoteRepository { ... }
 */
class NoteRepositoryImpl : NoteRepository {

    // 연습용 인메모리 저장소 (실제로는 Firestore 사용)
    private val notes = mutableListOf(
        Note(id = "1", title = "첫 번째 노트", content = "Hilt DI 공부하기"),
        Note(id = "2", title = "두 번째 노트", content = "Repository 패턴 익히기"),
        Note(id = "3", title = "세 번째 노트", content = "ViewModel 연결하기")
    )

    override suspend fun getAllNotes(): List<Note> {
        // 실제: firestore.collection("notes").get().await()
        return notes.toList()
    }

    override suspend fun getNoteById(id: String): Note? {
        // 실제: firestore.collection("notes").document(id).get().await()
        return notes.find { it.id == id }
    }

    override suspend fun addNote(note: Note) {
        // 실제: firestore.collection("notes").add(note).await()
        notes.add(note)
    }

    override suspend fun updateNote(note: Note) {
        // 실제: firestore.collection("notes").document(note.id).set(note).await()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) notes[index] = note
    }

    override suspend fun deleteNote(id: String) {
        // 실제: firestore.collection("notes").document(id).delete().await()
        notes.removeAll { it.id == id }
    }
}

/**
 * 🧪 테스트용 가짜(Fake) 구현체
 *
 * 같은 인터페이스를 구현하지만 항상 고정된 데이터를 반환
 * → 테스트할 때 네트워크 없이도 ViewModel 을 검증할 수 있다!
 */
class FakeNoteRepository : NoteRepository {

    val fakeNotes = mutableListOf(
        Note(id = "fake1", title = "테스트 노트", content = "가짜 데이터")
    )

    override suspend fun getAllNotes(): List<Note> = fakeNotes.toList()
    override suspend fun getNoteById(id: String): Note? = fakeNotes.find { it.id == id }
    override suspend fun addNote(note: Note) { fakeNotes.add(note) }
    override suspend fun updateNote(note: Note) {
        val idx = fakeNotes.indexOfFirst { it.id == note.id }
        if (idx != -1) fakeNotes[idx] = note
    }
    override suspend fun deleteNote(id: String) { fakeNotes.removeAll { it.id == id } }
}

// ============================================================
// 4. Hilt Module — "배달 본부의 메뉴 등록부"
// ============================================================

/**
 * ⭐ Hilt Module 이란?
 *
 * Hilt 에게 "이 인터페이스가 요청되면 이 구현체를 배달해줘!" 라고
 * 알려주는 설정 파일이다.
 *
 * 비유: 배달 본부에 "치킨 주문이 들어오면 BBQ 매장에서 가져와"
 *       라고 등록하는 것
 */

/*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ─── 방법 1: @Binds 사용 (인터페이스 ↔ 구현체 연결) ───

@Module
@InstallIn(SingletonComponent::class)  // 앱 전체에서 하나만 만들어 공유
object NoteModule {

    // "NoteRepository 가 필요하면 NoteRepositoryImpl 을 줘!"
    @Binds
    @Singleton  // 한 번만 생성, 앱 전체에서 재사용
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository
}

// ─── 방법 2: @Provides 사용 (직접 객체를 만들어서 제공) ───
// 외부 라이브러리 객체처럼 내가 소스코드를 못 건드리는 경우에 사용

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        firestore: FirebaseFirestore   // Hilt 가 위에서 등록한 Firestore 를 자동 주입
    ): NoteRepository {
        return NoteRepositoryImpl(firestore)
    }
}
*/

// ============================================================
// 연습용 수동 Module (Hilt 없이 같은 개념 체험)
// ============================================================

/**
 * Hilt 가 내부적으로 하는 일을 직접 코드로 재현한 것
 * "의존성 컨테이너" 라고도 부른다
 *
 * Hilt 를 쓰면 이 클래스가 필요 없어지지만,
 * 개념 이해를 위해 직접 만들어 본다
 */
object ManualDIContainer {

    // Singleton: 앱 전체에서 하나만 만들어 돌려씀
    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl()
    }

    // 테스트 모드 전환 (실제 Hilt 에서는 @TestInstallIn 으로 처리)
    var isTestMode = false

    fun getNoteRepository(): NoteRepository {
        return if (isTestMode) {
            FakeNoteRepository()  // 테스트용 가짜 배달
        } else {
            noteRepository        // 진짜 배달
        }
    }
}

// ============================================================
// 5. @HiltAndroidApp — 배달 본부 설치
// ============================================================

/**
 * ⭐ Application 클래스에 @HiltAndroidApp 을 붙이면
 *    앱 전체에 Hilt 배달 시스템이 가동된다
 *
 * 비유: 우리 동네에 배달 본부(물류센터)를 세우는 것
 *       이게 없으면 Hilt 주문 자체가 안 됨!
 */

/*
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp   // ← 이 한 줄이 Hilt 의 시작점!
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt 가 내부적으로 의존성 그래프를 여기서 초기화
    }
}
*/

// AndroidManifest.xml 에도 등록 필요:
/*
<application
    android:name=".MyApplication"   ← 이 부분 추가
    ...>
*/

// ============================================================
// 6. @AndroidEntryPoint — 배달 받을 수 있는 건물로 등록
// ============================================================

/**
 * ⭐ Activity, Fragment 에 @AndroidEntryPoint 를 붙이면
 *    해당 화면이 Hilt 배달을 받을 수 있게 된다
 *
 * 비유: 건물 입구에 "택배 수령 가능" 표시를 붙이는 것
 */

/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint   // ← 이 Activity 에서 Hilt 주입 가능!
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // hiltViewModel() 로 Hilt 가 만들어준 ViewModel 사용
            val viewModel: NoteViewModel = hiltViewModel()
            NoteListScreen(viewModel = viewModel)
        }
    }
}
*/

// ============================================================
// 7. @HiltViewModel — ViewModel 에 부품 자동 배달
// ============================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 노트 화면의 UI 상태
 */
data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val selectedNote: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ⭐ @HiltViewModel + @Inject constructor
 *
 * Hilt 가 이 ViewModel 을 만들 때:
 * 1. "NoteRepository 가 필요하네?" 를 자동 감지
 * 2. Module 에서 등록된 NoteRepositoryImpl 을 찾아서
 * 3. 생성자(constructor)에 자동으로 끼워 넣어 줌(주입)
 *
 * 비유: "ViewModel 이라는 요리사에게 재료(Repository)를 자동 배달해주는 것"
 *
 * 실제 코드:
 * @HiltViewModel
 * class NoteViewModel @Inject constructor(
 *     private val noteRepository: NoteRepository
 * ) : ViewModel()
 */
class NoteViewModel(
    // ✅ 인터페이스에 의존 → 구현체가 바뀌어도 ViewModel 코드는 그대로!
    private val noteRepository: NoteRepository = ManualDIContainer.getNoteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    /**
     * 노트 목록 불러오기
     */
    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val notes = noteRepository.getAllNotes()
                _uiState.value = _uiState.value.copy(
                    notes = notes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "노트 로드 실패: ${e.message}"
                )
            }
        }
    }

    /**
     * 새 노트 추가
     */
    fun addNote(title: String, content: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            try {
                val newNote = Note(
                    id = "note_${System.currentTimeMillis()}",
                    title = title,
                    content = content
                )
                noteRepository.addNote(newNote)
                loadNotes()  // 목록 새로고침
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "노트 추가 실패: ${e.message}"
                )
            }
        }
    }

    /**
     * 노트 삭제
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                loadNotes()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "노트 삭제 실패: ${e.message}"
                )
            }
        }
    }

    /**
     * 노트 선택 (상세 보기용)
     */
    fun selectNote(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            _uiState.value = _uiState.value.copy(selectedNote = note)
        }
    }
}

// ============================================================
// 8. Compose UI (참고용)
// ============================================================

/*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NoteListScreen(
    // ⭐ hiltViewModel() → Hilt 가 만들어준 ViewModel 자동 주입!
    viewModel: NoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "노트 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "내 노트",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 로딩 상태
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // 노트 목록
            LazyColumn {
                items(uiState.notes) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note.id) }
                    )
                }
            }

            // 에러 메시지
            uiState.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // 노트 추가 다이얼로그
    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content ->
                viewModel.addNote(title, content)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "삭제")
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 노트 추가") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, content) }) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
*/

// ============================================================
// 9. 전체 흐름 요약 — 배달 시스템 동작 순서
// ============================================================

/**
 * ⭐ Hilt DI 전체 흐름 (배달 비유)
 *
 *  ┌─────────────────────────────────────────────────┐
 *  │  @HiltAndroidApp                                │
 *  │  MyApplication                                  │
 *  │  → 배달 본부(물류센터) 설치                      │
 *  └───────────────────┬─────────────────────────────┘
 *                      │
 *  ┌───────────────────▼─────────────────────────────┐
 *  │  @Module + @InstallIn                            │
 *  │  NoteModule / DatabaseModule                     │
 *  │  → "이 주문이 들어오면 이 매장에서 배달해!"       │
 *  │     NoteRepository 요청 → NoteRepositoryImpl 배달 │
 *  └───────────────────┬─────────────────────────────┘
 *                      │
 *  ┌───────────────────▼─────────────────────────────┐
 *  │  @AndroidEntryPoint                              │
 *  │  MainActivity                                    │
 *  │  → 이 건물은 택배 수령 가능!                      │
 *  └───────────────────┬─────────────────────────────┘
 *                      │
 *  ┌───────────────────▼─────────────────────────────┐
 *  │  @HiltViewModel + @Inject constructor            │
 *  │  NoteViewModel(noteRepository: NoteRepository)   │
 *  │  → "Repository 필요해요!" 하면 Hilt 가 자동 배달  │
 *  └───────────────────┬─────────────────────────────┘
 *                      │
 *  ┌───────────────────▼─────────────────────────────┐
 *  │  Compose UI                                      │
 *  │  hiltViewModel<NoteViewModel>()                  │
 *  │  → Hilt 가 만들어준 ViewModel 을 화면에서 사용    │
 *  └─────────────────────────────────────────────────┘
 */

// ============================================================
// 10. 테스트에서의 활용
// ============================================================

/**
 * DI 의 가장 큰 장점: 테스트가 쉬워진다!
 *
 * ViewModel 이 인터페이스(NoteRepository)에만 의존하기 때문에
 * 테스트할 때 가짜(Fake) 구현체를 끼워 넣을 수 있다.
 */

/*
// Hilt 테스트 설정:
// @HiltAndroidTest
// @UninstallModules(NoteModule::class)  // 기존 모듈 해제
// class NoteViewModelTest {
//
//     @Module
//     @InstallIn(SingletonComponent::class)
//     object TestModule {
//         @Provides
//         @Singleton
//         fun provideFakeRepository(): NoteRepository = FakeNoteRepository()
//     }
//
//     @Inject
//     lateinit var repository: NoteRepository
//
//     @Test
//     fun `노트_추가_테스트`() = runTest {
//         val viewModel = NoteViewModel(repository)
//         viewModel.addNote("테스트", "내용")
//
//         val state = viewModel.uiState.value
//         assert(state.notes.any { it.title == "테스트" })
//     }
// }
*/

// Hilt 없이도 테스트 가능 (수동 DI):
fun testNoteViewModel() {
    println("=== NoteViewModel 테스트 (수동 DI) ===")

    // 가짜 Repository 주입
    val fakeRepo = FakeNoteRepository()
    val viewModel = NoteViewModel(noteRepository = fakeRepo)

    // ✅ 네트워크 없이도 ViewModel 로직을 검증할 수 있다!
    println("초기 노트 수: ${fakeRepo.fakeNotes.size}")  // 1
    println("테스트 완료!")
}

// ============================================================
// 📚 학습 포인트 정리
// ============================================================
/*
 1. DI(의존성 주입) 핵심 원리
    - 객체를 직접 만들지(new) 않고 외부에서 주입받는다
    - 인터페이스에 의존하면 구현체를 자유롭게 교체할 수 있다
    - 테스트, 유지보수, 확장에 유리

 2. Hilt 필수 어노테이션 5가지
    ┌──────────────────────┬───────────────────────────────────┐
    │ 어노테이션            │ 역할                              │
    ├──────────────────────┼───────────────────────────────────┤
    │ @HiltAndroidApp      │ Application 에 붙임, DI 시작점     │
    │ @AndroidEntryPoint   │ Activity/Fragment 에 붙임          │
    │ @HiltViewModel       │ ViewModel 에 붙여서 주입 활성화     │
    │ @Inject constructor  │ "이 생성자로 부품을 배달해줘"       │
    │ @Module + @Provides  │ "이 요청에는 이걸 배달해"           │
    └──────────────────────┴───────────────────────────────────┘

 3. Scope (범위) 이해
    - @Singleton: 앱 전체에서 하나만 생성, 계속 재사용
    - @ActivityScoped: 특정 Activity 살아있는 동안만 유지
    - @ViewModelScoped: ViewModel 살아있는 동안만 유지

 4. Repository 패턴
    UI(Compose) → ViewModel → Repository(인터페이스) → 구현체(DB/API)
    - 각 계층이 독립적 → 한 층을 바꿔도 다른 층에 영향 없음
    - ViewModel 은 "데이터를 어디서 가져오는지" 모르고 관심 없음

 5. @Binds vs @Provides
    - @Binds: 인터페이스 ↔ 구현체 연결 (내가 만든 클래스)
    - @Provides: 직접 객체 생성 (외부 라이브러리, 복잡한 생성 로직)

 6. 이전 세션과의 연결
    - Session 3 (ViewModel): DI 로 ViewModel 에 Repository 주입
    - Session 6 (Firebase): Firestore 를 @Provides 로 제공
    - Session 7 (API): Retrofit 객체를 @Provides 로 제공
    - Session 8 (AI): GeminiService 를 @Provides 로 제공
 */
