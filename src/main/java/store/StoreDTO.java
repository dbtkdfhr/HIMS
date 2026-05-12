package store;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StoreDTO {

  private long storeId;
  private long branchId;
  private long brandId;
  private String storeName;
  private String floorInfo;
  private String storeLocation;
  private String operationStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
