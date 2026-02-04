package cursor.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 3교시: ViewModel + Coroutines 연습용 파일 (로직 부분)
 *
 * - 화면 회전에도 데이터 유지: ViewModel 사용
 * - viewModelScope.launch 로 2초 뒤에 텍스트 바뀌는 가짜 로딩 만들기
 *
 * 이 ViewModel 은 아직 실제 화면에 연결되어 있지 않은 연습용입니다.
 */
class LoadingViewModel : ViewModel() {

    // 실제 앱에서는 StateFlow, LiveData 등을 쓰는 것이 좋지만
    // 여기서는 개념 이해만을 위해 아주 단순한 형태로 작성
    var status: String = "대기 중..."
        private set

    /**
     * 2초 동안 "로딩 중..." 상태를 보여준 뒤 "완료!" 로 변경하는 가짜 비동기 작업
     */
    fun startFakeLoading(onStatusChanged: (String) -> Unit) {
        // viewModelScope: ViewModel 이 살아있는 동안만 코루틴이 동작
        viewModelScope.launch {
            status = "로딩 중..."
            onStatusChanged(status)

            delay(2000L) // 2초 대기 (네트워크 호출이 있다고 가정)

            status = "완료!"
            onStatusChanged(status)
        }
    }
}

