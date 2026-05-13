package common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationStatus {
  ACTIVE("정상영업"),
  INACTIVE("휴점"),
  CLOSED("폐점");

  private final String displayName;
}
