package store.inventory;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StoreInventoryDTO {

  private long storeId;
  private long productId;
  private int currentQuantity;
  private int safetyQuantity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
