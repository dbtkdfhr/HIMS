package common.type;

public enum ReceiptStatus {
  RECEIVED("RECEIVED", "정상 입고"),
  PARTIAL_RECEIVED("PARTIAL_RECEIVED", "차이 입고"),
  CANCELED("CANCELED", "입고 반려");

  private final String label; // DB 저장용 (English)
  private final String displayName; // UI 표시용 (Korean)

  ReceiptStatus(String label, String displayName) {
    this.label = label;
    this.displayName = displayName;
  }

  public String getLabel() {
    return label;
  }

  public String getDisplayName() {
    return displayName;
  }
}
