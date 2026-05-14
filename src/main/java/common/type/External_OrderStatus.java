package common.type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum External_OrderStatus {
  RECEIVED,
  SHIPPED,
  REJECTED
}
