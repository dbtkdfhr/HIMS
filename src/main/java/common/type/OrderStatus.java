package common.type;

public enum OrderStatus {
  REQUESTED("발주 요청"), // 발주 요청 (대기)
  APPROVED("발주 승인"), // 발주 승인
  REJECTED("발주 반려"), // 발주 반려
  RECEIVED("입고 대기"), // 외부 발주처 출고 완료 및 입고 검수 대기
  CANCELED("취소됨"), // 외부 발주처 거절 또는 요청 취소
  DONE("처리 완료");

  private final String displayName;

  OrderStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
