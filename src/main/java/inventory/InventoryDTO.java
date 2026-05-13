package inventory;

import java.time.LocalDateTime;
import common.type.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {
  private Long storeId; // 매장 ID
  private Long productId; // 상품 ID
  private int currentQuantity; // 현재 재고 수량
  private int safetyQuantity; // 안전 재고 수량
  private LocalDateTime updatedAt; // 최종 수정 일시

  private String productName; // 상품명
  private int price; // 판매가
  private String seasonType; // 시즌 구분
  private ProductStatus productStatus; // 상품 상태

  private String brandName; // 브랜드명

  private String categoryName; // 카테고리명

  private boolean isLowStock; // 안전재고 이하 여부

}
