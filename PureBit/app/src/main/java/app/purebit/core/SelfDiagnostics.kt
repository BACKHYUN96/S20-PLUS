package app.purebit.core

data class DiagnosticEvidence(
    val source: PcmFormat?, val client: PcmFormat?, val playing: Boolean,
    val routed: Boolean, val usbRoute: Boolean, val reportedRates: List<Int>,
    val exactProfile: Boolean?, val exactBitPerfectPolicy: Boolean?,
    val sourceVerified: Boolean, val acceptedBytes: Long, val buffersEqual: Boolean,
    val underruns: Int?
)

object SelfDiagnostics {
    fun summarize(e: DiagnosticEvidence): String = buildString {
        appendLine("PC 없는 자체진단")
        appendLine(if (e.playing) "재생 중 캡처" else "정지 상태 캡처 — 활성 출력은 확인할 수 없습니다.")
        appendLine(if (e.source == null) "[미확인] 먼저 FLAC/WAV를 선택하세요." else "[정보] 원본: ${e.source}")
        appendLine(if (e.playing && e.routed) "[확인] 활성 출력 장치 조회 성공 (${if (e.usbRoute) "USB" else "일반 Android 출력"})"
            else "[미확인] 현재 활성 출력 장치 없음")
        appendLine(if (e.playing && e.routed && e.source != null && e.client == e.source)
            "[확인] 원본/AudioTrack 클라이언트 포맷 일치 — DAC 포맷 증거는 아님"
            else "[미확인] 활성 원본/클라이언트 포맷 일치 확인 불가")
        if (e.playing && e.routed && e.source != null) {
            appendLine(when {
                e.reportedRates.isEmpty() -> "[미확인] 장치 sample rate 목록이 비어 있습니다."
                e.source.rate !in e.reportedRates -> "[주의] 원본 ${e.source.rate} Hz가 장치 보고 목록 ${e.reportedRates}에 없습니다. 시스템 리샘플링 가능성; 실제 rate/변환 위치는 미확인."
                else -> "[정보] 원본 rate가 장치 지원 목록에 있음 — 현재 DAC rate 증거는 아님"
            })
            appendLine(when (e.exactProfile) {
                true -> "[정보] 원본과 같은 rate/encoding/channel의 장치 profile이 보고됨"
                false -> "[주의] 원본과 정확히 일치하는 장치 profile이 보고되지 않음"
                null -> "[미확인] 장치 profile 조회 불가"
            })
        }
        appendLine(when (e.exactBitPerfectPolicy) {
            true -> "[정보] 원본용 BIT_PERFECT mixer 정책 지원이 보고됨; 적용/실제 경로 증명은 별개"
            false -> "[주의] 원본용 BIT_PERFECT mixer 정책이 노출되지 않음"
            null -> "[미확인] 활성 USB mixer 정책 조회 불가"
        })
        appendLine(if (e.sourceVerified) "[확인] SOURCE PCM VERIFIED (파일/디코딩 범위)" else "[미확인] SOURCE PCM NOT VERIFIED")
        appendLine(when {
            e.acceptedBytes <= 0 -> "[미확인] 현재 구간에 수락된 PCM 바이트 없음"
            !e.buffersEqual -> "[오류] 앱 출력 버퍼 불일치"
            else -> "[확인] APP OUTPUT BUFFER VERIFIED: ${e.acceptedBytes} 바이트 (현재 구간; DAC 수신 증거 아님)"
        })
        appendLine(e.underruns?.let { "[${if (it > 0) "주의" else "정보"}] AudioTrack underruns: $it (현재 track)" } ?: "[미확인] 활성 track underrun 조회 불가")
        appendLine("[미확인] DEVICE PATH NOT VERIFIED / BIT-PERFECT NOT VERIFIED")
        appendLine("일반 앱 권한으로 AudioFlinger/audio_policy dumpsys와 Samsung HAL/DSP 내부 상태를 읽을 수 없습니다. 내부 로그는 PureBit 자체 이벤트만 포함합니다.")
    }
}

/** Bounded in-memory session log; no READ_LOGS permission or other-app log collection. */
class RecentLog(private val limit: Int = 256 * 1024) {
    init { require(limit > 0) }
    private val entries = java.util.ArrayDeque<String>()
    private var length = 0
    private var dropped = 0L
    @Synchronized fun append(entry: String) {
        val line = (entry + "\n").takeLast(limit)
        while (length + line.length > limit && entries.isNotEmpty()) { length -= entries.removeFirst().length; dropped++ }
        entries.addLast(line); length += line.length
    }
    @Synchronized fun snapshot() = "PureBit internal session log; older entries dropped=$dropped\n" + entries.joinToString("")
}
