package common.type;

public enum OrderStatus {
  REQUESTED,   // 발주 요청 (대기)
  APPROVED,    // 발주 승인
  REJECTED,    // 발주 반려
  RECEIVED,    // 외부 발주처 출고 완료 및 입고 검수 대기
  CANCELED,    // 외부 발주처 거절 또는 요청 취소
  DONE         // 입고 확인 또는 입고 취소 후 최종 상태
}
