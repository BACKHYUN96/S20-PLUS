package app.purebit.core

import org.junit.Assert.*
import org.junit.Test

class SelfDiagnosticsTest {
    private val source = PcmFormat(44100,16,2)
    private fun evidence() = DiagnosticEvidence(source, source, true, true, true, listOf(48000,96000), false, false, true, 973728, true, 0)
    @Test fun samsung44kCaseWarnsWithoutInventingActualDacRate() {
        val report = SelfDiagnostics.summarize(evidence())
        assertTrue(report.contains("리샘플링 가능성"))
        assertTrue(report.contains("실제 rate/변환 위치는 미확인"))
        assertTrue(report.contains("BIT_PERFECT mixer 정책이 노출되지 않음"))
        assertTrue(report.contains("APP OUTPUT BUFFER VERIFIED"))
        assertTrue(report.contains("BIT-PERFECT NOT VERIFIED"))
        assertFalse(report.contains("VERIFIED BIT-PERFECT"))
    }
    @Test fun matchingCapabilitiesAreNotDevicePathProof() {
        val report = SelfDiagnostics.summarize(evidence().copy(reportedRates=listOf(44100), exactProfile=true, exactBitPerfectPolicy=true))
        assertFalse(report.contains("리샘플링 가능성"))
        assertTrue(report.contains("현재 DAC rate 증거는 아님"))
        assertTrue(report.contains("DEVICE PATH NOT VERIFIED"))
    }
    @Test fun pausedAndEmptyEvidenceDoNotReuseActiveRouteClaims() {
        val paused = SelfDiagnostics.summarize(evidence().copy(playing=false,routed=false))
        assertTrue(paused.contains("정지 상태 캡처"))
        assertFalse(paused.contains("활성 출력 장치 조회 성공"))
        val empty = SelfDiagnostics.summarize(evidence().copy(source=null,client=null,playing=false,routed=false,
            reportedRates=emptyList(),exactProfile=null,exactBitPerfectPolicy=null,sourceVerified=false,acceptedBytes=0,underruns=null))
        assertTrue(empty.contains("먼저 FLAC/WAV")); assertTrue(empty.contains("SOURCE PCM NOT VERIFIED"))
        assertFalse(empty.contains("APP OUTPUT BUFFER VERIFIED"))
    }
    @Test fun unknownRatesAndBufferMismatchStayExplicit() {
        val report = SelfDiagnostics.summarize(evidence().copy(reportedRates=emptyList(),buffersEqual=false,underruns=5))
        assertTrue(report.contains("목록이 비어")); assertFalse(report.contains("리샘플링 가능성"))
        assertTrue(report.contains("앱 출력 버퍼 불일치")); assertTrue(report.contains("underruns: 5"))
    }
    @Test fun recentLogsAreBoundedAndRetainNewestEntries() {
        val log = RecentLog(20)
        log.append("old entry"); log.append("new entry"); log.append("last entry")
        val text=log.snapshot()
        assertFalse(text.contains("old entry")); assertTrue(text.contains("last entry"))
        assertTrue(text.contains("dropped="))
        log.append("x".repeat(100)); assertTrue(log.snapshot().substringAfter("\n").length<=20)
    }
}
