package common.type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum External_OrderStatus {
  RECEIVED("접수 완료"),
  SHIPPED("출고 완료"),
  REJECTED("출고 반려");

  private final String displayName;
}
