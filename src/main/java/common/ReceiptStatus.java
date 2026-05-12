package common;

public enum ReceiptStatus {
  RECEIVED("RECEIVED"), PARTIAL_RECEIVED("PARTIAL_RECEIVED"), CANCELED("CANCELED");

  private final String label;

  ReceiptStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
