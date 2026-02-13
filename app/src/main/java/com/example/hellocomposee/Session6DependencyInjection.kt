package com.example.hellocomposee

/*
 * 6교시: Hilt를 이용한 의존성 주입 (Dependency Injection)
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
// 1. DI 가 왜 필요한지 — Before & After 비교
// ============================================================

/*
 * ❌ DI 없이 직접 만드는 방식 (Bad)
 *
 * 문제점:
 * 1. ViewModel 이 Repository 를 "직접 생성" → 강한 결합(Tight Coupling)
 * 2. 테스트할 때 가짜(Mock) Repository 로 교체할 수 없다
 * 3. Repository 생성 방법이 바뀌면 모든 ViewModel 을 고쳐야 한다
 */
class BadViewModel_NoJI {
    private val repository = NoteRepositoryImpl()

    suspend fun loadNotes() {
        val notes = repository.getAllNotes()
        println("노트 목록: $notes")
    }
}

/*
* ✅ DI 로 주입받는 방식 (Good)
*
* 장점:
* 1. ViewModel 은 Repository "인터페이스"만 알면 된다 → 느슨한 결합(Loose Coupling)
* 2. 테스트할 때 FakeRepository 를 쉽게 끼워 넣을 수 있다
* 3. Repository 구현이 바뀌어도 ViewModel 은 안 고쳐도 된다
*/
class GoodViewModel_WithDI(
    private val repository: NoteRepository // 외부에서 주입
){
    suspend fun loadNotes(){
        val notes = repository.getAllNotes()
        println("노트 목록: $notes")
    }
}


// ============================================================
// 2. 데이터 모델
// ============================================================
data class Note(
    val id : String = "",
    val title : String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)


// ============================================================
// 3. Repository 패턴 — 인터페이스 + 구현 분리
// ============================================================
/*
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
    suspend fun getAllNotes() : List<Note>
    suspend fun getNoteById(id: String): Note?
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
}

/*
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
class NoteRepositoryImpl : NoteRepository{
    private val notes = mutableListOf(
        Note(id = "1", title = "첫 번째 노트", content = "Hilt DI 공부하기"),
        Note(id = "2", title = "두 번째 노트", content = "Repository 패턴 익히기"),
        Note(id = "3", title = "세 번째 노트", content = "ViewModel 연결하기")
    )

    override suspend fun getAllNotes(): List<Note> {
        return notes.toList()
    }
    override suspend fun getNoteById(id: String): Note? {
        return notes.find { it.id == id }
    }
    override suspend fun addNote(note: Note) {
        notes.add(note)
    }
    override suspend fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) notes[index] = note
    }
    override suspend fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
    }
}

/*
 * 🧪 테스트용 가짜(Fake) 구현체
 *
 * 같은 인터페이스를 구현하지만 항상 고정된 데이터를 반환
 * → 테스트할 때 네트워크 없이도 ViewModel 을 검증할 수 있다!
 */
class FakeNoteRepository : NoteRepository {
    val fakeNotes = mutableListOf(
        Note(id = "fake1", title = "테스트 노트", content = "가짜 데이터")
    )

    override suspend fun getAllNotes(): List<Note> {
        return fakeNotes.toList()
    }
    override suspend fun getNoteById(id: String): Note? {
        return fakeNotes.find { it.id == id }
    }
    override suspend fun addNote(note: Note) {
        fakeNotes.add(note)
    }
    override suspend fun updateNote(note: Note) {
        val idx = fakeNotes.indexOfFirst { it.id == note.id }
        if (idx != -1) fakeNotes[idx] = note
    }
    override suspend fun deleteNote(id: String) {
        fakeNotes.removeAll { it.id == id }
    }
}

// ============================================================
// 4. Hilt Module — "배달 본부의 메뉴 등록부"
// ============================================================

/*
 * ⭐ Hilt Module 이란?
 *
 * Hilt 에게 "이 인터페이스가 요청되면 이 구현체를 배달해줘!" 라고
 * 알려주는 설정 파일이다.
 *
 * 비유: 배달 본부에 "치킨 주문이 들어오면 BBQ 매장에서 가져와"
 *       라고 등록하는 것
 */

