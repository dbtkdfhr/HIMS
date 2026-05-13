package common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // enum은 불변객체라 setter가 필요없어서 data대신 getter사용
@RequiredArgsConstructor // 필수 필드만 받는 생성자 자동 생성 -> final
public enum ProductStatus {
  ON_SALE("판매 가능"), // ProductStatus ON_SALE = new ProductStatus("판매 가능"); 이거와 같은 구조
  STOPPED("품절"), DISCONTINUED("판매 중지");

  private final String displayName;

}
