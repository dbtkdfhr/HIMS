package store.receipt;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StoreReceiptDTO {

  private long storeReceiptId;
  private long orderRequestId;
  private long confirmEmployeeId;
  private int receivedQuantity;
  private int differenceQuantity;
  private String differenceReason;
  private String receiptStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
