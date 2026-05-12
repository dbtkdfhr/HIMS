package store;

import java.sql.Date;
import lombok.Data;

@Data
public class StoreDTO {

  private int storeId;
  private int branchId;
  private int brandId;
  private String storeName;
  private String floorInfo;
  private String storeLocation;
  private String operationStatus;
  private Date createdAt;
  private Date updatedAt;
}
