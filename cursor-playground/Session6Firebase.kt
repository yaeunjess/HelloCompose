package cursor.playground

/**
 * 6교시: Firebase - 앱의 두뇌와 기억 장치
 *
 * 이 파일은 Firebase Auth(인증)와 Firestore(DB)의 개념을 학습하기 위한 연습용 코드입니다.
 * 실제로 실행하려면 Firebase 프로젝트 설정과 google-services.json 이 필요합니다.
 *
 * ⭐ 핵심 개념:
 * - Firebase Auth: 사용자 인증 (구글/카카오 소셜 로그인)
 * - Firestore: NoSQL 클라우드 데이터베이스
 * - UID 기반 데이터 분리: 각 사용자의 데이터를 독립적으로 저장
 */

// ============================================================
// 1. 필요한 의존성 (build.gradle.kts 에 추가)
// ============================================================
/*
// Firebase BoM (Bill of Materials) - 버전 관리 통합
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

// Firebase Auth
implementation("com.google.firebase:firebase-auth-ktx")

// Firestore
implementation("com.google.firebase:firebase-firestore-ktx")

// Google 로그인용
implementation("com.google.android.gms:play-services-auth:20.7.0")
*/

// ============================================================
// 2. 데이터 모델 (Todo 아이템)
// ============================================================

/**
 * Firestore 에 저장될 Todo 데이터 클래스
 *
 * @property id Firestore 문서 ID
 * @property title 할 일 제목
 * @property isCompleted 완료 여부
 * @property createdAt 생성 시간 (밀리초)
 * @property userId 소유자 UID (⭐ 핵심: 사용자별 데이터 분리)
 */
data class TodoItem(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""  // 이 필드로 "내 데이터"만 조회 가능!
)

// ============================================================
// 3. Firebase Auth - 인증 관리
// ============================================================

/**
 * Firebase Auth 를 활용한 인증 Repository
 *
 * 실제 구현 시 아래 import 가 필요합니다:
 * import com.google.firebase.auth.FirebaseAuth
 * import com.google.firebase.auth.GoogleAuthProvider
 */
class AuthRepository {
    // private val auth = FirebaseAuth.getInstance()

    /**
     * 현재 로그인된 사용자의 UID 가져오기
     * 로그인 안 되어 있으면 null 반환
     */
    fun getCurrentUserId(): String? {
        // return auth.currentUser?.uid
        return "mock_user_id_12345" // 연습용 Mock 데이터
    }

    /**
     * 현재 사용자 정보 가져오기
     */
    fun getCurrentUser(): UserInfo? {
        // val firebaseUser = auth.currentUser ?: return null
        // return UserInfo(
        //     uid = firebaseUser.uid,
        //     email = firebaseUser.email ?: "",
        //     displayName = firebaseUser.displayName ?: "익명"
        // )
        return UserInfo(
            uid = "mock_user_id_12345",
            email = "jess@example.com",
            displayName = "제스"
        )
    }

    /**
     * 구글 로그인 처리
     *
     * 실제 구현 흐름:
     * 1. Google Sign-In Intent 실행 → 사용자가 계정 선택
     * 2. 선택된 계정의 idToken 받기
     * 3. Firebase Auth 에 credential 전달
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserInfo> {
        return try {
            // val credential = GoogleAuthProvider.getCredential(idToken, null)
            // val authResult = auth.signInWithCredential(credential).await()
            // val user = authResult.user ?: throw Exception("로그인 실패")
            //
            // Result.success(UserInfo(
            //     uid = user.uid,
            //     email = user.email ?: "",
            //     displayName = user.displayName ?: "익명"
            // ))

            // 연습용 Mock 성공 응답
            Result.success(UserInfo(
                uid = "mock_user_id_12345",
                email = "jess@example.com",
                displayName = "제스"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        // auth.signOut()
        println("로그아웃 완료")
    }
}

/**
 * 사용자 정보 데이터 클래스
 */
data class UserInfo(
    val uid: String,
    val email: String,
    val displayName: String
)

// ============================================================
// 4. Firestore - CRUD 연산
// ============================================================

/**
 * Firestore 를 활용한 Todo Repository
 *
 * ⭐ 핵심 개념: Collection 과 Document
 * - Collection: 문서들의 컨테이너 (예: "todos")
 * - Document: 실제 데이터 (예: 하나의 Todo 아이템)
 *
 * 데이터 구조:
 * todos (Collection)
 *   ├── document_id_1 (Document)
 *   │     ├── title: "장보기"
 *   │     ├── isCompleted: false
 *   │     ├── userId: "user_uid_123"
 *   │     └── createdAt: 1234567890
 *   └── document_id_2 (Document)
 *         └── ...
 */
class TodoRepository(
    private val authRepository: AuthRepository = AuthRepository()
) {
    // private val firestore = Firebase.firestore
    // private val todosCollection = firestore.collection("todos")

    /**
     * ✅ CREATE: 새 Todo 추가
     *
     * @param title 할 일 제목
     * @return 생성된 TodoItem (ID 포함)
     */
    suspend fun addTodo(title: String): Result<TodoItem> {
        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(Exception("로그인이 필요합니다"))

        return try {
            // 실제 Firestore 코드:
            // val documentRef = todosCollection.document() // 자동 ID 생성
            // val todo = TodoItem(
            //     id = documentRef.id,
            //     title = title,
            //     userId = userId  // ⭐ 현재 사용자 UID 저장
            // )
            // documentRef.set(todo).await()
            // Result.success(todo)

            // 연습용 Mock
            val mockTodo = TodoItem(
                id = "mock_doc_${System.currentTimeMillis()}",
                title = title,
                userId = userId
            )
            println("✅ Todo 추가됨: $mockTodo")
            Result.success(mockTodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ READ: 내 Todo 목록 가져오기
     *
     * ⭐ 핵심: userId 로 필터링하여 "내 데이터"만 조회
     */
    suspend fun getMyTodos(): Result<List<TodoItem>> {
        val userId = authRepository.getCurrentUserId()
            ?: return Result.failure(Exception("로그인이 필요합니다"))

        return try {
            // 실제 Firestore 코드:
            // val snapshot = todosCollection
            //     .whereEqualTo("userId", userId)  // ⭐ 내 데이터만 필터링
            //     .orderBy("createdAt", Query.Direction.DESCENDING)
            //     .get()
            //     .await()
            //
            // val todos = snapshot.documents.mapNotNull { doc ->
            //     doc.toObject(TodoItem::class.java)
            // }
            // Result.success(todos)

            // 연습용 Mock 데이터
            val mockTodos = listOf(
                TodoItem(id = "1", title = "Compose 공부하기", userId = userId),
                TodoItem(id = "2", title = "Firebase 연동하기", isCompleted = true, userId = userId),
                TodoItem(id = "3", title = "AI 기능 구현하기", userId = userId)
            )
            Result.success(mockTodos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ UPDATE: Todo 완료 상태 변경
     */
    suspend fun updateTodoCompletion(todoId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            // 실제 Firestore 코드:
            // todosCollection.document(todoId)
            //     .update("isCompleted", isCompleted)
            //     .await()
            // Result.success(Unit)

            // 연습용 Mock
            println("✅ Todo $todoId 완료 상태 변경: $isCompleted")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ DELETE: Todo 삭제
     */
    suspend fun deleteTodo(todoId: String): Result<Unit> {
        return try {
            // 실제 Firestore 코드:
            // todosCollection.document(todoId)
            //     .delete()
            //     .await()
            // Result.success(Unit)

            // 연습용 Mock
            println("🗑️ Todo $todoId 삭제됨")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 실시간 업데이트 리스너 (Firestore 의 강력한 기능!)
     *
     * 다른 기기에서 데이터가 변경되면 자동으로 알림 받음
     */
    fun observeMyTodos(onTodosChanged: (List<TodoItem>) -> Unit) {
        val userId = authRepository.getCurrentUserId() ?: return

        // 실제 Firestore 코드:
        // todosCollection
        //     .whereEqualTo("userId", userId)
        //     .addSnapshotListener { snapshot, error ->
        //         if (error != null) {
        //             println("Error: ${error.message}")
        //             return@addSnapshotListener
        //         }
        //
        //         val todos = snapshot?.documents?.mapNotNull { doc ->
        //             doc.toObject(TodoItem::class.java)
        //         } ?: emptyList()
        //
        //         onTodosChanged(todos)
        //     }

        // 연습용: 콜백 즉시 호출
        onTodosChanged(listOf(
            TodoItem(id = "1", title = "실시간 동기화 테스트", userId = userId)
        ))
    }
}

// ============================================================
// 5. ViewModel - UI 와 비즈니스 로직 연결
// ============================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Todo 화면의 상태
 */
data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: UserInfo? = null
)

/**
 * Todo 화면의 ViewModel
 *
 * UI 에서 이 ViewModel 을 통해 Firebase 와 상호작용
 */
class TodoViewModel(
    private val todoRepository: TodoRepository = TodoRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        // 앱 시작 시 현재 사용자 확인 및 Todo 목록 로드
        checkCurrentUser()
        loadTodos()
    }

    private fun checkCurrentUser() {
        val user = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(currentUser = user)
    }

    /**
     * Todo 목록 불러오기
     */
    fun loadTodos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            todoRepository.getMyTodos()
                .onSuccess { todos ->
                    _uiState.value = _uiState.value.copy(
                        todos = todos,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    /**
     * 새 Todo 추가
     */
    fun addTodo(title: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            todoRepository.addTodo(title)
                .onSuccess {
                    loadTodos() // 목록 새로고침
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(error = exception.message)
                }
        }
    }

    /**
     * Todo 완료 토글
     */
    fun toggleTodoCompletion(todo: TodoItem) {
        viewModelScope.launch {
            todoRepository.updateTodoCompletion(todo.id, !todo.isCompleted)
                .onSuccess {
                    loadTodos()
                }
        }
    }

    /**
     * Todo 삭제
     */
    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todoId)
                .onSuccess {
                    loadTodos()
                }
        }
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        authRepository.signOut()
        _uiState.value = TodoUiState() // 상태 초기화
    }
}

// ============================================================
// 6. Compose UI (참고용)
// ============================================================

/*
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 사용자 정보 표시
        uiState.currentUser?.let { user ->
            Text("안녕하세요, ${user.displayName}님!")
            Text("UID: ${user.uid}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Todo 입력 필드
        var newTodoTitle by remember { mutableStateOf("") }
        Row {
            OutlinedTextField(
                value = newTodoTitle,
                onValueChange = { newTodoTitle = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("새 할 일 입력") }
            )
            Button(onClick = {
                viewModel.addTodo(newTodoTitle)
                newTodoTitle = ""
            }) {
                Text("추가")
            }
        }

        // Todo 목록
        LazyColumn {
            items(uiState.todos) { todo ->
                TodoItemRow(
                    todo = todo,
                    onToggle = { viewModel.toggleTodoCompletion(todo) },
                    onDelete = { viewModel.deleteTodo(todo.id) }
                )
            }
        }
    }
}
*/

// ============================================================
// 📚 학습 포인트 정리
// ============================================================
/*
 1. Firebase Auth
    - FirebaseAuth.getInstance() 로 인증 객체 획득
    - currentUser?.uid 로 현재 로그인 사용자 UID 확인
    - signInWithCredential() 로 소셜 로그인 처리

 2. Firestore CRUD
    - collection().document() 로 문서 참조 생성
    - set() / add() 로 데이터 생성
    - get() 으로 데이터 읽기
    - update() 로 특정 필드 수정
    - delete() 로 문서 삭제

 3. 데이터 분리 전략 ⭐
    - 모든 문서에 userId 필드 추가
    - whereEqualTo("userId", currentUserId) 로 내 데이터만 조회
    - Firestore 보안 규칙으로 다른 사용자 데이터 접근 차단

 4. 보안 규칙 예시 (Firebase Console 에서 설정):
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        match /todos/{todoId} {
          allow read, write: if request.auth != null
                            && request.auth.uid == resource.data.userId;
        }
      }
    }
 */
