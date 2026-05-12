package store.inventory;

import java.sql.Date;
import lombok.Data;

@Data
public class StoreInventoryDTO {

  private int storeId;
  private int productId;
  private int currentQuantity;
  private int safetyQuantity;
  private Date createdAt;
  private Date updatedAt;
}
