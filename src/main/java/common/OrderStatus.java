package common;

public enum OrderStatus {
  REQUESTED,   // 발주 요청 (대기)
  APPROVED,    // 발주 승인
  REJECTED,    // 발주 반려
  SENT,        // 배송 시작 (외부 발주처 전송 완료)
  RECEIVED,    // 입고 완료 (매장 확인 완료)
  CANCELED     // 요청 취소
}
